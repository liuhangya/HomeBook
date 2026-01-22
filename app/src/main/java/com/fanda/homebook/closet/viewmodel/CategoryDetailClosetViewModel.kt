package com.fanda.homebook.closet.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.CategoryClosetUiState
import com.fanda.homebook.closet.state.CategoryDetailClosetUiState
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.CategoryRepository
import com.fanda.homebook.data.category.CategoryWithSubCategories
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.closet.AddClosetEntity
import com.fanda.homebook.data.closet.ClosetDetailGridItem
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.closet.ClosetSubCategoryGridItem
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.fanda.homebook.tools.UserCache
import com.fanda.homebook.tools.fromJson
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

class CategoryDetailClosetViewModel(
    savedStateHandle: SavedStateHandle,
    private val closetRepository: ClosetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val categoryId = savedStateHandle.get<Int>("categoryId") ?: -1
    private val subCategoryId = savedStateHandle.get<Int>("subCategoryId") ?: -1
    private val categoryName = savedStateHandle.get<String>("categoryName") ?: ""
    private val moveToTrash = savedStateHandle.get<Boolean>("moveToTrash") ?: false

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(CategoryDetailClosetUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                categoryId = categoryId, categoryName =  categoryName, subCategoryId = subCategoryId,
                moveToTrash = moveToTrash
            )
        }
    }

    // 分类列表
    val categories: StateFlow<List<CategoryWithSubCategories>> =
        categoryRepository.getAllItemsWithSub().stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
        )

    // 原始数据流
    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawClosets = _uiState.map {
        it.categoryId
    }.flatMapLatest { categoryId ->
        if (_uiState.value.moveToTrash) {
            closetRepository.getClosets(UserCache.ownerId, moveToTrash = true)
        } else if (categoryId == -1) {
            closetRepository.getClosetsBySubCategory(
                UserCache.ownerId, _uiState.value.subCategoryId
            )
        } else {
            closetRepository.getClosetsByCategory(
                UserCache.ownerId, _uiState.value.categoryId
            )
        }
    }.map { items ->
        items.map { ClosetDetailGridItem(addClosetEntity = it) }
    }.catch { e ->
        LogUtils.e("查询失败: ${e.message}")
        emit(emptyList())
    }

    // 选中的ID集合
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow()

    // 合并选中状态的数据流
    val closets = combine(
        rawClosets, _selectedIds
    ) { items, selectedIds ->
        items.map { item ->
            // 根据选中ID集合更新每个项的选中状态
            item.copyWithSelected(selectedIds.contains(item.addClosetEntity.closet.id))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    // 获取选中项
    val selectedItems: StateFlow<List<AddClosetEntity>> = combine(
        closets, _selectedIds
    ) { items, selectedIds ->
        items.filter { it.isSelected }.map { it.addClosetEntity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    // 选中项数量
    val selectedCount: StateFlow<Int> = _selectedIds.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = 0
    )

    fun updateSheetType(type: ShowBottomSheetType) {
        LogUtils.i("当前选中的物品数量： ${selectedIds.value.size}")
        if (uiState.value.isEditState && selectedIds.value.isEmpty()) {
            Toaster.show("请选择物品")
        } else {
            _uiState.update { it.copy(sheetType = type) }
        }
    }

    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    fun toggleEditState(show: Boolean) {
        _uiState.update {
            it.copy(isEditState = show)
        }
    }

    fun copyEntityDatabase() {
        viewModelScope.launch {
            // id 重置为0 , 数据库自增
            closetRepository.insertAll(selectedItems.value.map { it.closet.copy(id = 0) })
            Toaster.show("复制成功")
            clearAllSelection()
        }
    }

    fun deleteEntityDatabase() {
        viewModelScope.launch {
            closetRepository.deleteAll(selectedItems.value.map { it.closet })
            Toaster.show("删除成功")
            clearAllSelection()
        }
    }

    fun updateEntityDatabase(category: CategoryEntity?, subCategory: SubCategoryEntity?) {
        if (category != null) {
            viewModelScope.launch {
                closetRepository.updateAll(selectedItems.value.map {
                    it.closet.copy(
                        categoryId = category.id,
                        subCategoryId = subCategory?.id
                    )
                })
                Toaster.show("移动成功")
                clearAllSelection()
            }
        } else {
            Toaster.show("请选择分类")
        }
    }


    // 点击切换选中状态的方法
    fun toggleSelection(closetId: Int) {
        val currentSet = _selectedIds.value.toMutableSet()
        if (currentSet.contains(closetId)) {
            currentSet.remove(closetId)
        } else {
            currentSet.add(closetId)
        }
        _selectedIds.value = currentSet
    }

    fun isAllSelected(): Boolean {
        val currentItems = closets.value
        return currentItems.isNotEmpty() && currentItems.all { it.isSelected }
    }

    // 批量选中/取消选中
    fun updateAllSelection() {
        if (isAllSelected()) {
            clearAllSelection()
        } else {
            selectAll()
        }
    }

    // 批量选中/取消选中
    private fun selectAll() {
        viewModelScope.launch {
            val currentItems = closets.first()
            _selectedIds.value = currentItems.map { it.addClosetEntity.closet.id }.toSet()
        }
    }

    fun clearAllSelection() {
        _selectedIds.value = emptySet()
    }


}