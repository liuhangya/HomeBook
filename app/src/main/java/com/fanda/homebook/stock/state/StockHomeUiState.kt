package com.fanda.homebook.stock.state

import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.entity.ShowBottomSheetType

data class StockHomeUiState(
    val curSelectRackSubCategory: RackSubCategoryEntity? = null,
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
)