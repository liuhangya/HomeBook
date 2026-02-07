package com.fanda.homebook.closet.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.HomeClosetUiState
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.closet.AddClosetEntity
import com.fanda.homebook.data.closet.ClosetGridItem
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.owner.OwnerRepository
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.fanda.homebook.tools.UserCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 衣橱首页视图模型
 *
 * 负责管理衣橱首页的业务逻辑和状态，包括归属切换、分类显示等
 */
class HomeClosetViewModel(
    private val closetRepository: ClosetRepository,  // 衣橱仓库
    private val ownerRepository: OwnerRepository,    // 归属仓库
) : ViewModel() {

    // 私有可变状态流
    private val _addClosetUiState = MutableStateFlow(HomeClosetUiState())

    // 公开只读状态流
    val addClosetUiState = _addClosetUiState.asStateFlow()

    // 当前选中的归属
    val curSelectOwner: StateFlow<OwnerEntity?> = ownerRepository.getSelectedItem().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
    )

    // 垃圾桶中的衣橱物品数据流
    @OptIn(ExperimentalCoroutinesApi::class) private val moveToTrashClosets: StateFlow<List<AddClosetEntity>> = curSelectOwner.flatMapLatest { owner ->
        if (owner == null) {
            flowOf(emptyList())
        } else {
            // 缓存当前归属ID到全局用户缓存
            UserCache.ownerId = owner.id
            // 查询垃圾桶中的衣橱物品
            closetRepository.getClosets(owner.id, moveToTrash = true).catch { e ->
                LogUtils.e("查询失败: ${e.message}")
                emit(emptyList())
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 垃圾桶数据显示项
    val trashData: StateFlow<ClosetGridItem?> = moveToTrashClosets.map { closetsList ->
        closetsList.lastOrNull()?.let { lastEntity ->
            // 使用垃圾桶中最后一个物品的图片作为封面
            ClosetGridItem(
                imageLocalPath = lastEntity.closet.imageLocalPath, category = lastEntity.category ?: CategoryEntity(name = "垃圾桶"), count = closetsList.size, moveToTrash = true
            )
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
    )

    // 正常衣橱物品数据流
    @OptIn(ExperimentalCoroutinesApi::class) private val closets: StateFlow<List<AddClosetEntity>> = curSelectOwner.flatMapLatest { owner ->
        if (owner == null) {
            flowOf(emptyList())
        } else {
            // 缓存当前归属ID到全局用户缓存
            UserCache.ownerId = owner.id
            // 查询正常的衣橱物品
            closetRepository.getClosets(owner.id).catch { e ->
                LogUtils.e("查询失败: ${e.message}")
                emit(emptyList())
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 按一级分类分组后的衣橱数据
    val groupedClosets: StateFlow<List<ClosetGridItem>> = closets.map { closetsList ->
        // 按一级分类分组
        closetsList.groupBy { closet ->
            closet.category ?: CategoryEntity(name = "未分类")
        }.map { (categoryEntity, list) ->
            // 使用该分类下的最后一个物品的图片作为封面
            val lastEntity = list.last()
            ClosetGridItem(
                imageLocalPath = lastEntity.closet.imageLocalPath, category = categoryEntity, count = list.size
            )
        }.sortedBy { it.category.sortOrder }  // 按排序顺序排序
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 归属列表
    var owners by mutableStateOf(emptyList<OwnerEntity>())
        private set

    init {
        // 初始化：加载归属列表
        viewModelScope.launch {
            owners = ownerRepository.getItems()
        }
    }

    /**
     * 更新底部弹窗类型
     *
     * @param type 弹窗类型
     */
    fun updateSheetType(type: ShowBottomSheetType) {
        _addClosetUiState.update { it.copy(sheetType = type) }
    }

    /**
     * 关闭底部弹窗
     */
    fun dismissBottomSheet() {
        _addClosetUiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    /**
     * 更新选中的归属
     *
     * @param ownerEntity 要选中的归属实体
     */
    fun updateSelectedOwner(ownerEntity: OwnerEntity) {
        viewModelScope.launch {
            // 先取消当前选中的归属
            curSelectOwner.value?.let {
                ownerRepository.updateItem(it.copy(selected = false))
            }
            // 选中新的归属
            ownerRepository.updateItem(ownerEntity.copy(selected = true))
        }
    }

    /**
     * 检查指定分类下是否有衣橱物品（用于确认删除分类）
     *
     * @param categoryId 分类ID
     * @param onResult 检查结果回调
     */
    fun hasClosetsWithSubcategory(categoryId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = closetRepository.hasClosetsWithSubcategory(
                curSelectOwner.value?.id ?: -1, categoryId
            )
            onResult(result)
        }
    }
}