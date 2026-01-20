package com.fanda.homebook.closet.state

import com.fanda.homebook.data.size.SizeEntity

data class SizeUiState(
    val entity: SizeEntity? = null,
    val renameOrDeleteBottomSheet: Boolean = false,
    val editDialog: Boolean = false,
    val addDialog: Boolean = false,
    val addEntity: SizeEntity = SizeEntity(
        name = "",
    ),
)
