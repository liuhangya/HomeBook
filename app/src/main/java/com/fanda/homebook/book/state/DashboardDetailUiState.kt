package com.fanda.homebook.book.state

import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.entity.TransactionAmountType

enum class QueryWay {
    AMOUNT, TIME
}

data class DashboardDetailUiState(
    val queryWay: QueryWay = QueryWay.AMOUNT,
    val data : List<AddQuickEntity> = emptyList(),
    val title : String = "",
)