package com.fanda.homebook.closet.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.AddClosetUiState
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.CategoryRepository
import com.fanda.homebook.data.category.CategoryWithSubCategories
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.color.ColorTypeRepository
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.owner.OwnerRepository
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.product.ProductRepository
import com.fanda.homebook.data.quick.QuickEntity
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.data.season.ClosetSeasonRelation
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.season.SeasonRepository
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.data.size.SizeRepository
import com.fanda.homebook.data.transaction.TransactionRepository
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.entity.TransactionAmountType
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
import kotlin.collections.find

/**
 * 添加衣橱视图模型
 *
 * 负责管理添加衣橱物品的业务逻辑和状态
 */
class AddClosetViewModel(
    savedStateHandle: SavedStateHandle,                          // 保存状态句柄
    private val colorTypeRepository: ColorTypeRepository,        // 颜色仓库
    private val closetRepository: ClosetRepository,              // 衣橱仓库
    private val seasonRepository: SeasonRepository,              // 季节仓库
    private val productRepository: ProductRepository,            // 产品仓库
    private val sizeRepository: SizeRepository,                  // 尺寸仓库
    private val ownerRepository: OwnerRepository,                // 归属仓库
    private val categoryRepository: CategoryRepository,          // 分类仓库
    private val quickRepository: QuickRepository,                // 快捷记录仓库
    private val transactionRepository: TransactionRepository     // 交易仓库
) : ViewModel() {

    // 从保存状态中获取图片路径
    private val imageUri: String = savedStateHandle["imagePath"] ?: ""
    private val categoryId: Int = savedStateHandle["categoryId"] ?: -1

    // 私有可变状态流
    private val _addClosetUiState = MutableStateFlow(AddClosetUiState())

    // 公开只读状态流
    val addClosetUiState = _addClosetUiState.asStateFlow()

    // 当前选中的颜色
    @OptIn(ExperimentalCoroutinesApi::class)
    val colorType: StateFlow<ColorTypeEntity?> =
        _addClosetUiState.map { it.closetEntity.colorTypeId }
            .distinctUntilChanged()              // 避免重复ID触发
            .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
                colorTypeRepository.getItemById(id ?: 0)
            }.stateIn(
                scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
            )

    // 当前选中的季节列表
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectSeasons: StateFlow<List<SeasonEntity>?> =
        _addClosetUiState.map { it.seasonIds }.distinctUntilChanged()              // 避免重复ID触发
            .flatMapLatest { ids ->              // 当上游变化时，取消之前的流，启动新的
                seasonRepository.getSeasonsByIdsFlow(ids)
            }.stateIn(
                scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
            )

    // 当前选中的产品
    @OptIn(ExperimentalCoroutinesApi::class)
    val product: StateFlow<ProductEntity?> = _addClosetUiState.map { it.closetEntity.productId }
        .distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
            productRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的尺码
    @OptIn(ExperimentalCoroutinesApi::class)
    val size: StateFlow<SizeEntity?> = _addClosetUiState.map { it.closetEntity.sizeId }
        .distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
            sizeRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的归属
    @OptIn(ExperimentalCoroutinesApi::class)
    val owner: StateFlow<OwnerEntity?> = _addClosetUiState.map { it.closetEntity.ownerId }
        .distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
            ownerRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的一级分类
    @OptIn(ExperimentalCoroutinesApi::class)
    val category: StateFlow<CategoryEntity?> = _addClosetUiState.map { it.closetEntity.categoryId }
        .distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
            categoryRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的二级分类
    @OptIn(ExperimentalCoroutinesApi::class)
    val subCategory: StateFlow<SubCategoryEntity?> =
        _addClosetUiState.map { it.closetEntity.subCategoryId }
            .distinctUntilChanged()              // 避免重复ID触发
            .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
                categoryRepository.getSubItemById(id ?: -1)
            }.stateIn(
                scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
            )

    // 颜色列表
    val colorTypes: StateFlow<List<ColorTypeEntity>> = colorTypeRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 品牌列表
    val products: StateFlow<List<ProductEntity>> = productRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 分类列表（包含子分类）
    val categories: StateFlow<List<CategoryWithSubCategories>> =
        categoryRepository.getAllItemsWithSub().stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
        )

    // 尺码列表
    val sizes: StateFlow<List<SizeEntity>> = sizeRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 季节列表
    var seasons by mutableStateOf(emptyList<SeasonEntity>())
        private set

    // 归属列表
    var owners by mutableStateOf(emptyList<OwnerEntity>())
        private set

    init {
        // 初始化：加载图片和基础数据
        viewModelScope.launch {
            _addClosetUiState.update {
                it.copy(
                    imageUri = imageUri.toUri(),
                    closetEntity = it.closetEntity.copy(
                        ownerId = UserCache.ownerId,
                        categoryId = categoryId
                    )
                )
            }
            seasons = seasonRepository.getSeasons()
            owners = ownerRepository.getItems()
        }
    }

    /**
     * 获取季节描述字符串
     *
     * @param seasons 季节列表
     * @return 格式化后的季节字符串（如"春夏秋冬"）
     */
    fun getSeasonDes(seasons: List<SeasonEntity>?) = if (seasons.isNullOrEmpty()) {
        ""
    } else {
        seasons.sortedBy { it.id }.joinToString { it.name.replace("季", "") }
    }

    /**
     * 更新衣橱颜色
     *
     * @param colorType 颜色实体
     */
    fun updateClosetColor(colorType: ColorTypeEntity?) {
        colorType?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(colorTypeId = colorType.id)
                )
            }
        }
    }

    /**
     * 更新衣橱季节
     *
     * @param seasons 季节列表
     */
    fun updateClosetSeason(seasons: List<SeasonEntity>?) {
        seasons?.let {
            _addClosetUiState.update { state ->
                state.copy(seasonIds = seasons.map { it.id })
            }
        }
    }

    /**
     * 更新衣橱产品
     *
     * @param productEntity 产品实体
     */
    fun updateClosetProduct(productEntity: ProductEntity?) {
        productEntity?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(productId = productEntity.id)
                )
            }
        }
    }

    /**
     * 更新衣橱尺码
     *
     * @param sizeEntity 尺码实体
     */
    fun updateClosetSize(sizeEntity: SizeEntity?) {
        sizeEntity?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(sizeId = sizeEntity.id)
                )
            }
        }
    }

    /**
     * 更新衣橱归属
     *
     * @param ownerEntity 归属实体
     */
    fun updateClosetOwner(ownerEntity: OwnerEntity?) {
        ownerEntity?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(ownerId = ownerEntity.id)
                )
            }
        }
    }

    /**
     * 更新衣橱购买日期
     *
     * @param date 购买日期（时间戳）
     */
    fun updateClosetDate(date: Long) {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(date = date)
            )
        }
    }

    /**
     * 更新衣橱备注
     *
     * @param comment 备注内容
     */
    fun updateClosetComment(comment: String) {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(comment = comment)
            )
        }
    }

    /**
     * 更新同步到账单状态
     *
     * @param syncBook 是否同步到账单
     */
    fun updateClosetSyncBook(syncBook: Boolean) {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(syncBook = syncBook)
            )
        }
    }

    /**
     * 更新衣橱价格
     *
     * @param price 价格字符串
     */
    fun updateClosetPrice(price: String) {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(price = price)
            )
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
     * 检查是否显示指定类型的底部弹窗
     *
     * @param type 弹窗类型
     * @return 是否显示
     */
    fun showBottomSheet(type: ShowBottomSheetType) = _addClosetUiState.value.sheetType == type

    /**
     * 关闭底部弹窗
     */
    fun dismissBottomSheet() {
        _addClosetUiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    /**
     * 检查必填参数
     *
     * @return 参数是否有效
     */
    fun checkParams(): Boolean {
        val entity = addClosetUiState.value.closetEntity
        if (entity.ownerId == 0) {
            Toaster.show("请选择归属")
            return false
        }
        // 确认按钮点击逻辑
        if (category.value != null && subCategory.value == null && categories.value.find { it.category.id == category.value?.id && it.subCategories.isNotEmpty() } != null) {
            // 一级分类有子分类但未选择子分类时提示
            Toaster.show("请选择子分类")
            return false
        }
        return true
    }

    /**
     * 检查账单相关参数
     *
     * @return 参数是否有效
     */
    fun checkBookParams(): Boolean {
        val entity = addClosetUiState.value.closetEntity
        if (entity.price.isEmpty() || entity.price.toFloat() <= 0) {
            Toaster.show("请输入价格")
            return false
        }
        if (entity.date <= 0) {
            Toaster.show("请选择购入时间")
            return false
        }
        return true
    }

    /**
     * 保存衣橱实体到数据库
     *
     * @param context 上下文
     * @param onResult 保存结果回调
     */
    fun saveClosetEntityDatabase(context: Context, onResult: (Boolean) -> Unit) {
        val entity = addClosetUiState.value.closetEntity
        if (checkParams()) {
            viewModelScope.launch {
                // 保存图片到本地文件
                val imageFile = saveUriToFilesDir(context, addClosetUiState.value.imageUri!!)
                // 插入衣橱记录
                val closetId = closetRepository.insert(
                    entity.copy(
                        imageLocalPath = imageFile?.absolutePath ?: "",
                        createDate = System.currentTimeMillis(),
                        categoryId = entity.categoryId?.let { if (it <= 0L) null else entity.categoryId }
                    )
                )
                // 插入季节关联关系
                val closetSeasonRelationList = if (addClosetUiState.value.seasonIds.isNotEmpty()) {
                    addClosetUiState.value.seasonIds.map { seasonId ->
                        ClosetSeasonRelation(closetId.toInt(), seasonId)
                    }
                } else {
                    emptyList()
                }
                seasonRepository.insertSeasonRelationAll(closetSeasonRelationList)
                onResult(true)
            }
        }
    }

    /**
     * 保存快捷记录到数据库（用于账单同步）
     */
    fun saveQuickEntityDatabase() {
        viewModelScope.launch {
            val entity = addClosetUiState.value.closetEntity
            // 获取"服饰"子分类
            val subCategory = transactionRepository.getSubItemByName("服饰", 1)
            // 创建快捷记录实体
            val quickEntity = QuickEntity(
                date = entity.date,
                price = entity.price,
                categoryId = 1,  // 支出类别ID
                bookId = UserCache.bookId,
                categoryType = TransactionAmountType.EXPENSE.ordinal,  // 支出类型
                subCategoryId = subCategory?.id,
            )
            LogUtils.d("saveQuickEntityDatabase", quickEntity)
            // 插入快捷记录
            quickRepository.insert(quickEntity)
        }
    }

    /**
     * 更新选中的分类
     *
     * @param categoryEntity 一级分类实体
     * @param subCategoryEntity 二级分类实体
     */
    fun updateSelectedCategory(
        categoryEntity: CategoryEntity?, subCategoryEntity: SubCategoryEntity?
    ) {
        categoryEntity?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(
                        categoryId = categoryEntity.id,
                    )
                )
            }
        }
        // 如果没有子分类，则将子分类ID设置为null
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(subCategoryId = subCategoryEntity?.id)
            )
        }
    }

    /**
     * 更新图片Uri
     *
     * @param imageUri 图片Uri
     */
    fun updateImageUrl(imageUri: Uri) {
        _addClosetUiState.update { it.copy(imageUri = imageUri) }
    }
}