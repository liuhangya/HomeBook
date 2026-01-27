package com.fanda.homebook.stock.state

import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.stock.StockStatusEntity
import com.fanda.homebook.data.stock.StockUseStatus
import com.fanda.homebook.entity.ShowBottomSheetType

val stockStatusList = listOf(
    StockStatusEntity(
        useStatus = StockUseStatus.ALL, name = "全部", count = 0
    ), StockStatusEntity(
        useStatus = StockUseStatus.USING, name = "使用中", count = 0
    ), StockStatusEntity(
        useStatus = StockUseStatus.NO_USE, name = "未开封", count = 0
    ), StockStatusEntity(
        useStatus = StockUseStatus.USED, name = "已用完", count = 0
    )
)

data class StockHomeUiState(
    val curSelectRackSubCategory: RackSubCategoryEntity? = null,
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
    val curSelectUseStatus: StockStatusEntity = stockStatusList.first(),
    val rackId: Int = 1,
    val count : Int = 0
)

data class StockHomeRackUiState(
    val statusList: List<StockStatusEntity> = stockStatusList,
    val rackId: Int = 1,

)