package com.fanda.homebook.quick.state

import com.fanda.homebook.data.pay.PayWayEntity

data class PayWayUiState(
    val entity: PayWayEntity? = null,
    val renameOrDeleteBottomSheet: Boolean = false,
    val editDialog: Boolean = false,
    val addDialog: Boolean = false,
    val addEntity: PayWayEntity = PayWayEntity(
        name = "",
    ),
)
