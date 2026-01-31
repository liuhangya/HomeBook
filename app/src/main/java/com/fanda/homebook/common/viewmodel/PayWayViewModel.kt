package com.fanda.homebook.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.data.pay.PayWayEntity
import com.fanda.homebook.data.pay.PayWayRepository
import com.fanda.homebook.quick.state.PayWayUiState
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.hjq.toast.Toaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PayWayViewModel(private val payWayRepository: PayWayRepository) : ViewModel() {

    val payWays: StateFlow<List<PayWayEntity>> = payWayRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    private val _uiState = MutableStateFlow(PayWayUiState())

    val uiState = _uiState.asStateFlow()

    fun updateEntity(entity: PayWayEntity) {
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
            payWayRepository.update(_uiState.value.entity!!)
        }
    }

    fun deleteEntityDatabase() {
        viewModelScope.launch {
            payWayRepository.delete(_uiState.value.entity!!)
        }
    }

    fun updateSortOrders(items: MutableList<PayWayEntity>) {
        viewModelScope.launch {
//            items.forEachIndexed { index, colorType ->
//                LogUtils.d("index: $index, colorType: $colorType")
//            }
            val updatedList = items.mapIndexed { index, item -> item.copy(sortOrder = index) }
            payWayRepository.updateSortOrders(updatedList)
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
                if (payWayRepository.getItemByName(name) != null) {
                    Toaster.show("名称已存在")
                } else {
                    Toaster.show("添加成功")
                    toggleAddDialog(false)
                    payWayRepository.insertWithAutoOrder(_uiState.value.addEntity)
                }
            } else {
                Toaster.show("请输入名称")
            }
        }
    }
}