package com.fanda.homebook.book.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.book.entity.CategoryData
import com.fanda.homebook.book.entity.MonthData
import com.fanda.homebook.book.entity.MonthKey
import com.fanda.homebook.book.entity.YearSummaryData
import com.fanda.homebook.book.state.BookUiState
import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.data.book.BookRepository
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.data.quick.QueryParams
import com.fanda.homebook.data.quick.QuickEntity
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.data.quick.TransactionDateGroup
import com.fanda.homebook.data.quick.TransactionGroupedData
import com.fanda.homebook.data.transaction.TransactionRepository
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionWithSubCategories
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
import java.util.concurrent.TimeUnit

class BookViewModel(
    private val bookRepository: BookRepository, private val transactionRepository: TransactionRepository, private val quickRepository: QuickRepository
) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(BookUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    init {
        // 初始化时设置当前年月
        val (year, month) = millisToLocalDate(System.currentTimeMillis())
        _uiState.update { it.copy(year = year, month = month) }
    }

    // 当前选中账本
    @OptIn(ExperimentalCoroutinesApi::class) val curSelectedBook: StateFlow<BookEntity?> = _uiState.map { it.curSelectBookId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->     // 每当上游（curSelectBookId）变化，就取消之前的流，启动新的
            UserCache.bookId = id  // 更新用户缓存中的账本ID
            bookRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 当前选中的二级分类
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<TransactionSubEntity?> = _uiState.map { it.subCategoryId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->     // 每当上游（subCategoryId）变化，就取消之前的流，启动新的
            transactionRepository.getSubItemById(id ?: -1)  // 如果为null则传-1
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 核心查询参数组合
    @OptIn(ExperimentalCoroutinesApi::class) private val queryParams = combine(
        _uiState.map { it.curSelectBookId },
        _uiState.map { it.subCategoryId },
        _uiState.map { it.year },
        _uiState.map { it.month },
        _uiState.map { it.refresh },
    ) { bookId, subCategoryId, year, month, refresh ->
        QueryParams(bookId, subCategoryId, year, month, refresh)
    }.distinctUntilChanged()

    // 交易数据流（根据查询参数动态更新）
    @OptIn(ExperimentalCoroutinesApi::class) val transactionData: StateFlow<List<AddQuickEntity>> = queryParams.flatMapLatest { params ->
            LogUtils.d("queryParams transactionData: $params")
            quickRepository.getQuickListByCategory(
                params.bookId, uiState.value.categoryId, params.subCategoryId
            )
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
        )

    // 按日期分类的月度汇总数据流
    val monthSummaryData: StateFlow<TransactionGroupedData?> = combine(
        _uiState.map { it.year }, _uiState.map { it.month }, transactionData
    ) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 如果是全年（month <= 0），返回null，全年的数据单独处理
        if (params.second <= 0) {
            null
        } else {
            // 1. 按选中的年月过滤交易数据
            val filteredTransactions = filterTransactions(params.third, params.first, params.second)
            // 2. 按日期分组并格式化
            groupTransactionsByDate(filteredTransactions)
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
    )

    // 全年按日期分类的年度汇总数据流
    val yearSummaryData: StateFlow<YearSummaryData?> = combine(
        _uiState.map { it.year }, _uiState.map { it.month }, transactionData
    ) { year, month, list ->
        Triple(year, month, list)
    }.map { params ->
        // 如果不是全年（month > 0），返回null
        if (params.second > 0) {
            null
        } else {
            // 1. 按选中的年份过滤交易数据
            val filteredTransactions = filterTransactions(params.third, params.first, params.second)
            // 2. 分组并计算统计数据
            groupAndCalculateStatistics(filteredTransactions)
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
    )

    // 账本列表数据流
    val books: StateFlow<List<BookEntity>> = bookRepository.getItems().stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 分类数据流（包含主分类和子分类）
    val categories: StateFlow<List<TransactionWithSubCategories>> = transactionRepository.getAllItemsWithSub().stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    /**
     * 更新账本实体到数据库
     *
     * @param bookEntity 要更新的账本实体
     */
    fun updateBookEntityDatabase(bookEntity: BookEntity?) {
        bookEntity?.let {
            viewModelScope.launch {
                bookRepository.update(bookEntity)
            }
        }
    }

    /**
     * 插入新账本到数据库
     *
     * @param name 账本名称
     */
    fun insertBookEntityDatabase(name: String) {
        viewModelScope.launch {
            bookRepository.insert(BookEntity(name = name))
        }
    }

    /**
     * 从数据库删除账本
     *
     * @param bookEntity 要删除的账本实体
     */
    fun deleteBookEntityDatabase(bookEntity: BookEntity?) {
        bookEntity?.let {
            viewModelScope.launch {
                bookRepository.delete(bookEntity)
            }
        }
    }

    /**
     * 更新当前编辑的账本实体
     *
     * @param bookEntity 编辑中的账本实体
     */
    fun updateEditBookEntity(bookEntity: BookEntity?) {
        _uiState.value = _uiState.value.copy(curEditBookEntity = bookEntity)
    }

    /**
     * 更新编辑账本状态
     *
     * @param isEdit 是否为编辑模式
     */
    fun updateEditBookStatus(isEdit: Boolean) {
        _uiState.update { it.copy(isEditBook = isEdit) }
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
     * 更新查询分类
     *
     * @param subCategoryEntity 子分类实体
     */
    fun updateQueryCategory(subCategoryEntity: TransactionSubEntity?) {
        _uiState.update {
            it.copy(
                categoryId = subCategoryEntity?.categoryId,  // 更新主分类ID
                subCategoryId = subCategoryEntity?.id        // 更新子分类ID
            )
        }
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
     * 更新选中的账本
     *
     * @param bookId 账本ID
     */
    fun updateSelectBook(bookId: Int) {
        _uiState.update { it.copy(curSelectBookId = bookId) }
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
        LogUtils.i("刷新账单首页面数据")
        _uiState.update {
            it.copy(refresh = !it.refresh)  // 切换刷新标志
        }
    }

    /**
     * 删除快速记账记录
     *
     * @param quick 要删除的快速记账实体
     */
    fun deleteQuickDatabase(quick: QuickEntity) {
        viewModelScope.launch {
            quickRepository.delete(quick)
        }
        refresh()  // 删除后刷新数据
    }

    /**
     * 按日期分组并格式化的核心方法
     *
     * @param transactions 交易数据列表
     * @return 按日期分组的数据
     */
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
        }.sortedByDescending { it.date } // 按日期倒序排列（最新的在前）

        return TransactionGroupedData(
            year = currentYear, month = currentMonth, groups = dateGroups
        )
    }

    /**
     * 创建日期分组对象
     *
     * @param dateMillis 日期时间戳
     * @param transactions 该日期的交易列表
     * @return 日期分组对象
     */
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

        return TransactionDateGroup(date = dateMillis, displayDate = displayDate, sortOrder = sortOrder, transactions = transactions.sortedByDescending { it.quick.date }  // 按时间倒序排列
        )
    }

    /**
     * 获取星期几的中文表示
     *
     * @param calendar 日历对象
     * @return 星期几的中文字符串
     */
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

    /**
     * 分组和统计逻辑（用于年度数据）
     *
     * @param transactions 交易数据列表
     * @return 年度统计数据
     */
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
                year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH) + 1  // Calendar月份从0开始，加1转为1-12
            )
        }

        // 转换并排序月份
        val monthDataList = groupedByMonth.map { (monthKey, monthTransactions) ->
            createMonthData(monthKey, monthTransactions)
        }.sortedByDescending { "${it.year}${String.format("%02d", it.month)}" }  // 按年月倒序排列

        // 计算全年总计
        val totalIncome = monthDataList.sumOf { it.totalIncome }
        val totalExpense = monthDataList.sumOf { it.totalExpense }

        return YearSummaryData(
            totalIncome = totalIncome, totalExpense = totalExpense, monthList = monthDataList
        )
    }

    /**
     * 创建月份数据对象
     *
     * @param monthKey 月份键（年份+月份）
     * @param transactions 该月的交易列表
     * @return 月份数据对象
     */
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
            // 先按分类类型排序（收入在前，支出在后）
            it.categoryType == TransactionAmountType.INCOME.ordinal
        }.thenByDescending {
            // 再按总金额降序排序
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

    /**
     * 获取分类详情标题
     *
     * @param type 交易类型（收入/支出）
     * @param monthDisplay 月份显示文本
     * @param category 分类名称
     * @return 格式化后的标题
     */
    fun getCategoryDetailTitle(type: Int, monthDisplay: String, category: String): String {
        return if (type == TransactionAmountType.EXPENSE.ordinal) {
            "${monthDisplay}${category}支出"
        } else {
            "${monthDisplay}${category}收入"
        }
    }
}