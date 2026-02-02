package com.fanda.homebook.book.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.book.state.EditTransactionCategoryUiState
import com.fanda.homebook.data.transaction.TransactionEntity
import com.fanda.homebook.data.transaction.TransactionRepository
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionType
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.hjq.toast.Toaster
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

class EditTransactionCategoryViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(EditTransactionCategoryUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    // 当前选中的一级分类
    @OptIn(ExperimentalCoroutinesApi::class) val category: StateFlow<TransactionEntity?> = _uiState.map { it.categoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            transactionRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的二级分类
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<TransactionSubEntity?> = _uiState.map { it.subCategoryId }.distinctUntilChanged()              // 避免重复 ID 触发
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
    @OptIn(ExperimentalCoroutinesApi::class) val subCategories: StateFlow<List<TransactionSubEntity>?> = _uiState.map { it.categoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            transactionRepository.getSubItemsById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
        )

    fun updateCategory(
        categoryEntity: TransactionEntity?
    ) {
        categoryEntity?.let {
            _uiState.update {
                it.copy(categoryId = categoryEntity.id, subCategoryId = null)
            }
        }
    }

    fun toggleAddDialog(visible: Boolean) {
        _uiState.update {
            it.copy(addDialog = visible)
        }
    }

    fun toggleDeleteOrEditBottomSheet(visible: Boolean) {
        _uiState.update {
            it.copy(deleteOrEditDialog = visible)
        }
    }

    fun toggleEditDialog(visible: Boolean) {
        _uiState.update {
            it.copy(editDialog = visible)
        }
    }

    fun updateSubCategory(
        subCategoryEntity: TransactionSubEntity?
    ) {
        // 如果没有子分类，则将子分类ID设置为null
        _uiState.update {
            it.copy(subCategoryId = subCategoryEntity?.id)
        }
    }

    fun insertWithAutoOrder(name: String) {
        viewModelScope.launch {
            val subCategory = TransactionSubEntity(name = name, categoryId = _uiState.value.categoryId, type = TransactionType.CUSTOM.type)
            if (subCategory.name.isNotEmpty()) {
                if (transactionRepository.getSubItemByName(name, _uiState.value.categoryId) != null) {
                    Toaster.show("名称已存在")
                } else {
                    Toaster.show("添加成功")
                    toggleAddDialog(false)
                    transactionRepository.insertSubItemWithAutoOrder(subCategory)
                }
            } else {
                Toaster.show("请输入名称")
            }
        }
    }

    fun updateEntityDatabase(name: String) {
        viewModelScope.launch {
            if (subCategory.value != null) {
                transactionRepository.updateSubItem(subCategory.value!!.copy(name = name))
            }
        }
    }

    fun deleteEntityDatabase() {
        viewModelScope.launch {
            if (subCategory.value != null) {
                transactionRepository.deleteSubItem(subCategory.value!!)
            }
        }
    }

}