package com.fanda.homebook.data.color

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.ColorUiState
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ColorTypeViewModel(
    savedStateHandle: SavedStateHandle,
    private val colorTypeRepository: ColorTypeRepository
) : ViewModel() {

    // 颜色列表
    val colorTypes: StateFlow<List<ColorTypeEntity>> = colorTypeRepository.getColorTypes().stateIn(
        scope = viewModelScope,
        SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        emptyList()
    )

    private val _uiState = MutableStateFlow(ColorUiState())

    val uiState = _uiState.asStateFlow()

    fun updateColor(colorType: ColorTypeEntity) {
        _uiState.update {
            it.copy(colorType = colorType)
        }
    }

    fun updateColor(name: String) {
        _uiState.update {
            it.copy(colorType = it.colorType?.copy(name = name))
        }
    }

    fun toggleRenameOrDeleteBottomSheet(visible: Boolean) {
        _uiState.update {
            it.copy(renameOrDeleteBottomSheet = visible)
        }
    }

    fun toggleAddOrEditColorDialog(visible: Boolean) {
        _uiState.update {
            it.copy(editColorDialog = visible)
        }
    }

    fun updateColorTypeDatabase(name: String) {
        updateColor(name)
        viewModelScope.launch {
            colorTypeRepository.update(_uiState.value.colorType!!)
        }
    }

    fun deleteColorTypeDatabase() {
        viewModelScope.launch {
            colorTypeRepository.delete(_uiState.value.colorType!!)
        }
    }

    fun updateSortOrders(fromIndex: Int, toIndex: Int, items: MutableList<ColorTypeEntity>) {
        viewModelScope.launch {
            // 执行交换和更新
            val updatedList = performSwapAndUpdate(items, fromIndex, toIndex)
            colorTypeRepository.updateSortOrders(updatedList)
            LogUtils.d("updateSortOrders")
        }
    }

    /**
     * 执行交换并更新 sortOrder
     */
    private fun performSwapAndUpdate(
        list: MutableList<ColorTypeEntity>, fromIndex: Int, toIndex: Int
    ): List<ColorTypeEntity> {
        // 创建深拷贝，避免修改原始数据
        val updatedList = list.map { it.copy() }.toMutableList()

        // 获取交换的两个项目
        var itemFrom = updatedList[fromIndex]
        var itemTo = updatedList[toIndex]

        // 交换 sortOrder 值
        val tempSortOrder = itemFrom.sortOrder
        itemFrom = itemFrom.copy(sortOrder = itemTo.sortOrder)

        itemTo = itemTo.copy(sortOrder = tempSortOrder)

        // 交换列表位置（可选，取决于UI是否需要立即反映变化）
        updatedList[fromIndex] = itemTo
        updatedList[toIndex] = itemFrom

        return updatedList
    }

    fun updateAddColor(name: String) {
        _uiState.update {
            it.copy(addColorTypeEntity = it.addColorTypeEntity.copy(name = name))
        }
    }

    fun updateAddColor(color: Long) {
        _uiState.update {
            it.copy(addColorTypeEntity = it.addColorTypeEntity.copy(color = color))
        }
    }

    fun insertWithAutoOrder(result:(success:Boolean) -> Unit) {
        if (_uiState.value.addColorTypeEntity.name.isNotEmpty()) {
            viewModelScope.launch {
                colorTypeRepository.insertWithAutoOrder(_uiState.value.addColorTypeEntity)
                result(true)
            }
        }else {
            LogUtils.d("请输入颜色名称")
            result(false)
        }
    }
}