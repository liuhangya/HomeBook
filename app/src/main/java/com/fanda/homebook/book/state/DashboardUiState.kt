package com.fanda.homebook.book.state

import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.entity.TransactionAmountType

/**
 * 仪表板页面UI状态数据类
 * 管理仪表板页面的所有UI状态，包括时间筛选、标题、交易类型等
 *
 * @property year 当前筛选的年份，默认2026年
 * @property month 当前筛选的月份，默认1月
 * @property title 仪表板页面标题，默认"排行"
 * @property refresh 刷新标志，用于触发数据刷新
 * @property transactionAmountType 交易金额类型（收入/支出），默认支出类型
 * @property sheetType 当前显示的底部弹窗类型，默认NONE表示不显示
 */
data class DashboardUiState(
    // 时间筛选相关状态
    val year: Int = 2026,  // 当前显示的年份
    val month: Int = 1,    // 当前显示的月份

    // 页面显示相关状态
    val title: String = "排行",  // 页面标题

    // 数据刷新状态
    val refresh: Boolean = false,  // 刷新标志，为true时触发数据刷新

    // 交易类型筛选状态
    val transactionAmountType: TransactionAmountType = TransactionAmountType.EXPENSE,  // 默认显示支出数据

    // 底部弹窗显示状态
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE  // 当前显示的弹窗类型
)