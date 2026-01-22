package com.fanda.homebook.closet.state

import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.entity.ShowBottomSheetType

data class WatchAndEditClosetUiState(
    val closetEntity: ClosetEntity = ClosetEntity(),
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
    val isEditState: Boolean = false,
)
