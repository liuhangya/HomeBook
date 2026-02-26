package com.fanda.homebook.closet.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.CategoryDetailClosetUiState
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.CategoryRepository
import com.fanda.homebook.data.category.CategoryWithSubCategories
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.closet.AddClosetEntity
import com.fanda.homebook.data.closet.ClosetDetailGridItem
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.fanda.homebook.tools.UserCache
import com.hjq.toast.Toaster
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 分类详情衣橱视图模型
 *
 * 负责管理分类详情页面的业务逻辑和状态，支持多选操作
 */
class CategoryDetailClosetViewModel(
    savedStateHandle: SavedStateHandle, private val closetRepository: ClosetRepository, private val categoryRepository: CategoryRepository
) : ViewModel() {

    // 从保存状态中获取参数
    private val categoryId = savedStateHandle.get<Int>("categoryId") ?: -1
    private val subCategoryId = savedStateHandle.get<Int>("subCategoryId") ?: -1
    private val categoryName = savedStateHandle.get<String>("categoryName") ?: ""
    private val moveToTrash = savedStateHandle.get<Boolean>("moveToTrash") ?: false

    // 私有可变状态流
    private val _uiState = MutableStateFlow(CategoryDetailClosetUiState())

    // 公开只读状态流
    val uiState = _uiState.asStateFlow()

    init {
        // 初始化UI状态
        _uiState.update {
            it.copy(
                categoryId = categoryId, categoryName = categoryName, subCategoryId = subCategoryId, moveToTrash = moveToTrash
            )
        }
    }

    // 分类列表（包含子分类）
    val categories: StateFlow<List<CategoryWithSubCategories>> = categoryRepository.getAllItemsWithSub().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 当前父分类下的所有子分类列表状态流
    val subCategories: StateFlow<List<SubCategoryEntity>> = categoryRepository.getSubItemsById(categoryId).stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )


    // 原始衣橱数据流
    @OptIn(ExperimentalCoroutinesApi::class) private val rawClosets = _uiState.map {
        it.categoryId
    }.flatMapLatest { categoryId ->
        // 根据不同的查询条件获取不同的数据
        when {
            _uiState.value.moveToTrash -> {
                // 垃圾桶模式：获取已删除的衣橱物品
                closetRepository.getClosets(UserCache.ownerId, moveToTrash = true)
            }

            categoryId <= 0 && subCategoryId <= 0 -> {
                // 未分类模式：获取未分类的衣橱物品
                closetRepository.getNoCategoryClosets(UserCache.ownerId)
            }

            categoryId == -1 -> {
                // 二级分类模式：根据二级分类ID获取
                closetRepository.getClosetsBySubCategory(
                    UserCache.ownerId, _uiState.value.subCategoryId
                )
            }

            else -> {
                // 一级分类模式：根据一级分类ID获取
                closetRepository.getClosetsByCategory(
                    UserCache.ownerId, _uiState.value.categoryId
                )
            }
        }
    }.map { items ->
        // 转换为网格项格式
        items.map { ClosetDetailGridItem(addClosetEntity = it) }
    }.catch { e ->
        // 错误处理
        LogUtils.e("查询失败: ${e.message}")
        emit(emptyList())
    }

    // 选中的衣橱ID集合
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow()

    // 合并选中状态的数据流
    val closets = combine(
        rawClosets, _selectedIds
    ) { items, selectedIds ->
        // 更新每个项的选中状态
        items.map { item ->
            item.copyWithSelected(selectedIds.contains(item.addClosetEntity.closet.id))
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 获取选中的衣橱实体
    val selectedItems: StateFlow<List<AddClosetEntity>> = combine(
        closets, _selectedIds
    ) { items, selectedIds ->
        items.filter { it.isSelected }.map { it.addClosetEntity }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 选中项数量
    val selectedCount: StateFlow<Int> = _selectedIds.map { it.size }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = 0
    )

    /**
     * 更新底部弹窗类型（带选中项验证）
     *
     * @param type 弹窗类型
     */
    fun updateSheetType(type: ShowBottomSheetType) {
        LogUtils.i("当前选中的物品数量： ${selectedIds.value.size}")
        // 在编辑模式下，如果没有选中任何物品，则提示用户
        if (uiState.value.isEditState && selectedIds.value.isEmpty()) {
            Toaster.show("请选择物品")
        } else {
            _uiState.update { it.copy(sheetType = type) }
        }
    }

    /**
     * 检查是否显示指定类型的底部弹窗
     *
     * @param type 弹窗类型
     * @return 是否显示
     */
    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    /**
     * 关闭底部弹窗
     */
    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    /**
     * 切换编辑状态
     *
     * @param show 是否显示编辑状态
     */
    fun toggleEditState(show: Boolean) {
        _uiState.update {
            it.copy(isEditState = show)
        }
    }

    /**
     * 复制选中的衣橱实体到数据库
     */
    fun copyEntityDatabase() {
        viewModelScope.launch {
            // 复制选中的衣橱物品（ID重置为0，让数据库自增）
            closetRepository.insertAll(
                selectedItems.value.map { it.closet.copy(id = 0) })
            Toaster.show("复制成功")
            clearAllSelection()
        }
    }

    /**
     * 删除选中的衣橱实体从数据库
     */
    fun deleteEntityDatabase() {
        viewModelScope.launch {
            closetRepository.deleteAll(selectedItems.value.map { it.closet })
            Toaster.show("删除成功")
            clearAllSelection()
        }
    }

    /**
     * 更新选中的衣橱实体的分类
     *
     * @param category 一级分类实体
     * @param subCategory 二级分类实体
     */
    fun updateEntityDatabase(category: CategoryEntity?, subCategory: SubCategoryEntity?) {
        if (category != null) {
            viewModelScope.launch {
                // 批量更新分类信息
                closetRepository.updateAll(
                    selectedItems.value.map {
                        it.closet.copy(
                            categoryId = category.id, subCategoryId = subCategory?.id
                        )
                    })
                Toaster.show("移动成功")
                clearAllSelection()
            }
        } else {
            Toaster.show("请选择分类")
        }
    }

    /**
     * 切换单个衣橱物品的选中状态
     *
     * @param closetId 衣橱ID
     */
    fun toggleSelection(closetId: Int) {
        val currentSet = _selectedIds.value.toMutableSet()
        if (currentSet.contains(closetId)) {
            currentSet.remove(closetId)
        } else {
            currentSet.add(closetId)
        }
        _selectedIds.value = currentSet
    }

    /**
     * 检查是否所有项都被选中
     *
     * @return 是否全选
     */
    fun isAllSelected(): Boolean {
        val currentItems = closets.value
        return currentItems.isNotEmpty() && currentItems.all { it.isSelected }
    }

    /**
     * 更新全选/取消全选状态
     */
    fun updateAllSelection() {
        if (isAllSelected()) {
            clearAllSelection()
        } else {
            selectAll()
        }
    }

    /**
     * 全选所有项
     */
    private fun selectAll() {
        viewModelScope.launch {
            val currentItems = closets.first()
            _selectedIds.value = currentItems.map { it.addClosetEntity.closet.id }.toSet()
        }
    }

    /**
     * 清除所有选中项
     */
    fun clearAllSelection() {
        _selectedIds.value = emptySet()
    }

    fun updateSortWay(sortWay: Pair<Int, String>?) {
        _uiState.update {
            it.copy(sortWay = sortWay)
        }
    }

    fun updateCategoryWay(categoryWay: SubCategoryEntity?) {
        _uiState.update {
            it.copy(subCategoryEntity = categoryWay)
        }
    }
}