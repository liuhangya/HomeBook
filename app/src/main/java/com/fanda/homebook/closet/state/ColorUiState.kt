package com.fanda.homebook.closet.state

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.fanda.homebook.data.color.ColorTypeEntity

data class ColorUiState(
    val entity: ColorTypeEntity? = null,
    val renameOrDeleteBottomSheet: Boolean = false,
    val editDialog: Boolean = false,
    val addEntity: ColorTypeEntity = ColorTypeEntity(
        name = "",
        color = Color.Green.toArgb().toLong()
    ),
)
