package com.fanda.homebook.book.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.book.entity.CategoryData
import com.fanda.homebook.book.entity.MonthData
import com.fanda.homebook.book.entity.MonthGroup
import com.fanda.homebook.book.entity.MonthKey
import com.fanda.homebook.book.entity.YearSummaryData
import com.fanda.homebook.book.state.BookUiState
import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.data.book.BookRepository
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.data.quick.QueryParams
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.data.quick.TransactionDateGroup
import com.fanda.homebook.data.quick.TransactionGroupedData
import com.fanda.homebook.data.transaction.TransactionRepository
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionWithSubCategories
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
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BookViewModel(
    private val bookRepository: BookRepository, private val transactionRepository: TransactionRepository, private val quickRepository: QuickRepository
) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(BookUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    init {
        val (year, month) = millisToLocalDate(System.currentTimeMillis())
        _uiState.update { it.copy(year = year, month = month) }
    }

    // 当前选中账本
    @OptIn(ExperimentalCoroutinesApi::class) val curSelectedBook: StateFlow<BookEntity?> = _uiState.map { it.curSelectBookId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            UserCache.bookId = id
            bookRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的二级分类
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<TransactionSubEntity?> = _uiState.map { it.subCategoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            transactionRepository.getSubItemById(id ?: -1)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 核心查询参数组合
    @OptIn(ExperimentalCoroutinesApi::class)

    private val queryParams = combine(
        _uiState.map { it.curSelectBookId },
        _uiState.map { it.subCategoryId },
        _uiState.map { it.year },
        _uiState.map { it.month },
        _uiState.map { it.refresh },
    ) { bookId, subCategoryId, year, month, refresh ->
        QueryParams(bookId, subCategoryId, year, month, refresh)
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class) val transactionData: StateFlow<List<AddQuickEntity>> = queryParams.flatMapLatest { params ->
        LogUtils.d("queryParams transactionData: $params")
        quickRepository.getQuickListByCategory(params.bookId, uiState.value.categoryId, params.subCategoryId)
    }.stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 按日期分类的 StateFlow
    val monthSummaryData: StateFlow<TransactionGroupedData?> = combine(_uiState.map { it.year }, _uiState.map { it.month }, transactionData) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 如果是全年，返回空列表，全年的数据单独处理
        if (params.second <= 0) {
            null
        } else {
            // 1. 按选中的年月过滤
            val filteredTransactions = filterTransactions(params.third, params.first, params.second)
            // 按日期分组并格式化
            groupTransactionsByDate(filteredTransactions)
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
    )

    // 全年按日期分类的 StateFlow
    val yearSummaryData: StateFlow<YearSummaryData?> = combine(_uiState.map { it.year }, _uiState.map { it.month }, transactionData) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 如果不是全年，返回空列表
        if (params.second > 0) {
            null
        } else {
            // 1. 按选中的年月过滤
            val filteredTransactions = filterTransactions(params.third, params.first, params.second)
            groupAndCalculateStatistics(filteredTransactions)
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
    )

    // 账本列表
    val books: StateFlow<List<BookEntity>> = bookRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 分类
    val categories: StateFlow<List<TransactionWithSubCategories>> = transactionRepository.getAllItemsWithSub().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    fun updateBookEntityDatabase(bookEntity: BookEntity?) {
        bookEntity?.let {
            viewModelScope.launch {
                bookRepository.update(bookEntity)
            }
        }
    }

    fun insertBookEntityDatabase(name: String) {
        viewModelScope.launch {
            bookRepository.insert(BookEntity(name = name))
        }
    }

    fun deleteBookEntityDatabase(bookEntity: BookEntity?) {
        bookEntity?.let {
            viewModelScope.launch {
                bookRepository.delete(bookEntity)
            }
        }
    }


    fun updateEditBookEntity(bookEntity: BookEntity?) {
        _uiState.value = _uiState.value.copy(curEditBookEntity = bookEntity)
    }

    fun updateEditBookStatus(isEdit: Boolean) {
        _uiState.update { it.copy(isEditBook = isEdit) }
    }

    fun updateQueryDate(year: Int, month: Int) {
        _uiState.update { it.copy(year = year, month = month) }
    }

    fun updateQueryCategory(
        subCategoryEntity: TransactionSubEntity?
    ) {
        // 如果没有子分类，则将子分类ID设置为null
        _uiState.update {
            it.copy(categoryId = subCategoryEntity?.categoryId, subCategoryId = subCategoryEntity?.id)
        }
    }

    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    fun updateSelectBook(bookId: Int) {
        _uiState.update { it.copy(curSelectBookId = bookId) }
    }

    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    fun refreshBooks() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(refresh = !it.refresh)
            }
        }
    }

    // 按选中的年月过滤交易数据
    private fun filterTransactionsByMonth(
        transactions: List<AddQuickEntity>, selectedYear: Int, selectedMonth: Int
    ): List<AddQuickEntity> {
        // 计算选中年月的开始和结束时间戳
        val (startTime, endTime) = getMonthTimeRange(selectedYear, selectedMonth)

        return transactions.filter { transaction ->
            transaction.quick.date in startTime..<endTime
        }
    }

    // 获取指定年月的开始和结束时间戳
    private fun getMonthTimeRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0) // 月份从0开始
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis

        // 下个月的第一天
        calendar.add(Calendar.MONTH, 1)
        val endTime = calendar.timeInMillis

        return Pair(startTime, endTime)
    }

    // 按日期分组并格式化的核心方法
    private fun groupTransactionsByDate(
        transactions: List<AddQuickEntity>
    ): TransactionGroupedData {
        val currentYear = _uiState.value.year
        val currentMonth = _uiState.value.month

        // 1. 按日期分组（忽略时分秒）
        val groupedByDay = transactions.groupBy { transaction ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = transaction.quick.date
            }
            // 只取年月日作为分组键
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }

        // 2. 创建日期分组并格式化显示
        val dateGroups = groupedByDay.map { (dateMillis, dayTransactions) ->
            createDateGroup(dateMillis, dayTransactions)
        }.sortedByDescending { it.date } // 按日期倒序排列

        return TransactionGroupedData(
            year = currentYear, month = currentMonth, groups = dateGroups
        )
    }

    private fun createDateGroup(
        dateMillis: Long, transactions: List<AddQuickEntity>
    ): TransactionDateGroup {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetDate = Calendar.getInstance().apply {
            timeInMillis = dateMillis
        }

        // 计算日期差
        val diffInMillis = today.timeInMillis - targetDate.timeInMillis
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

        // 格式化显示文本
        val displayDate = when (diffInDays) {
            0 -> "今天"
            1 -> "昨天"
            2 -> "前天"
            else -> {
                // 显示星期几
                getDayOfWeekChinese(targetDate)
            }
        }

        // 排序顺序：越近的日期排序值越大
        val sortOrder = Int.MAX_VALUE - diffInDays

        return TransactionDateGroup(
            date = dateMillis, displayDate = displayDate, sortOrder = sortOrder, transactions = transactions.sortedByDescending { it.quick.date })
    }

    private fun getDayOfWeekChinese(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "周日"
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            else -> ""
        }
    }

    // 处理年统计数据 ===================================================================================================

    // 分组和统计逻辑
    private fun groupAndCalculateStatistics(
        transactions: List<AddQuickEntity>
    ): YearSummaryData {
        if (transactions.isEmpty()) {
            return YearSummaryData()
        }

        // 按年月分组
        val groupedByMonth = transactions.groupBy { entity ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = entity.quick.date
            }
            MonthKey(
                year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH) + 1
            )
        }

        // 转换并排序月份
        val monthDataList = groupedByMonth.map { (monthKey, monthTransactions) ->
            createMonthData(monthKey, monthTransactions)
        }.sortedByDescending { "${it.year}${String.format("%02d", it.month)}" }

        // 计算全年总计
        val totalIncome = monthDataList.sumOf { it.totalIncome }
        val totalExpense = monthDataList.sumOf { it.totalExpense }

        return YearSummaryData(
            totalIncome = totalIncome, totalExpense = totalExpense, monthList = monthDataList
        )
    }

    private fun createMonthData(
        monthKey: MonthKey, transactions: List<AddQuickEntity>
    ): MonthData {
        // 分离收入和支出
        val incomeTransactions = transactions.filter {
            it.quick.categoryType == TransactionAmountType.INCOME.ordinal
        }
        val expenseTransactions = transactions.filter {
            it.quick.categoryType == TransactionAmountType.EXPENSE.ordinal
        }

        // 计算月总计
        val monthTotalIncome = incomeTransactions.sumOf {
            it.quick.price.toDoubleOrNull() ?: 0.0
        }
        val monthTotalExpense = expenseTransactions.sumOf {
            it.quick.price.toDoubleOrNull() ?: 0.0
        }

        // 按分类分组
        val categoryGroups = transactions.groupBy {
            Pair(it.subCategory, it.quick.categoryType)
        }

        // 构建分类数据并排序
        val categoryDataList = categoryGroups.map { (key, categoryTransactions) ->
            val (subCategory, categoryType) = key
            CategoryData(
                subCategory = subCategory, categoryType = categoryType, totalAmount = categoryTransactions.sumOf {
                    it.quick.price.toDoubleOrNull() ?: 0.0
                }, transactions = categoryTransactions, monthDisplay = "${monthKey.month}月"
            )
        }.sortedWith(compareByDescending<CategoryData> {
            it.categoryType == TransactionAmountType.INCOME.ordinal
        }.thenByDescending {
            it.totalAmount
        })

        return MonthData(
            yearMonth = "${monthKey.year}-${String.format("%02d", monthKey.month)}",
            monthDisplay = "${monthKey.month}月",
            year = monthKey.year,
            month = monthKey.month,
            totalIncome = monthTotalIncome,
            totalExpense = monthTotalExpense,
            categories = categoryDataList,
            transactions = transactions
        )
    }

    fun getCategoryDetailTitle(type: Int, monthDisplay: String, category: String): String {
        return if (type == TransactionAmountType.EXPENSE.ordinal) {
            "${monthDisplay}${category}支出"
        } else {
            "${monthDisplay}${category}收入"
        }
    }

}