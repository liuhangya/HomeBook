package com.fanda.homebook.stock.state

import android.net.Uri
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.stock.StockEntity
import com.fanda.homebook.entity.ShowBottomSheetType

data class WatchAndEditStockUiState(
    val stockEntity: StockEntity = StockEntity(),
    val imageUri: Uri? = null,
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
    val isEditState: Boolean = false,
    val showUsedUpDateSelectDialog: Boolean = false,
)