package com.fanda.homebook.closet.state

import com.fanda.homebook.data.color.ColorTypeEntity

data class ColorUiState(
    val colorType: ColorTypeEntity? = null,
    val renameOrDeleteBottomSheet: Boolean = false,
    val editColorDialog: Boolean = false,
)
