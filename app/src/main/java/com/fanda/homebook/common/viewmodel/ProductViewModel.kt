package com.fanda.homebook.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.ProductUiState
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.product.ProductRepository
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

class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {

    val products: StateFlow<List<ProductEntity>> = productRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    private val _uiState = MutableStateFlow(ProductUiState())

    val uiState = _uiState.asStateFlow()

    fun updateEntity(entity: ProductEntity) {
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
            productRepository.update(_uiState.value.entity!!)
        }
    }

    fun deleteEntityDatabase() {
        viewModelScope.launch {
            productRepository.delete(_uiState.value.entity!!)
        }
    }

    fun updateSortOrders(items: MutableList<ProductEntity>) {
        viewModelScope.launch {
//            items.forEachIndexed { index, colorType ->
//                LogUtils.d("index: $index, colorType: $colorType")
//            }
            val updatedList = items.mapIndexed { index, item -> item.copy(sortOrder = index) }
            productRepository.updateSortOrders(updatedList)
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
                if (productRepository.getItemByName(name) != null) {
                    Toaster.show("名称已存在")
                } else {
                    Toaster.show("添加成功")
                    toggleAddDialog(false)
                    productRepository.insertWithAutoOrder(_uiState.value.addEntity)
                }
            } else {
                Toaster.show("请输入名称")
            }
        }
    }
}