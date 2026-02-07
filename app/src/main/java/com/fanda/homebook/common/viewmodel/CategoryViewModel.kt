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

/**
 * 分类（Category）相关的ViewModel
 * 负责管理分类数据的业务逻辑和UI状态
 * @param categoryRepository 分类数据仓库，用于数据持久化操作
 */
class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    /**
     * 分类列表的StateFlow
     * 自动从数据库获取数据，并在订阅时保持最新状态
     * 使用TIMEOUT_MILLIS配置超时时间，避免资源浪费
     */
    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getItems().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    /**
     * 私有的UI状态MutableStateFlow
     * 用于管理分类相关的UI状态（对话框、底部弹窗、编辑实体等）
     */
    private val _uiState = MutableStateFlow(CategoryUiState())

    /**
     * 公开的UI状态StateFlow
     * 对外提供只读的UI状态访问
     */
    val uiState = _uiState.asStateFlow()

    /**
     * 更新当前操作的分类实体
     * @param entity 要更新的分类实体对象
     */
    fun updateEntity(entity: CategoryEntity) {
        _uiState.update {
            it.copy(entity = entity)
        }
    }

    /**
     * 更新当前分类实体的名称
     * @param name 新的分类名称
     */
    fun updateEntity(name: String) {
        _uiState.update {
            it.copy(entity = it.entity?.copy(name = name))
        }
    }

    /**
     * 切换重命名/删除底部弹窗的显示状态
     * @param visible true显示，false隐藏
     */
    fun toggleRenameOrDeleteBottomSheet(visible: Boolean) {
        _uiState.update {
            it.copy(renameOrDeleteBottomSheet = visible)
        }
    }

    /**
     * 切换编辑对话框的显示状态
     * @param visible true显示，false隐藏
     */
    fun toggleEditDialog(visible: Boolean) {
        _uiState.update {
            it.copy(editDialog = visible)
        }
    }

    /**
     * 切换添加对话框的显示状态
     * @param visible true显示，false隐藏
     */
    fun toggleAddDialog(visible: Boolean) {
        _uiState.update {
            it.copy(addDialog = visible)
        }
    }

    /**
     * 更新数据库中的分类实体
     * 先更新本地UI状态，再异步更新数据库
     * @param name 要更新的分类名称
     */
    fun updateEntityDatabase(name: String) {
        updateEntity(name)
        viewModelScope.launch {
            // 确保实体不为空后更新数据库
            categoryRepository.updateItem(_uiState.value.entity!!)
        }
    }

    /**
     * 从数据库中删除当前分类实体
     */
    fun deleteEntityDatabase() {
        viewModelScope.launch {
            categoryRepository.deleteItem(_uiState.value.entity!!)
        }
    }

    /**
     * 更新分类的排序顺序
     * 根据拖动后的列表顺序重新计算sortOrder并更新数据库
     * @param items 拖动后的分类实体列表
     */
    fun updateSortOrders(items: MutableList<CategoryEntity>) {
        // 日志记录拖动后的数据
        items.forEach {
            LogUtils.i("拖动后的数据： $it")
        }

        viewModelScope.launch {
            // 重新计算排序序号（从0开始）
            val updatedList = items.mapIndexed { index, item -> item.copy(sortOrder = index) }

            // 日志记录要插入数据库的数据
            updatedList.forEach {
                LogUtils.i("插入数据库的数据： $it")
            }

            // 批量更新排序顺序到数据库
            categoryRepository.updateItemsSortOrders(updatedList)
        }
    }

    /**
     * 更新要添加的分类实体名称
     * @param name 要添加的分类名称
     */
    fun updateAddEntity(name: String) {
        _uiState.update {
            it.copy(addEntity = it.addEntity.copy(name = name))
        }
    }

    /**
     * 添加新分类（带自动排序）
     * 会自动为新分类分配排序序号
     * @param name 要添加的分类名称
     */
    fun insertWithAutoOrder(name: String) {
        viewModelScope.launch {
            // 更新要添加的实体名称
            updateAddEntity(name)

            // 验证名称是否有效
            if (_uiState.value.addEntity.name.isNotEmpty()) {
                // 检查名称是否已存在
                if (categoryRepository.getItemByName(name) != null) {
                    Toaster.show("名称已存在")
                } else {
                    Toaster.show("添加成功")
                    // 关闭添加对话框
                    toggleAddDialog(false)
                    // 插入新分类到数据库（会自动计算排序序号）
                    categoryRepository.insertItemWithAutoOrder(_uiState.value.addEntity)
                }
            } else {
                Toaster.show("请输入名称")
            }
        }
    }
}