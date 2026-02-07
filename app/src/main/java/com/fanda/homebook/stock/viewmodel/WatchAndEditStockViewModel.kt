package com.fanda.homebook.stock.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.data.period.PeriodEntity
import com.fanda.homebook.data.period.PeriodRepository
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.product.ProductRepository
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackRepository
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.stock.StockRepository
import com.fanda.homebook.data.stock.StockUseStatus
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.stock.state.WatchAndEditStockUiState
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.fanda.homebook.tools.saveUriToFilesDir
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
 * 查看和编辑库存物品的ViewModel
 * 负责管理查看和编辑库存物品页面的业务逻辑和状态
 *
 * @param savedStateHandle 用于保存和恢复状态的状态句柄，包含库存物品ID
 * @param stockRepository 库存数据仓库
 * @param rackRepository 货架数据仓库
 * @param productRepository 品牌/产品数据仓库
 * @param periodRepository 使用时段数据仓库
 */
class WatchAndEditStockViewModel(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository,
    private val rackRepository: RackRepository,
    private val productRepository: ProductRepository,
    private val periodRepository: PeriodRepository
) : ViewModel() {

    // 从SavedStateHandle中获取传入的库存物品ID参数
    private val stockId: Int = savedStateHandle["stockId"] ?: -1

    // 私有可变状态流，用于保存UI状态
    private val _uiState = MutableStateFlow(WatchAndEditStockUiState())

    // 公开只读状态流，用于UI层观察状态变化
    val uiState = _uiState.asStateFlow()

    // 货架列表状态
    var racks by mutableStateOf(emptyList<RackEntity>())
        private set

    // 使用时段列表状态
    var periods by mutableStateOf(emptyList<PeriodEntity>())
        private set

    // 品牌/产品列表状态流
    val products: StateFlow<List<ProductEntity>> = productRepository.getItems().stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            // 初始化时加载数据
            racks = rackRepository.getItems()
            periods = periodRepository.getTypes()
            // 根据ID加载库存物品
            val item = stockRepository.getStockById(stockId)
            _uiState.update { it.copy(stockEntity = item.stock) }
        }
    }

    // 当前选中货架的子分类列表状态流
    @OptIn(ExperimentalCoroutinesApi::class) val rackSubCategoryList: StateFlow<List<RackSubCategoryEntity>> = _uiState.map { it.stockEntity.rackId }  // 提取货架ID
        .distinctUntilChanged()          // 货架ID变化时才触发
        .flatMapLatest { id ->           // 货架ID变化时重新查询子分类
            rackRepository.getAllSubItemsById(id)
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
        )

    // 当前选中货架的实体状态流
    @OptIn(ExperimentalCoroutinesApi::class) val rackEntity: StateFlow<RackEntity?> = _uiState.map { it.stockEntity.rackId }  // 提取货架ID
        .distinctUntilChanged()          // 货架ID变化时才触发
        .flatMapLatest { id ->           // 货架ID变化时重新查询货架实体
            rackRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 当前选中的货架子分类实体状态流
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<RackSubCategoryEntity?> = _uiState.map { it.stockEntity.subCategoryId }  // 提取子分类ID
        .distinctUntilChanged()                 // 子分类ID变化时才触发
        .flatMapLatest { id ->                  // 子分类ID变化时重新查询子分类实体
            rackRepository.getSubItemById(id)
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 当前选中的品牌/产品实体状态流
    @OptIn(ExperimentalCoroutinesApi::class) val product: StateFlow<ProductEntity?> = _uiState.map { it.stockEntity.productId }  // 提取产品ID
        .distinctUntilChanged()             // 产品ID变化时才触发
        .flatMapLatest { id ->              // 产品ID变化时重新查询产品实体
            productRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 当前选中的使用时段实体状态流
    @OptIn(ExperimentalCoroutinesApi::class) val period: StateFlow<PeriodEntity?> = _uiState.map { it.stockEntity.periodId }  // 提取时段ID
        .distinctUntilChanged()            // 时段ID变化时才触发
        .flatMapLatest { id ->             // 时段ID变化时重新查询时段实体
            periodRepository.getTypeById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    /**
     * 更新底部弹窗类型
     *
     * @param type 要显示的底部弹窗类型
     * @param forceShow 是否强制显示（跳过编辑状态检查）
     */
    fun updateSheetType(type: ShowBottomSheetType, forceShow: Boolean = false) {
        // 非编辑状态下通常不允许显示弹窗（除非强制）
        if (!_uiState.value.isEditState && !forceShow) {
            return
        }
        // 选择分类弹窗前需要先选择货架
        if (type == ShowBottomSheetType.CATEGORY && rackEntity.value == null) {
            Toaster.show("请先选择货架")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(sheetType = type) }
        }
    }

    /**
     * 检查是否显示指定类型的底部弹窗
     *
     * @param type 要检查的底部弹窗类型
     * @return 是否显示该类型的弹窗
     */
    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    /**
     * 关闭底部弹窗
     */
    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    /**
     * 更新库存物品名称
     *
     * @param name 新的物品名称
     */
    fun updateStockName(name: String) {
        _uiState.update { it.copy(stockEntity = it.stockEntity.copy(name = name)) }
    }

    /**
     * 更新是否同步至账单的开关状态
     *
     * @param syncBook 是否同步至账单
     */
    fun updateSyncBook(syncBook: Boolean) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(syncBook = syncBook)
            )
        }
    }

    /**
     * 更新价格
     *
     * @param price 价格字符串
     */
    fun updatePrice(price: String) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(price = price)
            )
        }
    }

    /**
     * 更新备注/评论
     *
     * @param comment 备注内容
     */
    fun updateClosetComment(comment: String) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(comment = comment)
            )
        }
    }

    /**
     * 更新购入日期
     *
     * @param date 购入日期时间戳
     */
    fun updateBuyDate(date: Long) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(buyDate = date)
            )
        }
    }

    /**
     * 更新开封日期
     *
     * @param date 开封日期时间戳
     */
    fun updateOpenDate(date: Long) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(openDate = date)
            )
        }
    }

    /**
     * 更新过期日期
     *
     * @param date 过期日期时间戳
     */
    fun updateExpireDate(date: Long) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(expireDate = date)
            )
        }
    }

    /**
     * 更新货架选择
     *
     * @param rackEntity 选中的货架实体
     */
    fun updateRack(rackEntity: RackEntity?) {
        rackEntity?.let {
            _uiState.update {
                it.copy(
                    stockEntity = it.stockEntity.copy(rackId = rackEntity.id)
                )
            }
        }
    }

    /**
     * 更新品牌/产品选择
     *
     * @param productEntity 选中的品牌/产品实体
     */
    fun updateProduct(productEntity: ProductEntity?) {
        productEntity?.let {
            _uiState.update {
                it.copy(
                    stockEntity = it.stockEntity.copy(productId = productEntity.id)
                )
            }
        }
    }

    /**
     * 更新分类选择
     *
     * @param subCategoryEntity 选中的货架子分类实体
     */
    fun updateCategory(subCategoryEntity: RackSubCategoryEntity?) {
        subCategoryEntity?.let {
            _uiState.update {
                it.copy(
                    stockEntity = it.stockEntity.copy(subCategoryId = subCategoryEntity.id)
                )
            }
        }
    }

    /**
     * 更新使用时段选择
     *
     * @param periodEntity 选中的使用时段实体
     */
    fun updatePeriod(periodEntity: PeriodEntity?) {
        periodEntity?.let {
            _uiState.update {
                it.copy(
                    stockEntity = it.stockEntity.copy(periodId = periodEntity.id)
                )
            }
        }
    }

    /**
     * 更新保鲜期（月数）
     *
     * @param month 保鲜期月数
     */
    fun updateShelfMonth(month: Int) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(shelfMonth = month)
            )
        }
    }

    /**
     * 更新剩余量
     *
     * @param remain 剩余量描述
     */
    fun updateRemain(remain: String) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(remain = remain)
            )
        }
    }

    /**
     * 更新使用感受
     *
     * @param feel 使用感受描述
     */
    fun updateFeel(feel: String) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(feel = feel)
            )
        }
    }

    /**
     * 更新用完日期
     *
     * @param date 用完日期时间戳
     */
    fun updateUsedUpDate(date: Long) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(usedDate = date)
            )
        }
    }

    /**
     * 更新图片URI
     *
     * @param imageUri 图片URI
     */
    fun updateImageUrl(imageUri: Uri) {
        _uiState.update { it.copy(imageUri = imageUri) }
    }

    /**
     * 更新编辑状态
     *
     * @param state 是否为编辑状态
     */
    fun updateEditState(state: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isEditState = state,
                )
            }
        }
    }

    /**
     * 删除库存实体到数据库
     *
     * @param onResult 删除结果回调，参数为是否删除成功
     */
    fun deleteEntityDatabase(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            stockRepository.delete(uiState.value.stockEntity)
            onResult(true)
        }
    }

    /**
     * 复制库存实体到数据库（创建新记录）
     *
     * @param onResult 复制结果回调，参数为是否复制成功
     */
    fun copyEntityDatabase(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // 复制时重置ID为0，让数据库自动生成新ID
            stockRepository.insert(uiState.value.stockEntity.copy(id = 0))
            onResult(true)
        }
    }

    /**
     * 更新使用状态
     *
     * @param context 上下文，用于可能的图片保存操作
     * @param status 要更新的使用状态
     */
    fun updateUseStatus(context: Context, status: StockUseStatus) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    stockEntity = it.stockEntity.copy(useStatus = status.code)
                )
            }
            updateStockEntityDatabase(context)
        }
    }

    /**
     * 更新用完日期选择对话框的显示状态
     *
     * @param show 是否显示对话框
     */
    fun updateUsedUpDateSelectDialog(show: Boolean) {
        _uiState.update {
            it.copy(
                showUsedUpDateSelectDialog = show
            )
        }
    }

    /**
     * 更新库存实体到数据库
     *
     * @param context 上下文，用于图片文件保存
     * @param onResult 更新结果回调，参数为是否更新成功
     */
    fun updateStockEntityDatabase(context: Context, onResult: (Boolean) -> Unit = {}) {
        val entity = uiState.value.stockEntity
        // 验证必填字段
        if (entity.name.isEmpty()) {
            Toaster.show("请输入名称")
        } else if (entity.rackId == 0) {
            Toaster.show("请选择货架")
        } else if (entity.subCategoryId == 0) {
            Toaster.show("请选择类别")
        } else {
            viewModelScope.launch {
                // 如果更换了图片，保存新图片到文件系统
                if (_uiState.value.imageUri != null) {
                    val imageFile = saveUriToFilesDir(context, _uiState.value.imageUri!!)
                    stockRepository.update(entity.copy(imageLocalPath = imageFile?.absolutePath ?: ""))
                } else {
                    // 未更换图片，直接更新数据库
                    stockRepository.update(entity)
                }
                onResult(true)
            }
        }
    }
}