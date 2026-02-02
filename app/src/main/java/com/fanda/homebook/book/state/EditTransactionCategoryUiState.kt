package com.fanda.homebook.book.state

import com.fanda.homebook.entity.ShowBottomSheetType

data class EditTransactionCategoryUiState(
    val categoryId: Int = 1, val subCategoryId: Int? = null, val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
    val addDialog: Boolean = false,val deleteOrEditDialog: Boolean = false,
    val editDialog: Boolean = false,
)
