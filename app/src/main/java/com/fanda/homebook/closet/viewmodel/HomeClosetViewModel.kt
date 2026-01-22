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
import com.fanda.homebook.entity.ShowBottomSheetType
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

class HomeClosetViewModel(
    private val closetRepository: ClosetRepository,
    private val ownerRepository: OwnerRepository,
) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _addClosetUiState = MutableStateFlow(HomeClosetUiState())

    // 公开只读对象，用于读取UI状态
    val addClosetUiState = _addClosetUiState.asStateFlow()

    // 选中的归属
    val curSelectOwner: StateFlow<OwnerEntity?> = ownerRepository.getSelectedItem().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
    )

    // 当 curSelectOwner 变化时，重新查询 moveToTrashClosets ， 用于显示垃圾桶的数据
    @OptIn(ExperimentalCoroutinesApi::class)
    private val moveToTrashClosets: StateFlow<List<AddClosetEntity>> =
        curSelectOwner.flatMapLatest { owner ->
            if (owner == null) {
                flowOf(emptyList())
            } else {
                // 缓存起来全局使用
                UserCache.ownerId = owner.id
                closetRepository.getClosets(owner.id, moveToTrash = true).catch { e ->
                    // 处理错误
                    LogUtils.e("查询失败: ${e.message}")
                    emit(emptyList())
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    // 垃圾桶转换的显示数据
    val trashData: StateFlow<ClosetGridItem?> =
        moveToTrashClosets.map { closetsList ->
            closetsList.lastOrNull()?.let { lastEntity ->
                ClosetGridItem(
                    imageLocalPath = lastEntity.closet.imageLocalPath,
                    category = lastEntity.category ?: CategoryEntity(name = "垃圾桶"),
                    count = closetsList.size,
                    moveToTrash = true
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = null
        )

    // 当 curSelectOwner 变化时，重新查询 closets
    @OptIn(ExperimentalCoroutinesApi::class)
    private val closets: StateFlow<List<AddClosetEntity>> = curSelectOwner.flatMapLatest { owner ->
        if (owner == null) {
            flowOf(emptyList())
        } else {
            // 缓存起来全局使用
            UserCache.ownerId = owner.id
            closetRepository.getClosets(owner.id).catch { e ->
                // 处理错误
                LogUtils.e("查询失败: ${e.message}")
                emit(emptyList())
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    // 分组后的衣橱数据
    // 按一级分类分组的 StateFlow
    val groupedClosets: StateFlow<List<ClosetGridItem>> = closets.map { closetsList ->
        closetsList.groupBy { closet ->
            closet.category ?: CategoryEntity(name = "未分类")
        }.map { (categoryEntity, list) ->
            val lastEntity = list.last()
            ClosetGridItem(
                imageLocalPath = lastEntity.closet.imageLocalPath,
                category = categoryEntity,
                count = list.size
            )
        }.sortedBy { it.category.sortOrder }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    // 归属列表
    var owners by mutableStateOf(emptyList<OwnerEntity>())
        private set

    init {
        viewModelScope.launch {
            owners = ownerRepository.getItems()
        }
    }

    fun updateSheetType(type: ShowBottomSheetType) {
        _addClosetUiState.update { it.copy(sheetType = type) }
    }

    fun dismissBottomSheet() {
        _addClosetUiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    fun updateSelectedOwner(ownerEntity: OwnerEntity) {
        viewModelScope.launch {
            curSelectOwner.value?.let {
                ownerRepository.updateItem(it.copy(selected = false))
                ownerRepository.updateItem(ownerEntity.copy(selected = true))
            }
        }
    }

    fun hasClosetsWithSubcategory(categoryId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = closetRepository.hasClosetsWithSubcategory(
                curSelectOwner.value?.id ?: -1,
                categoryId
            )
            onResult(result)
        }
    }


}