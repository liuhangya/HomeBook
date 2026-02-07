package com.fanda.homebook.book.entity

import com.fanda.homebook.common.entity.TransactionAmountType

/**
 * 金额项目实体类
 * 用于表示分类统计中的单个项目，包含名称、金额和交易类型
 *
 * @property name 项目名称（通常是分类名称）
 * @property amount 金额数值
 * @property type 交易类型（收入或支出）
 */
data class AmountItemEntity(
    val name: String, val amount: Float, val type: TransactionAmountType
)

/**
 * 每日明细项目实体类
 * 用于表示某一天内的单笔交易记录
 *
 * @property category 分类ID
 * @property type 交易类型（收入或支出）
 * @property amount 交易金额
 * @property name 交易名称/描述
 * @property payWay 付款方式名称
 * @property remark 备注信息
 */
data class DailyItemEntity(
    val category: Int, val type: TransactionAmountType, val amount: Float, val name: String, val payWay: String, val remark: String
)

/**
 * 每日金额汇总实体类
 * 用于表示某一天的收支汇总信息，包含该天的所有交易明细
 *
 * @property id 唯一标识符
 * @property date 日期字符串（如：2024-01-01）
 * @property week 星期几（如：周一、周二）
 * @property income 当天总收入
 * @property expense 当天总支出
 * @property children 当天所有的交易明细列表
 */
data class DailyAmountEntity(
    val id: Int, val date: String, val week: String, val income: Float, val expense: Float, val children: List<DailyItemEntity>
)

