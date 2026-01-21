package com.fanda.homebook.closet.state

import com.fanda.homebook.data.category.CategoryEntity

data class CategoryUiState(
    val entity: CategoryEntity? = null,
    val renameOrDeleteBottomSheet: Boolean = false,
    val editDialog: Boolean = false,
    val addDialog: Boolean = false,
    val addEntity: CategoryEntity = CategoryEntity(
        name = "",
    ),
)
