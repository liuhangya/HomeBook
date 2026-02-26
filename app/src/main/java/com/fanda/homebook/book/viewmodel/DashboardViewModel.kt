package com.fanda.homebook.book.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.book.state.DashboardUiState
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.data.quick.QuickEntity
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.entity.TransactionAmountType
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
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 仪表板查询参数数据类
 *
 * @property transactionAmountType 交易金额类型（收入/支出）
 * @property year 查询年份
 * @property month 查询月份（0表示全年）
 * @property refresh 刷新标志
 */
data class DashboardQueryParams(
    val transactionAmountType: TransactionAmountType, val year: Int, val month: Int, val refresh: Boolean
)

/**
 * 仪表板ViewModel
 * 管理仪表板页面的数据状态和业务逻辑
 *
 * @param savedStateHandle 用于保存和恢复状态的句柄
 * @param quickRepository 快速记账数据仓库
 */
class DashboardViewModel(
    savedStateHandle: SavedStateHandle, private val quickRepository: QuickRepository
) : ViewModel() {

    // 从导航参数中获取的初始值
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
        // 获取当前年月
        val (curYear, curMonth) = millisToLocalDate(System.currentTimeMillis())

        // 初始化UI状态
        _uiState.update { it.copy(title = title) }

        // 设置查询年月，如果导航参数为空则使用当前年月
        if (year == null || month == null) {
            _uiState.update { it.copy(year = curYear, month = curMonth) }
        } else {
            _uiState.update { it.copy(year = year, month = month) }
        }

        // 设置交易类型，如果导航参数为空则使用支出类型
        if (type == TransactionAmountType.EXPENSE.ordinal) {
            _uiState.update { it.copy(transactionAmountType = TransactionAmountType.EXPENSE) }
        } else {
            _uiState.update { it.copy(transactionAmountType = TransactionAmountType.INCOME) }
        }
    }

    // 查询参数组合流，当相关状态变化时重新计算
    private val queryParams = combine(
        _uiState.map { it.transactionAmountType },
        _uiState.map { it.year },
        _uiState.map { it.month },
        _uiState.map { it.refresh },
    ) { type, year, month, refresh ->
        DashboardQueryParams(type, year, month, refresh)
    }.distinctUntilChanged()

    // 交易数据流（根据查询参数动态更新）
    @OptIn(ExperimentalCoroutinesApi::class) val transactionData: StateFlow<List<AddQuickEntity>> = queryParams.flatMapLatest { params ->
            LogUtils.d("queryParams transactionDataByDate: $params")
            // 根据账本ID和交易类型获取交易数据，不按分类过滤
            quickRepository.getQuickListByCategory(
                UserCache.bookId, null, null, params.transactionAmountType.ordinal
            )
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
        )

    // 按二级分类分组的交易数据流
    val transactionDataByCategory: StateFlow<List<DashboardSubCategoryGroupData>> = combine(
        _uiState.map { it.year }, _uiState.map { it.month }, transactionData
    ) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 1. 按时间过滤交易数据
        val filteredTransactions = filterTransactions(params.third, params.first, params.second)

        // 2. 按子分类分组
        val groupedByCategory = filteredTransactions.groupBy { rawEntity ->
            rawEntity.subCategory ?: TransactionSubEntity(name = "未分类", categoryId = -1)
        }

        // 3. 计算每个分类的统计数据
        groupedByCategory.map { (categoryEntity, list) ->
            // 计算总金额和分类金额
            val totalAmount = filteredTransactions.sumOf { it.quick.price.toDouble() }
            val categoryAmount = list.sumOf { it.quick.price.toDouble() }

            // 计算金额占比（0-1）
            val ratio: Float = if (totalAmount > 0) {
                categoryAmount.toFloat() / totalAmount.toFloat()
            } else {
                0f
            }

            LogUtils.i("总金额： $totalAmount | 分类金额： $categoryAmount | 占比： $ratio")

            DashboardSubCategoryGroupData(
                category = categoryEntity, data = list, totalAmount = categoryAmount, ratio = ratio
            )
        }.sortedByDescending { it.totalAmount }  // 按金额排序倒序排序
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 按月每天分类分组的交易数据流
    @OptIn(ExperimentalCoroutinesApi::class) val transactionDataByDaily: StateFlow<List<DailyTransactionData>> = combine(
        _uiState.map { it.year }, _uiState.map { it.month }, transactionData
    ) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 1. 按时间过滤交易数据
        val filteredTransactions = filterTransactions(params.third, params.first, params.second)

        // 2. 确定显示的月份
        val month = if (_uiState.value.month <= 0) {
            // 如果选中的是全年，则显示当前月
            Calendar.getInstance().get(Calendar.MONTH) + 1
        } else {
            _uiState.value.month
        }

        // 3. 按天分组
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

        // 4. 计算每日统计数据
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

        // 5. 按日期排序（升序）
        val sortedDailyData = dailyDataList.sortedBy { it.fullDate }

        LogUtils.d("重新过滤每日数据...")

        // 6. 填充缺失的日期（如果需要显示所有天的数据）
        fillMissingDays(sortedDailyData, uiState.value.year, month)
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 按月分类分组的交易数据流
    @OptIn(ExperimentalCoroutinesApi::class) val transactionDataByMonth: StateFlow<List<MonthTransactionData>> = combine(
        _uiState.map { it.year }, _uiState.map { it.month }, transactionData
    ) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 1. 按时间过滤，只按年份过滤（月份设为-1表示不按月份过滤）
        val filteredTransactions = filterTransactions(params.third, params.first, -1)

        // 2. 按月分组
        val groupedByMonth = filteredTransactions.groupBy { entity ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = entity.quick.date
            }
            calendar.get(Calendar.MONTH) + 1  // 月份 1-12
        }

        // 3. 计算每月统计数据
        val monthlyDataList = mutableListOf<MonthTransactionData>()

        // 处理1-12月
        for (month in 1..12) {
            val monthTransactions = groupedByMonth[month] ?: emptyList()
            val monthData = calculateMonthData(month, monthTransactions)
            monthlyDataList.add(monthData)
        }

        LogUtils.d("重新过滤每月数据...")

        // 4. 按月份排序
        monthlyDataList.sortedBy { it.month }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 按日期分类的交易数据流（未分组，直接按时间排序）
    val transactionDataByDate: StateFlow<List<AddQuickEntity>> = combine(
        _uiState.map { it.year }, _uiState.map { it.month }, transactionData
    ) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 按时间过滤
        val filteredTransactions = filterTransactions(params.third, params.first, params.second)
        // 按金额倒序排序
        filteredTransactions.sortedByDescending { it.quick.price.toDouble() }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    /**
     * 保存分类数据列表到用户缓存
     *
     * @param list 分类交易数据列表
     */
    fun saveCategoryDataList(list: List<AddQuickEntity>) {
        UserCache.categoryQuickList = list
    }

    /**
     * 更新查询日期
     *
     * @param year 年份
     * @param month 月份（0表示全年）
     */
    fun updateQueryDate(year: Int, month: Int) {
        _uiState.update { it.copy(year = year, month = month) }
    }

    /**
     * 更新底部弹窗类型
     *
     * @param type 弹窗类型
     */
    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    /**
     * 更新交易金额类型
     *
     * @param type 交易金额类型（收入/支出）
     */
    fun updateTransactionAmountType(type: TransactionAmountType) {
        _uiState.update { it.copy(transactionAmountType = type) }
    }

    /**
     * 检查是否显示指定类型的底部弹窗
     *
     * @param type 弹窗类型
     * @return 如果当前显示的弹窗类型匹配则返回true
     */
    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    /**
     * 关闭底部弹窗
     */
    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    /**
     * 触发数据刷新
     */
    fun refresh() {
        _uiState.update {
            LogUtils.d("刷新数据...")
            it.copy(refresh = !it.refresh)  // 切换刷新标志
        }
    }

    /**
     * 删除交易记录
     *
     * @param quick 要删除的交易记录
     */
    fun deleteQuickDatabase(quick: QuickEntity) {
        viewModelScope.launch {
            quickRepository.delete(quick)
        }
        refresh()  // 删除后刷新数据
    }

    /**
     * 获取总金额标题
     *
     * @return 根据交易类型返回"总支出"或"总收入"
     */
    fun getTotalAmountTitle() = if (uiState.value.transactionAmountType == TransactionAmountType.EXPENSE) {
        "总支出"
    } else {
        "总收入"
    }

    /**
     * 获取饼图标题
     *
     * @return 根据交易类型返回"支出构成"或"收入构成"
     */
    fun getPieChatTitle() = if (uiState.value.transactionAmountType == TransactionAmountType.EXPENSE) {
        "支出构成"
    } else {
        "收入构成"
    }

    /**
     * 获取分类详情标题
     *
     * @param category 分类名称
     * @return 格式化后的分类详情标题
     */
    fun getCategoryDetailTitle(category: String): String {
        return if (uiState.value.transactionAmountType == TransactionAmountType.EXPENSE) {
            if (uiState.value.month <= 0) {
                "${uiState.value.year}年${category}支出"
            } else {
                "${uiState.value.month}月${category}支出"
            }
        } else {
            if (uiState.value.month <= 0) {
                "${uiState.value.year}年${category}收入"
            } else {
                "${uiState.value.month}月${category}收入"
            }
        }
    }

    /**
     * 获取日期分类详情标题
     *
     * @param date 日期字符串
     * @return 格式化后的日期详情标题
     */
    fun getDayCategoryDetailTitle(date: String): String {
        return if (uiState.value.transactionAmountType == TransactionAmountType.EXPENSE) {
            "${date}支出"
        } else {
            "${date}收入"
        }
    }

    /**
     * 获取排行标题
     *
     * @return 格式化后的排行标题
     */
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

    /**
     * 获取总金额文本
     *
     * @return 格式化后的总金额字符串
     */
    fun getTotalAmountText(): String {
        return "%.2f".format(transactionDataByCategory.value.sumOf { it.totalAmount })
    }

    /**
     * 填充缺失的日期（如果需要显示完整的月日历）
     *
     * @param existingData 现有的每日数据
     * @param year 年份
     * @param month 月份
     * @return 填充完整后的每日数据列表
     */
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
                        data = emptyList(),
                        totalAmount = 0.0,
                    )
                )
            }
        }
        return filledList
    }

    /**
     * 计算单月统计数据
     *
     * @param month 月份（1-12）
     * @param transactions 该月的交易列表
     * @return 月份统计数据
     */
    private fun calculateMonthData(month: Int, transactions: List<AddQuickEntity>): MonthTransactionData {
        val totalAmount = transactions.sumOf { it.quick.price.toDouble() }
        return MonthTransactionData(
            month = month,
            data = transactions,
            monthName = "${month}月",
            totalAmount = totalAmount,
        )
    }
}

