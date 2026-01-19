package com.fanda.homebook.closet.state

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.fanda.homebook.data.color.ColorTypeEntity

data class ColorUiState(
    val colorType: ColorTypeEntity? = null,
    val renameOrDeleteBottomSheet: Boolean = false,
    val editColorDialog: Boolean = false,
    val addColorTypeEntity: ColorTypeEntity = ColorTypeEntity(
        name = "",
        color = Color.Green.toArgb().toLong()
    ),
)
