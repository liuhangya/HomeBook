package com.fanda.homebook.closet.state

import android.net.Uri
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.entity.ShowBottomSheetType

data class WatchAndEditClosetUiState(
    val closetEntity: ClosetEntity = ClosetEntity(),
    val imageUri: Uri? = null,
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
    val isEditState: Boolean = false,
    val seasonIds : List<Int> = listOf(),
)
