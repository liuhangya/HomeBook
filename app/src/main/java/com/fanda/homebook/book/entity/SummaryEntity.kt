package com.fanda.homebook.book.entity

import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.data.transaction.TransactionSubEntity

// 月份键值类
data class MonthKey(val year: Int, val month: Int)


// 月份数据
data class MonthData(
    val yearMonth: String, // 格式: "2024-01"
    val monthDisplay: String, // 显示格式: "1月"
    val year: Int, val month: Int, val totalIncome: Double = 0.0, val totalExpense: Double = 0.0, val categories: List<CategoryData> = emptyList(), val transactions: List<AddQuickEntity> = emptyList()
)

// 分类数据
data class CategoryData(
    val subCategory: TransactionSubEntity?, val categoryType: Int, // 收入或支出
    val monthDisplay: String,
    val totalAmount: Double = 0.0, val transactions: List<AddQuickEntity> = emptyList()
)

// 最终的统计结果
data class YearSummaryData(
    val totalIncome: Double = 0.0, val totalExpense: Double = 0.0, val monthList: List<MonthData> = emptyList()
)

// 月份分组
data class MonthGroup(
    val key: String, val monthDisplay: String, val year: Int, val month: Int, val items: List<AddQuickEntity>
)