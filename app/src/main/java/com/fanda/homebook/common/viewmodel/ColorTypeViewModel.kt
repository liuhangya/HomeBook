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

/**
 * 颜色类型管理ViewModel
 * 负责管理颜色类型的增删改查和排序操作
 *
 * @param savedStateHandle 用于保存和恢复状态的状态句柄，包含颜色ID
 * @param colorTypeRepository 颜色类型数据仓库
 */
class ColorTypeViewModel(
    savedStateHandle: SavedStateHandle,
    private val colorTypeRepository: ColorTypeRepository
) : ViewModel() {

    // 从SavedStateHandle中获取传入的颜色ID参数
    private val colorId: Int = savedStateHandle["colorId"] ?: -1

    init {
        viewModelScope.launch {
            // 如果传入了颜色ID，则加载对应的颜色数据
            if (colorId != -1) {
                colorTypeRepository.getItemById(colorId).collect { colorType ->
                    _uiState.update {
                        it.copy(entity = colorType)
                    }
                }
            }
        }
    }

    // 所有颜色类型列表状态流
    val colorTypes: StateFlow<List<ColorTypeEntity>> = colorTypeRepository.getItems().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    // 私有可变状态流，用于保存UI状态
    private val _uiState = MutableStateFlow(ColorUiState())

    // 公开只读状态流，用于UI层观察状态变化
    val uiState = _uiState.asStateFlow()

    /**
     * 更新当前正在操作（编辑/删除）的颜色类型实体
     *
     * @param colorType 要更新的颜色类型实体
     */
    fun updateEntity(colorType: ColorTypeEntity) {
        _uiState.update {
            it.copy(entity = colorType)
        }
    }

    /**
     * 更新当前正在操作的颜色类型的名称
     *
     * @param name 新的颜色类型名称
     */
    fun updateEntity(name: String) {
        _uiState.update {
            it.copy(entity = it.entity?.copy(name = name))
        }
    }

    /**
     * 切换重命名/删除底部弹窗的显示状态
     *
     * @param visible 是否显示弹窗
     */
    fun toggleRenameOrDeleteBottomSheet(visible: Boolean) {
        _uiState.update {
            it.copy(renameOrDeleteBottomSheet = visible)
        }
    }

    /**
     * 更新颜色类型实体到数据库（编辑操作）
     *
     * @param result 操作结果回调函数，参数为是否成功
     */
    fun updateEntityDatabase(result: (success: Boolean) -> Unit) {
        if (_uiState.value.entity != null) {
            if (_uiState.value.entity!!.name.isNotEmpty()) {
                viewModelScope.launch {
                    // 检查名称是否已存在（排除自身）
                    val existItem = colorTypeRepository.getItemByName(_uiState.value.entity!!.name)
                    if (existItem != null && existItem.id != _uiState.value.entity!!.id) {
                        Toaster.show("名称已存在")
                        result(false)
                        return@launch
                    }
                    // 名称可用，执行更新操作
                    colorTypeRepository.update(_uiState.value.entity!!)
                    result(true)
                    Toaster.show("编辑成功")
                }
            } else {
                // 名称为空提示
                Toaster.show("请输入名称")
                result(false)
            }
        } else {
            // 实体为空异常
            Toaster.show("编辑异常")
            result(false)
        }
    }

    /**
     * 删除颜色类型实体到数据库
     */
    fun deleteEntityDatabase() {
        viewModelScope.launch {
            colorTypeRepository.delete(_uiState.value.entity!!)
        }
    }

    /**
     * 更新颜色类型的排序顺序
     * 用于拖拽排序后保存新的顺序
     *
     * @param items 重新排序后的颜色类型列表
     */
    fun updateSortOrders(items: MutableList<ColorTypeEntity>) {
        viewModelScope.launch {
            // 根据新的顺序重新设置sortOrder字段（从0开始）
            val updatedList = items.mapIndexed { index, item ->
                item.copy(sortOrder = index)
            }
            // 批量更新排序顺序到数据库
            colorTypeRepository.updateSortOrders(updatedList)
        }
    }

    /**
     * 更新待添加的颜色类型实体名称
     *
     * @param name 待添加的颜色类型名称
     */
    fun updateAddEntity(name: String) {
        _uiState.update {
            it.copy(addEntity = it.addEntity.copy(name = name))
        }
    }

    /**
     * 更新当前正在操作的颜色类型的颜色值
     *
     * @param color 新的颜色值（Long类型表示ARGB颜色）
     */
    fun updateEntity(color: Long) {
        _uiState.update {
            it.copy(entity = it.entity?.copy(color = color))
        }
    }

    /**
     * 更新待添加的颜色类型实体的颜色值
     *
     * @param color 待添加的颜色值（Long类型表示ARGB颜色）
     */
    fun updateAddEntity(color: Long) {
        _uiState.update {
            it.copy(addEntity = it.addEntity.copy(color = color))
        }
    }

    /**
     * 插入新的颜色类型到数据库（自动处理排序顺序）
     *
     * @param result 操作结果回调函数，参数为是否成功
     */
    fun insertWithAutoOrder(result: (success: Boolean) -> Unit) {
        if (_uiState.value.addEntity.name.isNotEmpty()) {
            viewModelScope.launch {
                // 检查名称是否已存在
                if (colorTypeRepository.getItemByName(_uiState.value.addEntity.name) != null) {
                    Toaster.show("名称已存在")
                    result(false)
                    return@launch
                }
                // 名称可用，执行添加操作
                colorTypeRepository.insertWithAutoOrder(_uiState.value.addEntity)
                result(true)
                Toaster.show("添加成功")
            }
        } else {
            // 名称为空提示
            Toaster.show("请输入名称")
            result(false)
        }
    }
}