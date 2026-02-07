package com.fanda.homebook.book.entity

import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.data.transaction.TransactionSubEntity

/**
 * 月份键值类
 *
 * 用于唯一标识一个月份，作为Map的键使用
 *
 * @property year 年份（如：2024）
 * @property month 月份（1-12）
 */
data class MonthKey(val year: Int, val month: Int)

/**
 * 月份数据类
 *
 * 存储一个月的交易统计数据和分类数据
 *
 * @property yearMonth 年份月份字符串，格式："2024-01"
 * @property monthDisplay 月份显示字符串，格式："1月"
 * @property year 年份
 * @property month 月份（1-12）
 * @property totalIncome 月总收入
 * @property totalExpense 月总支出
 * @property categories 月内各分类统计数据列表
 * @property transactions 月内所有交易记录列表
 */
data class MonthData(
    val yearMonth: String,           // 格式: "2024-01"
    val monthDisplay: String,        // 显示格式: "1月"
    val year: Int, val month: Int, val totalIncome: Double = 0.0,   // 月总收入
    val totalExpense: Double = 0.0,  // 月总支出
    val categories: List<CategoryData> = emptyList(),     // 分类数据
    val transactions: List<AddQuickEntity> = emptyList()  // 交易记录
)

/**
 * 分类统计数据类
 *
 * 存储特定分类在一个月内的交易统计
 *
 * @property subCategory 子分类实体（如："餐饮"、"交通"等）
 * @property categoryType 分类类型（0: 收入, 1: 支出）
 * @property monthDisplay 月份显示字符串
 * @property totalAmount 该分类总金额
 * @property transactions 该分类下的交易记录列表
 */
data class CategoryData(
    val subCategory: TransactionSubEntity?,  // 子分类实体
    val categoryType: Int,                   // 收入或支出类型
    val monthDisplay: String,                // 月份显示
    val totalAmount: Double = 0.0,           // 分类总金额
    val transactions: List<AddQuickEntity> = emptyList()  // 分类交易记录
)

/**
 * 年度统计汇总数据类
 *
 * 存储一整年的交易统计和月度数据
 *
 * @property totalIncome 年总收入
 * @property totalExpense 年总支出
 * @property monthList 月度数据列表（按月份排序）
 */
data class YearSummaryData(
    val totalIncome: Double = 0.0,           // 年总收入
    val totalExpense: Double = 0.0,          // 年总支出
    val monthList: List<MonthData> = emptyList()  // 月度数据列表
)