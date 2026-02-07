package com.fanda.homebook.closet.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.CategoryClosetUiState
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.closet.ClosetSubCategoryGridItem
import com.fanda.homebook.common.entity.ShowBottomSheetType
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

/**
 * 衣橱分类视图模型
 *
 * 负责管理衣橱分类页面的业务逻辑和状态
 */
class CategoryClosetViewModel(
    savedStateHandle: SavedStateHandle,          // 保存状态句柄
    private val closetRepository: ClosetRepository, // 衣橱仓库
) : ViewModel() {

    // 从保存状态中获取分类实体JSON字符串
    private val categoryEntity = savedStateHandle.get<String>("categoryEntity") ?: ""

    // 私有可变状态流
    private val _uiState = MutableStateFlow(CategoryClosetUiState())

    // 公开只读状态流
    val uiState = _uiState.asStateFlow()

    init {
        // 初始化：解析分类实体
        _uiState.update {
            it.copy(
                categoryEntity = categoryEntity.fromJson<CategoryEntity>()
            )
        }
    }

    // 获取当前分类下的所有衣橱物品
    val closets = closetRepository.getClosetsByCategory(UserCache.ownerId, _uiState.value.categoryEntity.id).catch { e ->
            // 错误处理
            LogUtils.e("查询失败: ${e.message}")
            emit(emptyList())
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
        )

    // 按二级分类分组后的衣橱物品
    val groupedClosets: StateFlow<List<ClosetSubCategoryGridItem>> = closets.map { closetsList ->
        // 按二级分类分组
        closetsList.groupBy { closet ->
            closet.subCategory ?: SubCategoryEntity(name = "未分类", categoryId = -1)
        }.map { (categoryEntity, list) ->
            // 使用该分类下的最后一个衣橱物品的图片作为封面
            val lastEntity = list.last()
            ClosetSubCategoryGridItem(
                imageLocalPath = lastEntity.closet.imageLocalPath, // 封面图片路径
                category = categoryEntity,                          // 分类实体
                count = list.size                                   // 该分类下的物品数量
            )
        }.sortedBy { it.category.sortOrder } // 按排序顺序排序
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    /**
     * 更新底部弹窗类型
     *
     * @param type 弹窗类型
     */
    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    /**
     * 关闭底部弹窗
     */
    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }
}