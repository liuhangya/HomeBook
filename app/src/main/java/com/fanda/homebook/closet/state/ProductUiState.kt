package com.fanda.homebook.closet.state

import com.fanda.homebook.data.product.ProductEntity

data class ProductUiState(
    val entity: ProductEntity? = null,
    val renameOrDeleteBottomSheet: Boolean = false,
    val editDialog: Boolean = false,
    val addDialog: Boolean = false,
    val addEntity: ProductEntity = ProductEntity(
        name = "",
    ),
)
