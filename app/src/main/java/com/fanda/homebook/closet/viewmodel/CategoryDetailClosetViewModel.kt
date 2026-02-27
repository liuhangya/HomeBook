package com.fanda.homebook.closet.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.data.SORT_WAY_ADD_TIME
import com.fanda.homebook.data.SORT_WAY_BUY_TIME
import com.fanda.homebook.data.SORT_WAY_CLOTH_COUNT
import com.fanda.homebook.data.SORT_WAY_PRICE
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.color.ColorTypeRepository
import com.fanda.homebook.data.plan.PlanEntity
import com.fanda.homebook.data.quick.QueryParams
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.season.SeasonRepository
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.first


data class ClosetQueryParams(
    val categoryId: Int = -1,                                   // 一级分类ID
    val sortWay: Pair<Int, String>? = LocalDataSource.sortWayData.first(),   // 排序方式
    val subCategoryEntity: SubCategoryEntity? = SubCategoryEntity(-1, "全部", categoryId = -1),           // -1表示全部
    val seasonEntity: SeasonEntity? = null,          // null 表示不筛选
    val colorTypeEntity: ColorTypeEntity? = null,          // null 表示不筛选
)

/**
 * 分类详情衣橱视图模型
 *
 * 负责管理分类详情页面的业务逻辑和状态，支持多选操作
 */
class CategoryDetailClosetViewModel(
    savedStateHandle: SavedStateHandle,
    private val closetRepository: ClosetRepository,
    private val categoryRepository: CategoryRepository,
    private val colorTypeRepository: ColorTypeRepository,
    private val seasonRepository: SeasonRepository
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

    // 季节列表
    var seasons by mutableStateOf(emptyList<SeasonEntity>())
        private set

    init {
        // 初始化UI状态
        _uiState.update {
            it.copy(
                categoryId = categoryId, categoryName = categoryName, subCategoryId = subCategoryId, moveToTrash = moveToTrash
            )
        }
        viewModelScope.launch {
            seasons = seasonRepository.getSeasons()
        }
    }

    // 颜色列表
    val colorTypes: StateFlow<List<ColorTypeEntity>> = colorTypeRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 分类列表（包含子分类）
    val categories: StateFlow<List<CategoryWithSubCategories>> = categoryRepository.getAllItemsWithSub().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 当前父分类下的所有子分类列表状态流
    val subCategories: StateFlow<List<SubCategoryEntity>> = categoryRepository.getSubItemsById(categoryId).stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 核心查询参数组合
    @OptIn(ExperimentalCoroutinesApi::class) private val queryParams = combine(
        _uiState.map { it.categoryId },
        _uiState.map { it.sortWay },
        _uiState.map { it.subCategoryEntity },
        _uiState.map { it.seasonEntity },
        _uiState.map { it.colorTypeEntity },
    ) { categoryId, sortWay, subCategoryEntity, seasonEntity, colorTypeEntity ->
        ClosetQueryParams(categoryId, sortWay, subCategoryEntity, seasonEntity, colorTypeEntity)
    }.distinctUntilChanged()

    // 基础衣橱数据流 - 只根据分类条件查询
    @OptIn(ExperimentalCoroutinesApi::class) private val baseClosetsFlow = queryParams.map { it.categoryId }.distinctUntilChanged().flatMapLatest { currentCategoryId ->
        // 根据不同的查询条件获取基础数据
        when {
            moveToTrash -> {
                // 垃圾桶模式：获取已删除的衣橱物品
                closetRepository.getClosets(UserCache.ownerId, moveToTrash = true)
            }

            currentCategoryId <= 0 && subCategoryId <= 0 -> {
                // 未分类模式：获取未分类的衣橱物品
                closetRepository.getNoCategoryClosets(UserCache.ownerId)
            }

            currentCategoryId == -1 -> {
                // 二级分类模式：根据二级分类ID获取
                closetRepository.getClosetsBySubCategory(UserCache.ownerId, subCategoryId)
            }

            else -> {
                // 一级分类模式：根据一级分类ID获取
                closetRepository.getClosetsByCategory(UserCache.ownerId, currentCategoryId)
            }
        }
    }.catch { e ->
        // 错误处理
        LogUtils.e("基础数据查询失败: ${e.message}")
        emit(emptyList())
    }

    // 过滤后的衣橱数据流 - 根据所有查询参数进行过滤
    @OptIn(ExperimentalCoroutinesApi::class) private val filteredClosetsFlow = combine(
        baseClosetsFlow, queryParams
    ) { baseItems, params ->
        var filteredItems = baseItems.sortedByDescending { it.closet.createDate }

        // 按二级分类筛选
        if (params.subCategoryEntity != null && params.subCategoryEntity.id != -1) {
            filteredItems = filteredItems.filter { it.closet.subCategoryId == params.subCategoryEntity.id }
        }

        // 按季节筛选
        if (params.seasonEntity != null) {
            filteredItems = filteredItems.filter { it.seasons?.map { it.id }?.contains(params.seasonEntity.id) == true }
        }

        // 按颜色筛选
        if (params.colorTypeEntity != null) {
            filteredItems = filteredItems.filter { it.closet.colorTypeId == params.colorTypeEntity.id }
        }

        if (params.sortWay != null) {
            filteredItems = when (params.sortWay.first) {
                SORT_WAY_ADD_TIME -> filteredItems.sortedByDescending { it.closet.createDate }
                SORT_WAY_CLOTH_COUNT -> filteredItems.sortedByDescending { it.closet.wearCount }
                SORT_WAY_PRICE -> filteredItems.sortedByDescending {   it.closet.price.toFloatOrNull() ?: 0f }
                SORT_WAY_BUY_TIME -> filteredItems.sortedByDescending { it.closet.date }
                else -> {
                    filteredItems
                }
            }
        }

        // 转换为网格项格式
        filteredItems.map { ClosetDetailGridItem(addClosetEntity = it) }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 选中的衣橱ID集合
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow()

    // 合并选中状态的数据流
    val closets = combine(
        filteredClosetsFlow, _selectedIds
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

    fun updateSeasonWay(seasonWay: SeasonEntity?) {
        _uiState.update {
            it.copy(seasonEntity = seasonWay)
        }
    }

    fun updateColorTypeWay(colorTypeWay: ColorTypeEntity?) {
        _uiState.update {
            it.copy(colorTypeEntity = colorTypeWay)
        }
    }
}