package com.fanda.homebook.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.CategoryUiState
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.CategoryRepository
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.hjq.toast.Toaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    private val _uiState = MutableStateFlow(CategoryUiState())

    val uiState = _uiState.asStateFlow()

    fun updateEntity(entity: CategoryEntity) {
        _uiState.update {
            it.copy(entity = entity)
        }
    }

    fun updateEntity(name: String) {
        _uiState.update {
            it.copy(entity = it.entity?.copy(name = name))
        }
    }

    fun toggleRenameOrDeleteBottomSheet(visible: Boolean) {
        _uiState.update {
            it.copy(renameOrDeleteBottomSheet = visible)
        }
    }

    fun toggleEditDialog(visible: Boolean) {
        _uiState.update {
            it.copy(editDialog = visible)
        }
    }

    fun toggleAddDialog(visible: Boolean) {
        _uiState.update {
            it.copy(addDialog = visible)
        }
    }

    fun updateEntityDatabase(name: String) {
        updateEntity(name)
        viewModelScope.launch {
            categoryRepository.updateItem(_uiState.value.entity!!)
        }
    }

    fun deleteEntityDatabase() {
        viewModelScope.launch {
            categoryRepository.deleteItem(_uiState.value.entity!!)
        }
    }

    fun updateSortOrders(items: MutableList<CategoryEntity>) {
        items.forEach {
            LogUtils.i("拖动后的数据： $it")
        }
        viewModelScope.launch {
            val updatedList = items.mapIndexed { index, item -> item.copy(sortOrder = index) }

            updatedList.forEach {
                LogUtils.i("插入数据库的数据： $it")
            }
            categoryRepository.updateItemsSortOrders(updatedList)
        }
    }


    fun updateAddEntity(name: String) {
        _uiState.update {
            it.copy(addEntity = it.addEntity.copy(name = name))
        }
    }

    fun insertWithAutoOrder(name: String) {
        viewModelScope.launch {
            updateAddEntity(name)
            if (_uiState.value.addEntity.name.isNotEmpty()) {
                if (categoryRepository.getItemByName(name) != null) {
                    Toaster.show("名称已存在")
                } else {
                    Toaster.show("添加成功")
                    toggleAddDialog(false)
                    categoryRepository.insertItemWithAutoOrder(_uiState.value.addEntity)
                }
            } else {
                Toaster.show("请输入名称")
            }
        }
    }
}