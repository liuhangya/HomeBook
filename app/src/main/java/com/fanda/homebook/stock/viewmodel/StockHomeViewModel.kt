package com.fanda.homebook.stock.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackRepository
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.stock.AddStockEntity
import com.fanda.homebook.data.stock.StockRepository
import com.fanda.homebook.data.stock.StockStatusCounts
import com.fanda.homebook.data.stock.StockStatusEntity
import com.fanda.homebook.data.stock.StockUseStatus
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.stock.state.StockHomeRackUiState
import com.fanda.homebook.stock.state.StockHomeUiState
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
 * 库存首页ViewModel
 * 负责管理库存首页的业务逻辑和状态，包括货架选择、分类筛选、状态统计等
 *
 * @param rackRepository 货架数据仓库
 * @param stockRepository 库存数据仓库
 */
class StockHomeViewModel(
    private val rackRepository: RackRepository, private val stockRepository: StockRepository
) : ViewModel() {

    // 私有可变状态流，用于保存主页UI状态
    private val _uiState = MutableStateFlow(StockHomeUiState())

    // 私有可变状态流，用于保存货架相关UI状态
    private val _rackUiState = MutableStateFlow(StockHomeRackUiState())

    // 公开只读状态流，用于UI层观察主页状态变化
    val uiState = _uiState.asStateFlow()

    // 公开只读状态流，用于UI层观察货架状态变化
    val rackUiState = _rackUiState.asStateFlow()

    // 当前选中的货架状态流（从数据库查询当前选中的货架）
    val curSelectRack: StateFlow<RackEntity?> = rackRepository.getSelectedItem().stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
    )

    // 当前选中货架的子分类列表状态流（当货架变化时重新查询）
    @OptIn(ExperimentalCoroutinesApi::class) val rackSubCategoryList: StateFlow<List<RackSubCategoryEntity>> = curSelectRack.flatMapLatest { rack ->
        if (rack == null) {
            flowOf(emptyList())  // 没有选中货架时返回空列表
        } else {
            // 缓存选中的货架ID到用户缓存
            UserCache.rackId = rack.id
            _rackUiState.update {
                it.copy(rackId = rack.id)
            }
            // 清空之前选中的子分类
            _uiState.update {
                it.copy(curSelectRackSubCategory = null, rackId = rack.id)
            }
            // 查询当前货架的所有子分类
            rackRepository.getAllSubItemsById(rack.id).catch { e ->
                // 处理查询错误
                LogUtils.e("查询子分类失败: ${e.message}")
                emit(emptyList())
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 库存物品列表状态流（根据筛选条件动态查询）
    @OptIn(ExperimentalCoroutinesApi::class) val stocks: StateFlow<List<AddStockEntity>> = _uiState.flatMapLatest { state ->
        LogUtils.i("查询当前囤货列表： $state")
        // 根据货架ID、子分类ID和使用状态查询库存列表
        stockRepository.getStocksByRackAndSubCategory(
            state.rackId, state.curSelectRackSubCategory?.id, state.curSelectUseStatus.useStatus.code
        ).catch { e ->
            // 处理查询错误
            LogUtils.e("查询库存列表失败: ${e.message}")
            emit(emptyList())
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 库存状态数量统计状态流（统计不同状态下的物品数量）
    @OptIn(ExperimentalCoroutinesApi::class) val stockStatusCounts: StateFlow<StockStatusCounts?> = _rackUiState.flatMapLatest { state ->
        LogUtils.i("查询当前货架使用数量状态： $state")
        // 查询指定货架的状态数量统计
        stockRepository.getStockStatusCounts(state.rackId).map { count ->
            // 根据统计结果更新状态列表的数量
            val stockStatusList = listOf(
                StockStatusEntity(
                    useStatus = StockUseStatus.ALL, name = "全部", count = count!!.allCount - count.usedCount  // 排除已用完的数量
                ), StockStatusEntity(
                    useStatus = StockUseStatus.USING, name = "使用中", count = count.usingCount
                ), StockStatusEntity(
                    useStatus = StockUseStatus.NO_USE, name = "未开封", count = count.noUseCount
                ), StockStatusEntity(
                    useStatus = StockUseStatus.USED, name = "已用完", count = count.usedCount
                )
            )
            // 更新主页的总数量显示
            _uiState.update {
                it.copy(count = count.allCount - count.usedCount)
            }
            // 更新货架状态列表
            _rackUiState.update {
                it.copy(statusList = stockStatusList)
            }
            count
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = StockStatusCounts(0, 0, 0, 0)
    )

    // 货架列表状态（所有可用的货架）
    var racks by mutableStateOf(emptyList<RackEntity>())
        private set

    init {
        viewModelScope.launch {
            // 初始化时加载所有货架列表
            racks = rackRepository.getItems()
        }
    }

    /**
     * 更新底部弹窗类型
     *
     * @param type 要显示的底部弹窗类型
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

    /**
     * 更新选中的货架
     *
     * @param entity 要选中的货架实体
     */
    fun updateSelectedRack(entity: RackEntity) {
        viewModelScope.launch {
            // 取消之前选中货架的选中状态
            curSelectRack.value?.let {
                rackRepository.updateItem(it.copy(selected = false))
            }
            // 设置新货架为选中状态
            rackRepository.updateItem(entity.copy(selected = true))
        }
    }

    /**
     * 更新选中的货架子分类
     * 点击已选中的子分类会取消选择
     *
     * @param entity 要选中的货架子分类实体
     */
    fun updateSelectedSubRackCategory(entity: RackSubCategoryEntity) {
        viewModelScope.launch {
            // 如果点击的是当前已选中的子分类，则取消选择
            if (_uiState.value.curSelectRackSubCategory != null && _uiState.value.curSelectRackSubCategory!!.id == entity.id) {
                _uiState.update {
                    it.copy(curSelectRackSubCategory = null)
                }
            } else {
                // 选中新的子分类
                _uiState.update {
                    it.copy(curSelectRackSubCategory = entity)
                }
            }
        }
    }

    /**
     * 更新选中的使用状态
     *
     * @param entity 要选中的使用状态实体
     */
    fun updateSelectedUseStatus(entity: StockStatusEntity) {
        _uiState.update {
            it.copy(curSelectUseStatus = entity)
        }
        // 当选择"全部"状态时，清空子分类的选择
        if (entity.useStatus == StockUseStatus.ALL) {
            _uiState.update {
                it.copy(curSelectRackSubCategory = null)
            }
        }
    }
}