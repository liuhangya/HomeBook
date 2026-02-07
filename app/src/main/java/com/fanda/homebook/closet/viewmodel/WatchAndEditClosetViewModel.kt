package com.fanda.homebook.closet.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.WatchAndEditClosetUiState
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
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.season.SeasonRepository
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.data.size.SizeRepository
import com.fanda.homebook.common.entity.ShowBottomSheetType
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
 * 查看与编辑衣橱视图模型
 *
 * 负责管理查看和编辑衣橱物品的业务逻辑和状态
 */
class WatchAndEditClosetViewModel(
    savedStateHandle: SavedStateHandle,                          // 保存状态句柄
    private val colorTypeRepository: ColorTypeRepository,        // 颜色仓库
    private val closetRepository: ClosetRepository,              // 衣橱仓库
    private val seasonRepository: SeasonRepository,              // 季节仓库
    private val productRepository: ProductRepository,            // 产品仓库
    private val sizeRepository: SizeRepository,                  // 尺寸仓库
    private val ownerRepository: OwnerRepository,                // 归属仓库
    private val categoryRepository: CategoryRepository           // 分类仓库
) : ViewModel() {

    // 从保存状态中获取衣橱ID
    private val closetId: Int = savedStateHandle["closetId"] ?: -1

    // 私有可变状态流
    private val _addClosetUiState = MutableStateFlow(WatchAndEditClosetUiState())

    // 公开只读状态流
    val addClosetUiState = _addClosetUiState.asStateFlow()

    init {
        // 初始化：加载基础数据和衣橱详情
        viewModelScope.launch {
            seasons = seasonRepository.getSeasons()
            owners = ownerRepository.getItems()
            // 获取衣橱详情
            val item = closetRepository.getClosetById(UserCache.ownerId, closetId)
            // 获取衣橱的季节关联
            val seasonIds = seasonRepository.getSeasonIdsByClosetId(closetId)
            _addClosetUiState.update {
                it.copy(closetEntity = item.closet, seasonIds = seasonIds)
            }
        }
    }

    // 当前选中的颜色
    @OptIn(ExperimentalCoroutinesApi::class) val colorType: StateFlow<ColorTypeEntity?> = _addClosetUiState.map { it.closetEntity.colorTypeId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
            colorTypeRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的季节列表
    @OptIn(ExperimentalCoroutinesApi::class) val selectSeasons: StateFlow<List<SeasonEntity>?> = _addClosetUiState.map { it.seasonIds }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { ids ->              // 当上游变化时，取消之前的流，启动新的
            seasonRepository.getSeasonsByIdsFlow(ids)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的产品
    @OptIn(ExperimentalCoroutinesApi::class) val product: StateFlow<ProductEntity?> = _addClosetUiState.map { it.closetEntity.productId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
            productRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的尺码
    @OptIn(ExperimentalCoroutinesApi::class) val size: StateFlow<SizeEntity?> = _addClosetUiState.map { it.closetEntity.sizeId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
            sizeRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的归属
    @OptIn(ExperimentalCoroutinesApi::class) val owner: StateFlow<OwnerEntity?> = _addClosetUiState.map { it.closetEntity.ownerId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
            ownerRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的一级分类
    @OptIn(ExperimentalCoroutinesApi::class) val category: StateFlow<CategoryEntity?> = _addClosetUiState.map { it.closetEntity.categoryId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->               // 当上游变化时，取消之前的流，启动新的
            categoryRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的二级分类
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<SubCategoryEntity?> = _addClosetUiState.map { it.closetEntity.subCategoryId }.distinctUntilChanged()              // 避免重复ID触发
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
    val categories: StateFlow<List<CategoryWithSubCategories>> = categoryRepository.getAllItemsWithSub().stateIn(
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
     * 增加穿着次数
     *
     * @param context 上下文，用于保存到数据库
     */
    fun plusClosetWearCount(context: Context) {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(wearCount = it.closetEntity.wearCount + 1)
            )
        }
        // 实时更新数据库
        viewModelScope.launch {
            updateClosetEntityDatabase(context)
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
     * 更新底部弹窗类型（仅编辑模式下可用）
     *
     * @param type 弹窗类型
     */
    fun updateSheetType(type: ShowBottomSheetType) {
        // 编辑状态才允许切换
        if (addClosetUiState.value.isEditState) {
            _addClosetUiState.update { it.copy(sheetType = type) }
        }
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
     * 更新图片Uri
     *
     * @param imageUri 图片Uri
     */
    fun updateImageUrl(imageUri: Uri) {
        _addClosetUiState.update { it.copy(imageUri = imageUri) }
    }

    /**
     * 更新衣橱实体到数据库
     *
     * @param context 上下文，用于保存图片
     * @param onResult 保存结果回调
     */
    fun updateClosetEntityDatabase(context: Context, onResult: (Boolean) -> Unit = {}) {
        val entity = addClosetUiState.value.closetEntity
        if (entity.ownerId == 0) {
            Toaster.show("请选择归属")
        } else {
            viewModelScope.launch {
                // 如果更换了图片，保存新图片到本地
                if (_addClosetUiState.value.imageUri != null) {
                    val imageFile = saveUriToFilesDir(context, _addClosetUiState.value.imageUri!!)
                    closetRepository.update(entity.copy(imageLocalPath = imageFile?.absolutePath ?: ""))
                } else {
                    closetRepository.update(entity)
                }
                // 更新季节关联关系
                seasonRepository.updateSeasonsForCloset(entity.id, _addClosetUiState.value.seasonIds)
                onResult(true)
            }
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
     * 切换衣橱物品到垃圾桶/恢复
     *
     * @param context 上下文，用于保存到数据库
     * @param move 是否移动到垃圾桶（true：移动到垃圾桶，false：恢复）
     */
    fun toggleMoveToTrash(context: Context, move: Boolean) {
        viewModelScope.launch {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(moveToTrash = move),
                )
            }
            // 移动到垃圾桶后退出编辑状态
            if (move) {
                updateEditState(false)
            }
            // 更新数据库
            updateClosetEntityDatabase(context)
        }
    }

    /**
     * 更新编辑状态
     *
     * @param state 是否处于编辑状态
     */
    fun updateEditState(state: Boolean) {
        viewModelScope.launch {
            _addClosetUiState.update {
                it.copy(
                    isEditState = state,
                )
            }
        }
    }
}