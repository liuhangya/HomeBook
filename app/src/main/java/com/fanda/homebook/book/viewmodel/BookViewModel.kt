package com.fanda.homebook.book.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.book.state.BookUiState
import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.data.book.BookRepository
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.fanda.homebook.tools.UserCache
import com.fanda.homebook.tools.getLastDayTimestamp
import com.fanda.homebook.tools.millisToLocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookViewModel(private val bookRepository: BookRepository) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(BookUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    init {
        val (year, month) = millisToLocalDate(_uiState.value.queryDate)
        _uiState.update { it.copy(year = year, month = month) }
    }

    // 当前选中账本
    @OptIn(ExperimentalCoroutinesApi::class) val curSelectedBook: StateFlow<BookEntity?> = _uiState.map { it.curSelectBookId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            UserCache.bookId = id
            bookRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 账本列表
    val books: StateFlow<List<BookEntity>> = bookRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    fun updateBookEntityDatabase(bookEntity: BookEntity?) {
        bookEntity?.let {
            viewModelScope.launch {
                bookRepository.update(bookEntity)
            }
        }
    }

    fun insertBookEntityDatabase(name: String) {
        viewModelScope.launch {
            bookRepository.insert(BookEntity(name = name))
        }
    }

    fun deleteBookEntityDatabase(bookEntity: BookEntity?) {
        bookEntity?.let {
            viewModelScope.launch {
                bookRepository.delete(bookEntity)
            }
        }
    }


    fun updateEditBookEntity(bookEntity: BookEntity?) {
        _uiState.value = _uiState.value.copy(curEditBookEntity = bookEntity)
    }

    fun updateEditBookStatus(isEdit: Boolean) {
        _uiState.update { it.copy(isEditBook = isEdit) }
    }

    fun updateQueryDate(year: Int, month: Int) {
        _uiState.update { it.copy(queryDate = getLastDayTimestamp(year, month), year = year, month = month) }
    }

    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    fun updateSelectBook(bookId: Int) {
        _uiState.update { it.copy(curSelectBookId = bookId) }
    }

    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }
}