/**
 * 计算单日统计数据
 *
 * @param dateMillis 日期时间戳
 * @param transactions 该日的交易列表
 * @param calendar 日历对象
 * @return 每日统计数据
 */
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
        data = transactions,
        totalAmount = totalAmount,
    )
}

/**
 * 仪表板子分类分组数据
 *
 * @property category 子分类实体
 * @property data 该分类下的交易数据列表
 * @property totalAmount 该分类的总金额
 * @property ratio 该分类金额占总金额的比例（0-1）
 */
data class DashboardSubCategoryGroupData(
    val category: TransactionSubEntity, val data: List<AddQuickEntity>, val totalAmount: Double, val ratio: Float = 0f
)

/**
 * 每日统计数据模型
 *
 * @property date 日期字符串，格式: "M.d" 如 "9.10"
 * @property displayDate 显示文本，格式: "9月10日"
 * @property data 当日的交易数据列表
 * @property dayOfMonth 月份中的第几天 (1-31)
 * @property fullDate 完整时间戳（用于排序）
 * @property totalAmount 当天所有分类的总金额
 */
data class DailyTransactionData(
    val date: String,
    val displayDate: String,
    val data: List<AddQuickEntity>,
    val dayOfMonth: Int,
    val fullDate: Long,
    val totalAmount: Double,
)

/**
 * 每月统计数据模型
 *
 * @property month 月份（1-12）
 * @property monthName 月份名称，如 "1月"
 * @property data 该月的交易数据列表
 * @property totalAmount 该月的总金额
 * @property color 显示颜色，默认为绿色
 */
data class MonthTransactionData(
    val month: Int, val monthName: String, val data: List<AddQuickEntity>, val totalAmount: Double, val color: Color = Color(0xFF4CAF50)
)

/**
 * 过滤交易数据
 *
 * @param transactions 原始交易数据列表
 * @param year 目标年份
 * @param month 目标月份（0表示全年）
 * @return 过滤后的交易数据列表
 */
fun filterTransactions(
    transactions: List<AddQuickEntity>, year: Int, month: Int
): List<AddQuickEntity> {
    return if (month <= 0) {
        // 按年份过滤
        transactions.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.quick.date }
            cal.get(Calendar.YEAR) == year
        }
    } else {
        // 按年月过滤
        transactions.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.quick.date }
            cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) + 1 == month
        }
    }
}