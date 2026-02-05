package com.fanda.homebook.book.state

import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.entity.TransactionAmountType

data class DashboardUiState(
    val year : Int = 2026,
    val month : Int = 1,
    val title : String = "排行",
    val refresh: Boolean = false,
    val transactionAmountType: TransactionAmountType = TransactionAmountType.EXPENSE,
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE
)