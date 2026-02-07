package com.fanda.homebook.stock.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.data.period.PeriodEntity
import com.fanda.homebook.data.period.PeriodRepository
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.product.ProductRepository
import com.fanda.homebook.data.quick.QuickEntity
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackRepository
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.stock.StockRepository
import com.fanda.homebook.data.stock.StockUseStatus
import com.fanda.homebook.data.transaction.TransactionRepository
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.entity.TransactionAmountType
import com.fanda.homebook.stock.state.AddStockUiState
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.fanda.homebook.tools.UserCache
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
 * 添加库存物品的ViewModel
 * 负责管理添加库存物品页面的业务逻辑和状态
 *
 * @param savedStateHandle 用于保存和恢复状态的状态句柄
 * @param stockRepository 库存数据仓库
 * @param rackRepository 货架数据仓库
 * @param productRepository 品牌/产品数据仓库
 * @param periodRepository 使用时段数据仓库
 * @param transactionRepository 交易数据仓库
 * @param quickRepository 快速记账数据仓库
 */
class AddStockViewModel(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository,
    private val rackRepository: RackRepository,
    private val productRepository: ProductRepository,
    private val periodRepository: PeriodRepository,
    private val transactionRepository: TransactionRepository,
    private val quickRepository: QuickRepository
) : ViewModel() {

    // 从SavedStateHandle中获取传入的图片路径参数
    private val imageUri: String = savedStateHandle["imagePath"] ?: ""

    // 私有可变状态流，用于保存UI状态
    private val _uiState = MutableStateFlow(AddStockUiState())

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
            // 初始化时设置图片URI
            _uiState.update { it.copy(imageUri = imageUri.toUri()) }
            // 加载货架列表
            racks = rackRepository.getItems()
            // 加载使用时段列表
            periods = periodRepository.getTypes()
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
     */
    fun updateSheetType(type: ShowBottomSheetType) {
        // 选择分类弹窗前需要先选择货架
        if ((type == ShowBottomSheetType.CATEGORY || type == ShowBottomSheetType.STOCK_CATEGORY) && rackEntity.value == null) {
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
     * 同时将使用状态更新为"使用中"
     *
     * @param date 开封日期时间戳
     */
    fun updateOpenDate(date: Long) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(openDate = date, useStatus = StockUseStatus.USING.code)
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
     * 更新图片URI
     *
     * @param imageUri 图片URI
     */
    fun updateImageUrl(imageUri: Uri) {
        _uiState.update { it.copy(imageUri = imageUri) }
    }

    /**
     * 检查基本参数是否有效
     *
     * @return 参数是否有效
     */
    fun checkParams(): Boolean {
        val entity = uiState.value.stockEntity
        if (entity.name.isEmpty()) {
            Toaster.show("请输入名称")
            return false
        } else if (entity.rackId == 0) {
            Toaster.show("请选择货架")
            return false
        } else if (entity.subCategoryId == 0) {
            Toaster.show("请选择类别")
            return false
        }
        return true
    }

    /**
     * 检查账单相关参数是否有效
     *
     * @return 账单参数是否有效
     */
    fun checkBookParams(): Boolean {
        val entity = uiState.value.stockEntity
        if (entity.price.isEmpty() || entity.price.toFloat() <= 0) {
            Toaster.show("请输入价格")
            return false
        }
        if (entity.buyDate <= 0) {
            Toaster.show("请选择购入时间")
            return false
        }
        return true
    }

    /**
     * 保存快速记账实体到数据库
     * 用于将库存购买记录同步到记账模块
     */
    fun saveQuickEntityDatabase() {
        viewModelScope.launch {
            val entity = uiState.value.stockEntity
            val subCategory = transactionRepository.getSubItemByName("购物", 1)
            val quickEntity = QuickEntity(
                date = entity.buyDate,
                price = entity.price,
                categoryId = 1,
                bookId = UserCache.bookId,
                categoryType = TransactionAmountType.EXPENSE.ordinal,
                subCategoryId = subCategory?.id,
            )
            LogUtils.d("saveQuickEntityDatabase", quickEntity)
            quickRepository.insert(quickEntity)
        }
    }

    /**
     * 保存库存实体到数据库
     *
     * @param context 上下文，用于图片文件保存
     * @param onResult 保存结果回调，参数为是否保存成功
     */
    fun saveStockEntityDatabase(context: Context, onResult: (Boolean) -> Unit) {
        val entity = uiState.value.stockEntity
        if (checkParams()) {
            viewModelScope.launch {
                // 保存图片到文件系统
                val imageFile = saveUriToFilesDir(context, uiState.value.imageUri!!)
                // 插入库存记录到数据库
                stockRepository.insert(
                    entity.copy(
                        imageLocalPath = imageFile?.absolutePath ?: "", createDate = System.currentTimeMillis()
                    )
                )
                onResult(true)
            }
        }
    }
}