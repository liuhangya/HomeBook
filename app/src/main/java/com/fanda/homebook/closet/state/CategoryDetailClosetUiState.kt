package com.fanda.homebook.closet.state

import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.closet.AddClosetEntity
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.Category


data class CategoryDetailClosetUiState(
    val categoryId: Int = -1,
    val subCategoryId: Int = -1,
    val categoryName: String = "",
    val isEditState: Boolean = false,
    val moveToTrash: Boolean = false,
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
)
