package com.fanda.homebook.book.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.book.state.DashboardUiState
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.data.transaction.TransactionRepository
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.entity.TransactionAmountType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.fanda.homebook.tools.UserCache
import com.fanda.homebook.tools.millisToLocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Calendar

class DashboardViewModel(savedStateHandle: SavedStateHandle, private val quickRepository: QuickRepository) : ViewModel() {

    private val year = savedStateHandle.get<Int>("year")
    private val month = savedStateHandle.get<Int>("month")
    private val type = savedStateHandle.get<Int>("type")
    private val title = savedStateHandle.get<String>("title") ?: "排行"


    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(DashboardUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    init {
        LogUtils.d("init: $year , $month , $type")
        val (curYear, curMonth) = millisToLocalDate(System.currentTimeMillis())
        _uiState.update { it.copy(title = title) }
        if (year == null || month == null) {
            _uiState.update { it.copy(year = curYear, month = curMonth) }
        } else {
            _uiState.update { it.copy(year = year, month = month) }
        }
        if (type == TransactionAmountType.EXPENSE.ordinal) {
            _uiState.update { it.copy(transactionAmountType = TransactionAmountType.EXPENSE) }
        } else {
            _uiState.update { it.copy(transactionAmountType = TransactionAmountType.INCOME) }
        }
    }

    private val queryParams = combine(_uiState.map { it.transactionAmountType }, _uiState.map { it.year }, _uiState.map { it.month }) { type, year, month ->
        Triple(type, year, month)
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class) val transactionData: StateFlow<List<AddQuickEntity>> = queryParams.flatMapLatest { params ->
        LogUtils.d("queryParams transactionDataByDate: $params")
        quickRepository.getQuickListByCategory(UserCache.bookId, null, null, params.first.ordinal)
    }.stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 按二级分类分组的 StateFlow
    val transactionDataByCategory: StateFlow<List<DashboardSubCategoryGroupData>> = combine(_uiState.map { it.year }, _uiState.map { it.month }, transactionData) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 按时间过滤
        val filteredTransactions = filterTransactions(params.third, params.first, params.second)
        filteredTransactions.groupBy { rawEntity ->
            rawEntity.subCategory ?: TransactionSubEntity(name = "未分类", categoryId = -1)
        }.map { (categoryEntity, list) ->
            val totalAmount = filteredTransactions.sumOf { it.quick.price.toDouble() }
            val categoryAmount = list.sumOf { it.quick.price.toDouble() }
            // 金额占比（0-1）
            val ratio: Float = (if (totalAmount > 0) {
                categoryAmount.toFloat() / totalAmount.toFloat()
            } else {
                0f
            })
            LogUtils.i("总金额： $totalAmount | 分类金额： $categoryAmount | 占比： $ratio")
            DashboardSubCategoryGroupData(categoryEntity, list, categoryAmount, ratio)
        }.sortedBy { it.category.sortOrder }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 按月每天分类分组的 StateFlow
    @OptIn(ExperimentalCoroutinesApi::class) val transactionDataByDaily: StateFlow<List<DailyTransactionData>> =

        combine(_uiState.map { it.year }, _uiState.map { it.month }, transactionData) { year, month, list ->
            Triple(year, month, list)
        }.map { params ->
            // 按时间过滤
            val filteredTransactions = filterTransactions(params.third, params.first, params.second)
            val month = if (_uiState.value.month <= 0) {
                // 如果选中的是全年，则计算当月的
                Calendar.getInstance().get(Calendar.MONTH) + 1
            } else {
                _uiState.value.month
            }

            // 1. 按天分组 【时间戳 - 数据列表】
            val groupedByDay = filteredTransactions.groupBy { entity ->
                // 获取交易日期并转换为当天的开始时间戳
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = entity.quick.date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                calendar.timeInMillis
            }

            // 2. 计算每日统计数据
            val dailyDataList = mutableListOf<DailyTransactionData>()

            groupedByDay.forEach { (dateMillis, dayTransactions) ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = dateMillis
                }

                // 验证是否是目标年月
                val transactionYear = calendar.get(Calendar.YEAR)
                val transactionMonth = calendar.get(Calendar.MONTH) + 1

                if (transactionYear == year && transactionMonth == month) {
                    val dayData = calculateDayData(dateMillis, dayTransactions, calendar)
                    dailyDataList.add(dayData)
                }
            }

            // 3. 按日期排序（升序）
            val sortedDailyData = dailyDataList.sortedBy { it.fullDate }

            LogUtils.d("重新过滤每日数据...")
            // 4. 填充缺失的日期（如果需要显示所有天的数据）
            fillMissingDays(sortedDailyData, uiState.value.year, month)

        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
        )

