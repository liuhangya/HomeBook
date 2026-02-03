package com.fanda.homebook.book.state

import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.entity.ShowBottomSheetType

data class BookUiState(
    val isEditBook: Boolean = false,
    val curSelectBookId: Int = 1,
    val curEditBookEntity: BookEntity? = null,
    val showEditBookDialog: Boolean = false,
    val year : Int = 2026,
    val month : Int = 1,
    val refresh: Boolean = false,
    val categoryId: Int? = null,
    val subCategoryId: Int? = null,
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE
)