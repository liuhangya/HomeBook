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
import com.fanda.homebook.entity.ShowBottomSheetType
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

class StockHomeViewModel(private val rackRepository: RackRepository, private val stockRepository: StockRepository) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(StockHomeUiState())
    private val _rackUiState = MutableStateFlow(StockHomeRackUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    val rackUiState = _rackUiState.asStateFlow()

    // 选中的货架
    val curSelectRack: StateFlow<RackEntity?> = rackRepository.getSelectedItem().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
    )

    // 当 curSelectRack 变化时，重新查询当前子分类
    @OptIn(ExperimentalCoroutinesApi::class) val rackSubCategoryList: StateFlow<List<RackSubCategoryEntity>> = curSelectRack.flatMapLatest { rack ->
        if (rack == null) {
            flowOf(emptyList())
        } else {
            // 缓存起来全局使用
            UserCache.rackId = rack.id
            _rackUiState.update {
                it.copy(rackId = rack.id)
            }
            // 清空之前选中的子分类
            _uiState.update {
                it.copy(curSelectRackSubCategory = null, rackId = rack.id)
            }
            rackRepository.getAllSubItemsById(rack.id).catch { e ->
                // 处理错误
                LogUtils.e("查询失败: ${e.message}")
                emit(emptyList())
            }
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 当 _uiState 变化时，重新查询当前列表数量
    @OptIn(ExperimentalCoroutinesApi::class) val stocks: StateFlow<List<AddStockEntity>> = _uiState.flatMapLatest { state ->
        LogUtils.i("查询当前囤货列表： $state")
        stockRepository.getStocksByRackAndSubCategory(state.rackId, state.curSelectRackSubCategory?.id, state.curSelectUseStatus.useStatus.code).catch { e ->
            // 处理错误
            LogUtils.e("查询失败: ${e.message}")
            emit(emptyList())
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )


    // 当 _rackUiState 变化时，重新查询状态数量列表
    @OptIn(ExperimentalCoroutinesApi::class) val stockStatusCounts: StateFlow<StockStatusCounts?> = _rackUiState.flatMapLatest { state ->
        LogUtils.i("查询当前货架使用数量状态： $state")
        stockRepository.getStockStatusCounts(state.rackId).map { count ->
            val stockStatusList = listOf(
                StockStatusEntity(
                    useStatus = StockUseStatus.ALL, name = "全部", count = count!!.allCount - count.usedCount
                ), StockStatusEntity(
                    useStatus = StockUseStatus.USING, name = "使用中", count = count.usingCount
                ), StockStatusEntity(
                    useStatus = StockUseStatus.NO_USE, name = "未开封", count = count.noUseCount
                ), StockStatusEntity(
                    useStatus = StockUseStatus.USED, name = "已用完", count = count.usedCount
                )
            )
            // 更新数量，触发重新查询列表数据
            _uiState.update {
                it.copy(count = count.allCount - count.usedCount)
            }
            _rackUiState.update {
                it.copy(statusList = stockStatusList)
            }
            count
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = StockStatusCounts(0, 0, 0, 0)
    )

    // 货架列表
    var racks by mutableStateOf(emptyList<RackEntity>())
        private set

    init {
        viewModelScope.launch {
            racks = rackRepository.getItems()
        }
    }

    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    fun updateSelectedRack(entity: RackEntity) {
        viewModelScope.launch {
            curSelectRack.value?.let {
                rackRepository.updateItem(it.copy(selected = false))
                rackRepository.updateItem(entity.copy(selected = true))
            }
        }
    }

    fun updateSelectedSubRackCategory(entity: RackSubCategoryEntity) {
        viewModelScope.launch {
            // 反选掉之前选中的
            if (_uiState.value.curSelectRackSubCategory != null && _uiState.value.curSelectRackSubCategory!!.id == entity.id) {
                _uiState.update {
                    it.copy(curSelectRackSubCategory = null)
                }
            } else {
                // 选中当前的子分类
                _uiState.update {
                    it.copy(curSelectRackSubCategory = entity)
                }
            }
        }
    }

    fun updateSelectedUseStatus(entity: StockStatusEntity) {
        _uiState.update {
            it.copy(curSelectUseStatus = entity)
        }
    }
}