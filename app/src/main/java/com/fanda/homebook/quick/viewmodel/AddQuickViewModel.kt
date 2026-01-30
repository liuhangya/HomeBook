package com.fanda.homebook.quick.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.AddClosetUiState
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.CategoryWithSubCategories
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.transaction.TransactionEntity
import com.fanda.homebook.data.transaction.TransactionRepository
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionWithSubCategories
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.state.AddQuickUiState
import com.fanda.homebook.tools.TIMEOUT_MILLIS
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

class AddQuickViewModel(
    savedStateHandle: SavedStateHandle, private val transactionRepository: TransactionRepository
) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(AddQuickUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    // 当前选中的一级分类
    @OptIn(ExperimentalCoroutinesApi::class) val category: StateFlow<TransactionEntity?> = _uiState.map { it.quickEntity.categoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            transactionRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的二级分类
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<TransactionSubEntity?> = _uiState.map { it.quickEntity.subCategoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            transactionRepository.getSubItemById(id ?: -1)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 分类列表
    val categories: StateFlow<List<TransactionEntity>> = transactionRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 子分类列表
    @OptIn(ExperimentalCoroutinesApi::class) val subCategories: StateFlow<List<TransactionSubEntity>?> = _uiState.map { it.quickEntity.categoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            transactionRepository.getSubItemsById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
        )


    fun updateCategory(
        categoryEntity: TransactionEntity?
    ) {
        categoryEntity?.let {
            _uiState.update {
                it.copy(
                    quickEntity = it.quickEntity.copy(
                        categoryId = categoryEntity.id,
                    )
                )
            }
        }
    }

    fun updateSubCategory(
        subCategoryEntity: TransactionSubEntity?
    ) {
        // 如果没有子分类，则将子分类ID设置为null
        _uiState.update {
            it.copy(
                quickEntity = it.quickEntity.copy(subCategoryId = subCategoryEntity?.id)
            )
        }
    }

    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }


}