    // 按月分类分组的 StateFlow
    @OptIn(ExperimentalCoroutinesApi::class) val transactionDataByMonth: StateFlow<List<MonthTransactionData>> = combine(
        _uiState.map { it.year }, _uiState.map { it.month }, transactionData
    ) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 按时间过滤 ，只过滤年，不管有没有选中月份
        val filteredTransactions = filterTransactions(params.third, params.first, -1)
        // 1. 按月分组
        val groupedByMonth = filteredTransactions.groupBy { entity ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = entity.quick.date
            }
            calendar.get(Calendar.MONTH) + 1  // 月份 1-12
        }

        // 2. 计算每月统计数据
        val monthlyDataList = mutableListOf<MonthTransactionData>()

        // 处理1-12月
        for (month in 1..12) {
            val monthTransactions = groupedByMonth[month] ?: emptyList()
            val monthData = calculateMonthData(month, monthTransactions)
            monthlyDataList.add(monthData)
        }
        LogUtils.d("重新过滤每月数据...")
        // 3. 按月份排序
        monthlyDataList.sortedBy { it.month }

    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 按日期分类的 StateFlow
    val transactionDataByDate: StateFlow<List<AddQuickEntity>> = combine(_uiState.map { it.year }, _uiState.map { it.month }, transactionData) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 按时间过滤
        val filteredTransactions = filterTransactions(params.third, params.first, params.second)
        filteredTransactions.sortedBy { it.quick.date }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    fun saveCategoryDataList(list: List<AddQuickEntity>) {
        UserCache.categoryQuickList = list
    }

    fun updateQueryDate(year: Int, month: Int) {
        _uiState.update { it.copy(year = year, month = month) }
    }

    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    fun updateTransactionAmountType(type: TransactionAmountType) {
        _uiState.update { it.copy(transactionAmountType = type) }
    }

    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    fun getTotalAmountTitle() = if (uiState.value.transactionAmountType == TransactionAmountType.EXPENSE) {
        "总支出"
    } else {
        "总收入"
    }

    fun getPieChatTitle() = if (uiState.value.transactionAmountType == TransactionAmountType.EXPENSE) {
        "支出构成"
    } else {
        "收入构成"
    }

    fun getCategoryDetailTitle(category: String): String {
        return if (uiState.value.transactionAmountType == TransactionAmountType.EXPENSE) {
            if (uiState.value.month <= 0) {
                "${uiState.value.year}年${category}支出排行"
            } else {
                "${uiState.value.month}月${category}支出排行"
            }
        } else {
            if (uiState.value.month <= 0) {
                "${uiState.value.year}年${category}收入排行"
            } else {
                "${uiState.value.month}月${category}收入排行"
            }
        }
    }

    fun getRankTitle(): String {
        return if (uiState.value.transactionAmountType == TransactionAmountType.EXPENSE) {
            if (uiState.value.month <= 0) {
                "${uiState.value.year}年支出排行"
            } else {
                "${uiState.value.month}月支出排行"
            }
        } else {
            if (uiState.value.month <= 0) {
                "${uiState.value.year}年收入排行"
            } else {
                "${uiState.value.month}月收入排行"
            }
        }
    }


    fun getTotalAmountText(): String {
        return "%.2f".format(transactionDataByCategory.value.sumOf { it.totalAmount })
    }

    // 填充缺失的日期（如果需要显示完整的月日历）
    private fun fillMissingDays(
        existingData: List<DailyTransactionData>, year: Int, month: Int
    ): List<DailyTransactionData> {
        val filledList = mutableListOf<DailyTransactionData>()

        // 获取该月的天数
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, 1)
        }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 创建现有数据的映射（按dayOfMonth）
        val existingMap = existingData.associateBy { it.dayOfMonth }

        // 填充1号到最后一天
        for (day in 1..daysInMonth) {
            val existingData = existingMap[day]

            if (existingData != null) {
                filledList.add(existingData)
            } else {
                // 创建没有数据的日期
                calendar.set(year, month - 1, day)
                val dateMillis = calendar.timeInMillis

                val dateStr = "$month.$day"
                val displayDateStr = "${month}月${day}日"

                filledList.add(
                    DailyTransactionData(
                        date = dateStr,
                        displayDate = displayDateStr,
                        dayOfMonth = day,
                        fullDate = dateMillis,
                        totalAmount = 0.0,
                    )
                )
            }
        }
        return filledList
    }


    // 计算单月统计数据
    private fun calculateMonthData(month: Int, transactions: List<AddQuickEntity>): MonthTransactionData {
        val totalAmount = transactions.sumOf { it.quick.price.toDouble() }
        return MonthTransactionData(
            month = month,
            monthName = "${month}月",
            totalAmount = totalAmount,
        )
    }
}


// 计算单日统计数据
private fun calculateDayData(
    dateMillis: Long, transactions: List<AddQuickEntity>, calendar: Calendar
): DailyTransactionData {
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1

    val totalAmount = transactions.sumOf { it.quick.price.toDouble() }
    // 格式化日期字符串
    val dateStr = "$month.$dayOfMonth"  // 格式: "9.10"
    val displayDateStr = "${month}月${dayOfMonth}日"

    return DailyTransactionData(
        date = dateStr,
        displayDate = displayDateStr,
        dayOfMonth = dayOfMonth,
        fullDate = dateMillis,
        totalAmount = totalAmount,
    )
}

data class DashboardSubCategoryGroupData(
    val category: TransactionSubEntity, val data: List<AddQuickEntity>,
    // 总额统计
    val totalAmount: Double,
    // 占比 0-1
    val ratio: Float = 0f
)

// 每日统计数据模型
data class DailyTransactionData(
    val date: String,           // 格式: "M.d" 如 "9.10"
    val displayDate: String,    // 显示文本: "9月10日"
    val dayOfMonth: Int,        // 月份中的第几天 (1-31)
    val fullDate: Long,         // 完整时间戳（用于排序）
    val totalAmount: Double,    // 当天所有分类的总金额
)

// 每日统计数据模型
data class MonthTransactionData(
    val month: Int, val monthName: String,    // 月份名称 "1月"
    val totalAmount: Double,    // 当天所有分类的总金额
    val color: Color = Color(0xFF4CAF50)
)


// 过滤方法
fun filterTransactions(
    transactions: List<AddQuickEntity>, year: Int, month: Int
): List<AddQuickEntity> {
    return if (month <= 0) {
        transactions.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.quick.date }
            cal.get(Calendar.YEAR) == year
        }
    } else {
        transactions.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.quick.date }
            cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) + 1 == month
        }
    }
}