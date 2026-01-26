package com.fanda.homebook.closet.viewmodel

import android.content.Context
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
import com.fanda.homebook.data.closet.AddClosetEntity
import com.fanda.homebook.data.closet.ClosetGridItem
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
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
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

class AddClosetViewModel(
    savedStateHandle: SavedStateHandle,
    private val colorTypeRepository: ColorTypeRepository,
    private val closetRepository: ClosetRepository,
    private val seasonRepository: SeasonRepository,
    private val productRepository: ProductRepository,
    private val sizeRepository: SizeRepository,
    private val ownerRepository: OwnerRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val imageUri: String = savedStateHandle["imagePath"] ?: ""

    // 私有可变对象，用于保存UI状态
    private val _addClosetUiState = MutableStateFlow(AddClosetUiState())

    // 公开只读对象，用于读取UI状态
    val addClosetUiState = _addClosetUiState.asStateFlow()

    // 当前选中的颜色
    @OptIn(ExperimentalCoroutinesApi::class) val colorType: StateFlow<ColorTypeEntity?> = _addClosetUiState.map { it.closetEntity.colorTypeId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（id）变化，就取消之前的 getItemById 流，启动新的
            colorTypeRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的季节
    @OptIn(ExperimentalCoroutinesApi::class) val season: StateFlow<SeasonEntity?> = _addClosetUiState.map { it.closetEntity.seasonId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游变化，就取消之前的 getItemById 流，启动新的
            seasonRepository.getSeasonById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的产品
    @OptIn(ExperimentalCoroutinesApi::class) val product: StateFlow<ProductEntity?> = _addClosetUiState.map { it.closetEntity.productId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            productRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的尺码
    @OptIn(ExperimentalCoroutinesApi::class) val size: StateFlow<SizeEntity?> = _addClosetUiState.map { it.closetEntity.sizeId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            sizeRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的归属
    @OptIn(ExperimentalCoroutinesApi::class) val owner: StateFlow<OwnerEntity?> = _addClosetUiState.map { it.closetEntity.ownerId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            ownerRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的一级分类
    @OptIn(ExperimentalCoroutinesApi::class) val category: StateFlow<CategoryEntity?> = _addClosetUiState.map { it.closetEntity.categoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
            categoryRepository.getItemById(id)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的二级分类
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<SubCategoryEntity?> = _addClosetUiState.map { it.closetEntity.subCategoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->     // 每当上游（colorTypeId）变化，就取消之前的 getItemById 流，启动新的
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

    // 分类列表
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

    init {
        viewModelScope.launch {
            _addClosetUiState.update { it.copy(imageUri = imageUri.toUri()) }
            seasons = seasonRepository.getSeasons()
            owners = ownerRepository.getItems()
        }
    }

    fun updateClosetColor(colorType: ColorTypeEntity?) {
        colorType?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(colorTypeId = colorType.id)
                )
            }
        }
    }

    fun updateClosetSeason(season: SeasonEntity?) {
        season?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(seasonId = season.id)
                )
            }
        }
    }

    fun updateClosetProduct(productEntity: ProductEntity?) {
        productEntity?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(productId = productEntity.id)
                )
            }
        }
    }

    fun updateClosetSize(sizeEntity: SizeEntity?) {
        sizeEntity?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(sizeId = sizeEntity.id)
                )
            }
        }
    }

    fun updateClosetOwner(ownerEntity: OwnerEntity?) {
        ownerEntity?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(ownerId = ownerEntity.id)
                )
            }
        }
    }

    fun updateClosetDate(date: Long) {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(date = date)
            )
        }
    }

    fun updateClosetComment(comment: String) {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(comment = comment)
            )
        }
    }

    fun plusClosetWearCount() {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(wearCount = it.closetEntity.wearCount + 1)
            )
        }
    }

    fun updateClosetSyncBook(syncBook: Boolean) {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(syncBook = syncBook)
            )
        }
    }

    fun updateClosetPrice(price: String) {
        _addClosetUiState.update {
            it.copy(
                closetEntity = it.closetEntity.copy(price = price)
            )
        }
    }

    fun updateSheetType(type: ShowBottomSheetType) {
        _addClosetUiState.update { it.copy(sheetType = type) }
    }

    fun showBottomSheet(type: ShowBottomSheetType) = _addClosetUiState.value.sheetType == type

    fun dismissBottomSheet() {
        _addClosetUiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    fun saveClosetEntityDatabase(context: Context, onResult: (Boolean) -> Unit) {
        val entity = addClosetUiState.value.closetEntity
        if (entity.ownerId == 0) {
            Toaster.show("请选择归属")
        } else if (entity.price.isEmpty()) {
            Toaster.show("请输入价格")
        } else if (entity.categoryId == 0) {
            Toaster.show("请选择分类")
        } else if (entity.colorTypeId == 0) {
            Toaster.show("请选择颜色")
        } else if (entity.seasonId == 0) {
            Toaster.show("请选择季节")
        } else if (entity.sizeId == 0) {
            Toaster.show("请选择尺码")
        } else if (entity.comment.isEmpty()) {
            Toaster.show("请输入备注")
        } else {
            viewModelScope.launch {
                val imageFile = saveUriToFilesDir(context, addClosetUiState.value.imageUri!!)
                if (imageFile != null) {
                    closetRepository.insert(entity.copy(imageLocalPath = imageFile.absolutePath))
                    Toaster.show("保存成功")
                    onResult(true)
                } else {
                    Toaster.show("保存失败")
                }
            }
        }

    }

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

}