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

/**
 * 编辑交易分类ViewModel
 * 管理交易分类编辑页面的数据状态和业务逻辑
 *
 * @param transactionRepository 交易分类数据仓库
 */
class EditTransactionCategoryViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(EditTransactionCategoryUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    // 当前选中的一级分类（主分类）
    @OptIn(ExperimentalCoroutinesApi::class) val category: StateFlow<TransactionEntity?> = _uiState.map { it.categoryId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->     // 每当上游（categoryId）变化，就取消之前的流，启动新的
            transactionRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 当前选中的二级分类（子分类）
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<TransactionSubEntity?> = _uiState.map { it.subCategoryId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->     // 每当上游（subCategoryId）变化，就取消之前的流，启动新的
            transactionRepository.getSubItemById(id ?: -1)  // 如果为null则传-1
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 一级分类列表（主分类列表）
    val categories: StateFlow<List<TransactionEntity>> = transactionRepository.getItems().stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 二级分类列表（子分类列表，根据选中的主分类动态变化）
    @OptIn(ExperimentalCoroutinesApi::class) val subCategories: StateFlow<List<TransactionSubEntity>?> = _uiState.map { it.categoryId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->     // 每当上游（categoryId）变化，就取消之前的流，启动新的
            transactionRepository.getSubItemsById(id)  // 根据主分类ID获取子分类
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
        )

    /**
     * 更新选中的主分类
     *
     * @param categoryEntity 主分类实体
     */
    fun updateCategory(categoryEntity: TransactionEntity?) {
        categoryEntity?.let {
            _uiState.update {
                it.copy(
                    categoryId = categoryEntity.id,  // 更新主分类ID
                    subCategoryId = null             // 清空子分类ID
                )
            }
        }
    }

    /**
     * 切换添加分类对话框的显示状态
     *
     * @param visible 是否显示
     */
    fun toggleAddDialog(visible: Boolean) {
        _uiState.update {
            it.copy(addDialog = visible)
        }
    }

    /**
     * 切换删除或编辑底部弹窗的显示状态
     *
     * @param visible 是否显示
     */
    fun toggleDeleteOrEditBottomSheet(visible: Boolean) {
        _uiState.update {
            it.copy(deleteOrEditDialog = visible)
        }
    }

    /**
     * 切换编辑分类对话框的显示状态
     *
     * @param visible 是否显示
     */
    fun toggleEditDialog(visible: Boolean) {
        _uiState.update {
            it.copy(editDialog = visible)
        }
    }

    /**
     * 更新选中的子分类
     *
     * @param subCategoryEntity 子分类实体
     */
    fun updateSubCategory(subCategoryEntity: TransactionSubEntity?) {
        _uiState.update {
            it.copy(subCategoryId = subCategoryEntity?.id)
        }
    }

    /**
     * 插入新的子分类（自动计算排序顺序）
     *
     * @param name 分类名称
     */
    fun insertWithAutoOrder(name: String) {
        viewModelScope.launch {
            // 创建新的子分类实体
            val subCategory = TransactionSubEntity(
                name = name, categoryId = _uiState.value.categoryId,  // 使用当前选中的主分类ID
                type = TransactionType.CUSTOM.type       // 设置为自定义类型
            )

            // 验证名称是否为空
            if (subCategory.name.isNotEmpty()) {
                // 检查名称是否已存在
                if (transactionRepository.getSubItemByName(name, _uiState.value.categoryId) != null) {
                    Toaster.show("名称已存在")
                } else {
                    // 插入新的子分类
                    Toaster.show("添加成功")
                    toggleAddDialog(false)  // 关闭添加对话框
                    transactionRepository.insertSubItemWithAutoOrder(subCategory)
                }
            } else {
                Toaster.show("请输入名称")
            }
        }
    }

    /**
     * 更新子分类实体到数据库
     *
     * @param name 新的分类名称
     */
    fun updateEntityDatabase(name: String) {
        viewModelScope.launch {
            // 检查当前是否有选中的子分类
            if (subCategory.value != null) {
                // 创建更新后的子分类实体
                val updatedEntity = subCategory.value!!.copy(name = name)
                transactionRepository.updateSubItem(updatedEntity)
            }
        }
    }

    /**
     * 从数据库删除子分类实体
     */
    fun deleteEntityDatabase() {
        viewModelScope.launch {
            // 检查当前是否有选中的子分类
            if (subCategory.value != null) {
                transactionRepository.deleteSubItem(subCategory.value!!)
            }
        }
    }
}