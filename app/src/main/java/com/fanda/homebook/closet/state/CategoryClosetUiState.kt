package com.fanda.homebook.closet.state

import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.Category


data class CategoryClosetUiState(
    val categoryEntity: CategoryEntity = CategoryEntity(name = ""),
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
)
