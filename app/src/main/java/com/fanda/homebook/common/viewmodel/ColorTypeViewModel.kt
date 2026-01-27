package com.fanda.homebook.common.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.ColorUiState
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.color.ColorTypeRepository
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

class ColorTypeViewModel(
    savedStateHandle: SavedStateHandle, private val colorTypeRepository: ColorTypeRepository
) : ViewModel() {

    private val colorId: Int = savedStateHandle["colorId"] ?: -1

    init {
        viewModelScope.launch {
            if (colorId != -1) {
                colorTypeRepository.getItemById(colorId).collect { colorType ->
                    _uiState.update {
                        it.copy(entity = colorType)
                    }
                }
            }
        }
    }

    // 颜色列表
    val colorTypes: StateFlow<List<ColorTypeEntity>> = colorTypeRepository.getItems().stateIn(
        scope = viewModelScope,
        SharingStarted.Companion.WhileSubscribed(TIMEOUT_MILLIS),
        emptyList()
    )

    private val _uiState = MutableStateFlow(ColorUiState())

    val uiState = _uiState.asStateFlow()

    fun updateEntity(colorType: ColorTypeEntity) {
        _uiState.update {
            it.copy(entity = colorType)
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

    fun updateEntityDatabase(result: (success: Boolean) -> Unit) {
        if (_uiState.value.entity != null) {
            if (_uiState.value.entity!!.name.isNotEmpty()) {
                viewModelScope.launch {
                    val existItem = colorTypeRepository.getItemByName(_uiState.value.entity!!.name)
                    if (existItem != null && existItem.id != _uiState.value.entity!!.id) {
                        Toaster.show("名称已存在")
                        result(false)
                        return@launch
                    }
                    colorTypeRepository.update(_uiState.value.entity!!)
                    result(true)
                    Toaster.show("编辑成功")
                }
            } else {
                Toaster.show("请输入名称")
                result(false)
            }
        } else {
            Toaster.show("编辑异常")
            result(false)
        }
    }

    fun deleteEntityDatabase() {
        viewModelScope.launch {
            colorTypeRepository.delete(_uiState.value.entity!!)
        }
    }

    fun updateSortOrders(items: MutableList<ColorTypeEntity>) {
        viewModelScope.launch {
//            items.forEachIndexed { index, colorType ->
//                LogUtils.d("index: $index, colorType: $colorType")
//            }
            val updatedList = items.mapIndexed { index, item -> item.copy(sortOrder = index) }
            colorTypeRepository.updateSortOrders(updatedList)
        }
    }


    fun updateAddEntity(name: String) {
        _uiState.update {
            it.copy(addEntity = it.addEntity.copy(name = name))
        }
    }

    fun updateEntity(color: Long) {
        _uiState.update {
            it.copy(entity = it.entity?.copy(color = color))
        }
    }

    fun updateAddEntity(color: Long) {
        _uiState.update {
            it.copy(addEntity = it.addEntity.copy(color = color))
        }
    }

    fun insertWithAutoOrder(result: (success: Boolean) -> Unit) {
        if (_uiState.value.addEntity.name.isNotEmpty()) {
            viewModelScope.launch {
                if (colorTypeRepository.getItemByName(_uiState.value.addEntity.name) != null) {
                    Toaster.show("名称已存在")
                    result(false)
                    return@launch
                }
                colorTypeRepository.insertWithAutoOrder(_uiState.value.addEntity)
                result(true)
                Toaster.show("添加成功")
            }
        } else {
            Toaster.show("请输入名称")
            result(false)
        }
    }
}