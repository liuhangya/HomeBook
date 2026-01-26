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
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.period.PeriodEntity
import com.fanda.homebook.data.period.PeriodRepository
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.product.ProductRepository
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackRepository
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.data.stock.StockRepository
import com.fanda.homebook.entity.ShowBottomSheetType
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddStockViewModel(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository,
    private val rackRepository: RackRepository,
    private val productRepository: ProductRepository,
    private val periodRepository: PeriodRepository
) : ViewModel() {

    private val imageUri: String = savedStateHandle["imagePath"] ?: ""

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(AddStockUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    // 货架列表
    var racks by mutableStateOf(emptyList<RackEntity>())
        private set

    // 时段列表
    var periods by mutableStateOf(emptyList<PeriodEntity>())
        private set

    // 品牌列表
    val products: StateFlow<List<ProductEntity>> = productRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(imageUri = imageUri.toUri()) }
            racks = rackRepository.getItems()
            periods = periodRepository.getTypes()
        }
    }

    // 当前选中的货架的子分类列表
    @OptIn(ExperimentalCoroutinesApi::class)
    val rackSubCategoryList: StateFlow<List<RackSubCategoryEntity>> =
        _uiState.map { it.stockEntity.rackId }.distinctUntilChanged()              // 避免重复 ID 触发
            .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
                rackRepository.getAllSubItemsById(id)
            }.stateIn(
                scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
            )

    // 当前选中的货架
    @OptIn(ExperimentalCoroutinesApi::class)
    val rackEntity: StateFlow<RackEntity?> =
        _uiState.map { it.stockEntity.rackId }.distinctUntilChanged()              // 避免重复 ID 触发
            .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
                rackRepository.getItemById(id)
            }.stateIn(
                scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
            )

    // 当前选中的货架子分类
    @OptIn(ExperimentalCoroutinesApi::class)
    val subCategory: StateFlow<RackSubCategoryEntity?> =
        _uiState.map { it.stockEntity.subCategoryId }
            .distinctUntilChanged()              // 避免重复 ID 触发
            .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
                rackRepository.getSubItemById(id)
            }.stateIn(
                scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
            )

    // 当前选中的产品
    @OptIn(ExperimentalCoroutinesApi::class)
    val product: StateFlow<ProductEntity?> =
        _uiState.map { it.stockEntity.productId }.distinctUntilChanged()              // 避免重复 ID 触发
            .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
                productRepository.getItemById(id)
            }.stateIn(
                scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
            )

    // 当前选中的时段
    @OptIn(ExperimentalCoroutinesApi::class)
    val period: StateFlow<PeriodEntity?> =
        _uiState.map { it.stockEntity.periodId }.distinctUntilChanged()              // 避免重复 ID 触发
            .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
                periodRepository.getTypeById(id)
            }.stateIn(
                scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
            )


    fun updateSheetType(type: ShowBottomSheetType) {
        if (type == ShowBottomSheetType.CATEGORY && rackEntity.value == null) {
            Toaster.show("请先选择货架")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(sheetType = type) }
        }
    }

    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    fun updateStockName(name: String) {
        _uiState.update { it.copy(stockEntity = it.stockEntity.copy(name = name)) }
    }

    fun updateSyncBook(syncBook: Boolean) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(syncBook = syncBook)
            )
        }
    }

    fun updatePrice(price: String) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(price = price)
            )
        }
    }

    fun updateClosetComment(comment: String) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(comment = comment)
            )
        }
    }

    fun updateBuyDate(date: Long) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(buyDate = date)
            )
        }
    }

    fun updateOpenDate(date: Long) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(openDate = date)
            )
        }
    }

    fun updateExpireDate(date: Long) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(expireDate = date)
            )
        }
    }

    fun updateRack(rackEntity: RackEntity?) {
        rackEntity?.let {
            _uiState.update {
                it.copy(
                    stockEntity = it.stockEntity.copy(rackId = rackEntity.id)
                )
            }
        }
    }

    fun updateProduct(productEntity: ProductEntity?) {
        productEntity?.let {
            _uiState.update {
                it.copy(
                    stockEntity = it.stockEntity.copy(productId = productEntity.id)
                )
            }
        }
    }

    fun updateCategory(subCategoryEntity: RackSubCategoryEntity?) {
        subCategoryEntity?.let {
            _uiState.update {
                it.copy(
                    stockEntity = it.stockEntity.copy(subCategoryId = subCategoryEntity.id)
                )
            }
        }
    }

    fun updatePeriod(periodEntity: PeriodEntity?) {
        periodEntity?.let {
            _uiState.update {
                it.copy(
                    stockEntity = it.stockEntity.copy(periodId = periodEntity.id)
                )
            }
        }
    }

    fun updateShelfMonth(month: Int) {
        _uiState.update {
            it.copy(
                stockEntity = it.stockEntity.copy(shelfMonth = month)
            )
        }
    }

    fun updateImageUrl(imageUri: Uri) {
        _uiState.update { it.copy(imageUri = imageUri) }
    }

    fun saveStockEntityDatabase(context: Context, onResult: (Boolean) -> Unit) {
        val entity = uiState.value.stockEntity
        if (entity.name.isEmpty()) {
            Toaster.show("请输入名称")
        } else if (entity.rackId == 0) {
            Toaster.show("请选择货架")
        } else if (entity.price.isEmpty()) {
            Toaster.show("请输入价格")
        } else if (entity.productId == 0) {
            Toaster.show("请选择品牌")
        } else if (entity.subCategoryId == 0) {
            Toaster.show("请选择类别")
        } else if (entity.periodId == 0) {
            Toaster.show("请选择使用时段")
        } else {
            viewModelScope.launch {
                val imageFile = saveUriToFilesDir(context, uiState.value.imageUri!!)
                if (imageFile != null) {
                    stockRepository.insert(entity.copy(imageLocalPath = imageFile.absolutePath))
                    Toaster.show("保存成功")
                    onResult(true)
                } else {
                    Toaster.show("保存失败")
                }
            }
        }

    }

}