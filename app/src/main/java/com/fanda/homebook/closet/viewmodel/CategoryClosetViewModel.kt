package com.fanda.homebook.closet.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.CategoryClosetUiState
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.closet.ClosetSubCategoryGridItem
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.fanda.homebook.tools.UserCache
import com.fanda.homebook.tools.fromJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CategoryClosetViewModel(
    savedStateHandle: SavedStateHandle,
    private val closetRepository: ClosetRepository,
) : ViewModel() {

    private val categoryEntity = savedStateHandle.get<String>("categoryEntity") ?: ""

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(CategoryClosetUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                categoryEntity = categoryEntity.fromJson<CategoryEntity>()
            )
        }
    }

    val closets = closetRepository.getClosetsByCategory(UserCache.ownerId, _uiState.value.categoryEntity.id).catch { e ->
        // 处理错误
        LogUtils.e("查询失败: ${e.message}")
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 按二级分类分组的 StateFlow
    val groupedClosets: StateFlow<List<ClosetSubCategoryGridItem>> = closets.map { closetsList ->
        closetsList.groupBy { closet ->
            closet.subCategory ?: SubCategoryEntity(name = "未分类", categoryId = -1)
        }.map { (categoryEntity, list) ->
            val lastEntity = list.last()
            ClosetSubCategoryGridItem(
                imageLocalPath = lastEntity.closet.imageLocalPath, category = categoryEntity, count = list.size
            )
        }.sortedBy { it.category.sortOrder }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }


}