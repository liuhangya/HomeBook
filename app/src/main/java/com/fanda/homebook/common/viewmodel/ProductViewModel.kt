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

/**
 * 品牌/产品管理ViewModel
 * 负责管理品牌/产品的增删改查和排序操作
 *
 * @param productRepository 品牌/产品数据仓库
 */
class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {

    // 所有品牌/产品列表状态流
    val products: StateFlow<List<ProductEntity>> = productRepository.getItems().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    // 私有可变状态流，用于保存UI状态
    private val _uiState = MutableStateFlow(ProductUiState())

    // 公开只读状态流，用于UI层观察状态变化
    val uiState = _uiState.asStateFlow()

    /**
     * 更新当前正在操作（编辑/删除）的品牌/产品实体
     *
     * @param entity 要更新的品牌/产品实体
     */
    fun updateEntity(entity: ProductEntity) {
        _uiState.update {
            it.copy(entity = entity)
        }
    }

    /**
     * 更新当前正在操作的品牌/产品的名称
     *
     * @param name 新的品牌/产品名称
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
     * 更新品牌/产品实体到数据库（重命名操作）
     *
     * @param name 新的品牌/产品名称
     */
    fun updateEntityDatabase(name: String) {
        updateEntity(name)  // 先更新本地状态
        viewModelScope.launch {
            // 然后更新到数据库
            productRepository.update(_uiState.value.entity!!)
        }
    }

    /**
     * 删除品牌/产品实体到数据库
     */
    fun deleteEntityDatabase() {
        viewModelScope.launch {
            productRepository.delete(_uiState.value.entity!!)
        }
    }

    /**
     * 更新品牌/产品的排序顺序
     * 用于拖拽排序后保存新的顺序
     *
     * @param items 重新排序后的品牌/产品列表
     */
    fun updateSortOrders(items: MutableList<ProductEntity>) {
        viewModelScope.launch {
            // 根据新的顺序重新设置sortOrder字段（从0开始）
            val updatedList = items.mapIndexed { index, item ->
                item.copy(sortOrder = index)
            }
            // 批量更新排序顺序到数据库
            productRepository.updateSortOrders(updatedList)
        }
    }

    /**
     * 更新待添加的品牌/产品实体名称
     *
     * @param name 待添加的品牌/产品名称
     */
    fun updateAddEntity(name: String) {
        _uiState.update {
            it.copy(addEntity = it.addEntity.copy(name = name))
        }
    }

    /**
     * 插入新的品牌/产品到数据库（自动处理排序顺序）
     *
     * @param name 要添加的品牌/产品名称
     */
    fun insertWithAutoOrder(name: String) {
        viewModelScope.launch {
            // 先更新待添加实体的名称
            updateAddEntity(name)

            if (_uiState.value.addEntity.name.isNotEmpty()) {
                // 检查名称是否已存在
                if (productRepository.getItemByName(name) != null) {
                    Toaster.show("名称已存在")
                } else {
                    // 名称可用，执行添加操作
                    Toaster.show("添加成功")
                    toggleAddDialog(false)  // 关闭添加对话框
                    productRepository.insertWithAutoOrder(_uiState.value.addEntity)
                }
            } else {
                // 名称为空提示
                Toaster.show("请输入名称")
            }
        }
    }
}