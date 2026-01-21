package com.fanda.homebook.closet.state

import com.fanda.homebook.data.category.SubCategoryEntity

data class SubCategoryUiState(
    val entity: SubCategoryEntity? = null,
    val renameOrDeleteBottomSheet: Boolean = false,
    val editDialog: Boolean = false,
    val addDialog: Boolean = false,
    val addEntity: SubCategoryEntity = SubCategoryEntity(
        name = "", categoryId = 1
    ),
)
