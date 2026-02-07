package com.fanda.homebook.common.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.SubCategoryUiState
import com.fanda.homebook.data.category.CategoryRepository
import com.fanda.homebook.data.category.SubCategoryEntity
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
 * 子分类管理ViewModel
 * 负责管理子分类（二级分类）的增删改查和排序操作
 *
 * @param savedStateHandle 用于保存和恢复状态的状态句柄，包含父分类ID和名称
 * @param categoryRepository 分类数据仓库
 */
class SubCategoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // 从SavedStateHandle中获取传入的父分类ID和名称参数
    private val categoryId: Int = savedStateHandle["categoryId"] ?: 1
    private val categoryName: String = savedStateHandle["categoryName"] ?: ""

    // 当前父分类下的所有子分类列表状态流
    val categories: StateFlow<List<SubCategoryEntity>> =
        categoryRepository.getSubItemsById(categoryId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    // 私有可变状态流，用于保存UI状态
    private val _uiState = MutableStateFlow(SubCategoryUiState())

    // 公开只读状态流，用于UI层观察状态变化
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 初始化时设置父分类ID和名称
            _uiState.update {
                it.copy(
                    addEntity = it.addEntity.copy(categoryId = categoryId),
                    categoryName = categoryName
                )
            }
        }
    }

    /**
     * 更新当前正在操作（编辑/删除）的子分类实体
     *
     * @param entity 要更新的子分类实体
     */
    fun updateEntity(entity: SubCategoryEntity) {
        _uiState.update {
            it.copy(entity = entity)
        }
    }

    /**
     * 更新当前正在操作的子分类的名称
     *
     * @param name 新的子分类名称
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
     * 切换编辑对话框的显示状态
     *
     * @param visible 是否显示编辑对话框
     */
    fun toggleEditDialog(visible: Boolean) {
        _uiState.update {
            it.copy(editDialog = visible)
        }
    }

    /**
     * 切换添加对话框的显示状态
     *
     * @param visible 是否显示添加对话框
     */
    fun toggleAddDialog(visible: Boolean) {
        _uiState.update {
            it.copy(addDialog = visible)
        }
    }

    /**
     * 更新子分类实体到数据库（重命名操作）
     *
     * @param name 新的子分类名称
     */
    fun updateEntityDatabase(name: String) {
        updateEntity(name)  // 先更新本地状态
        viewModelScope.launch {
            // 然后更新到数据库
            categoryRepository.updateSubItem(_uiState.value.entity!!)
        }
    }

    /**
     * 删除子分类实体到数据库
     */
    fun deleteEntityDatabase() {
        viewModelScope.launch {
            categoryRepository.deleteSubItem(_uiState.value.entity!!)
        }
    }

    /**
     * 更新子分类的排序顺序
     * 用于拖拽排序后保存新的顺序
     *
     * @param items 重新排序后的子分类列表
     */
    fun updateSortOrders(items: MutableList<SubCategoryEntity>) {
        // 调试日志：打印拖动后的数据
        items.forEach {
            LogUtils.i("拖动后的数据： $it")
        }

        viewModelScope.launch {
            // 根据新的顺序重新设置sortOrder字段（从0开始）
            val updatedList = items.mapIndexed { index, item ->
                item.copy(sortOrder = index)
            }

            // 调试日志：打印将要插入数据库的数据
            updatedList.forEach {
                LogUtils.i("插入数据库的数据： $it")
            }

            // 批量更新排序顺序到数据库
            categoryRepository.updateSubItemsSortOrders(updatedList)
        }
    }

    /**
     * 更新待添加的子分类实体名称
     *
     * @param name 待添加的子分类名称
     */
    fun updateAddEntity(name: String) {
        _uiState.update {
            it.copy(addEntity = it.addEntity.copy(name = name))
        }
    }

    /**
     * 插入新的子分类到数据库（自动处理排序顺序）
     *
     * @param name 要添加的子分类名称
     */
    fun insertWithAutoOrder(name: String) {
        viewModelScope.launch {
            // 先更新待添加实体的名称
            updateAddEntity(name)

            if (_uiState.value.addEntity.name.isNotEmpty()) {
                // 检查名称是否已存在
                if (categoryRepository.getSubItemByName(name) != null) {
                    Toaster.show("名称已存在")
                } else {
                    // 名称可用，执行添加操作
                    Toaster.show("添加成功")
                    toggleAddDialog(false)  // 关闭添加对话框
                    categoryRepository.insertSubItemWithAutoOrder(_uiState.value.addEntity)
                }
            } else {
                // 名称为空提示
                Toaster.show("请输入名称")
            }
        }
    }
}