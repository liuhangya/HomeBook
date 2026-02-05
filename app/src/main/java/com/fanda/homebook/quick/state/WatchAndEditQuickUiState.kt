package com.fanda.homebook.quick.state

import com.fanda.homebook.data.quick.QuickEntity
import com.fanda.homebook.entity.ShowBottomSheetType

data class WatchAndEditQuickUiState(val quickEntity: QuickEntity = QuickEntity(categoryId = 1), val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE)