package com.fanda.homebook.quick.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.data.pay.PayWayEntity
import com.fanda.homebook.data.pay.PayWayRepository
import com.fanda.homebook.data.quick.LocalQuickRepository
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.data.transaction.TransactionEntity
import com.fanda.homebook.data.transaction.TransactionRepository
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.state.AddQuickUiState
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import com.fanda.homebook.tools.UserCache
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

class AddQuickViewModel(
    savedStateHandle: SavedStateHandle, private val transactionRepository: TransactionRepository, private val payWayRepository: PayWayRepository, private val quickRepository: QuickRepository
) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(AddQuickUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    // 当前选中的一级分类
    @OptIn(ExperimentalCoroutinesApi::class) val category: StateFlow<TransactionEntity?> = _uiState.map { it.quickEntity.categoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->
            transactionRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 当前选中的二级分类
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<TransactionSubEntity?> = _uiState.map { it.quickEntity.subCategoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->
            transactionRepository.getSubItemById(id ?: -1)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 子分类列表
    @OptIn(ExperimentalCoroutinesApi::class) val subCategories: StateFlow<List<TransactionSubEntity>?> = _uiState.map { it.quickEntity.categoryId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->
            transactionRepository.getSubItemsById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
        )

    // 当前选中的支付方式
    @OptIn(ExperimentalCoroutinesApi::class) val payWay: StateFlow<PayWayEntity?> = _uiState.map { it.quickEntity.payWayId }.distinctUntilChanged()              // 避免重复 ID 触发
        .flatMapLatest { id ->
            payWayRepository.getItemById(id ?: 0)
        }.stateIn(
            scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), null
        )

    // 分类列表
    val categories: StateFlow<List<TransactionEntity>> = transactionRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )

    // 支付方式列表
    val payWays: StateFlow<List<PayWayEntity>> = payWayRepository.getItems().stateIn(
        scope = viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList()
    )


    fun updateCategory(
        categoryEntity: TransactionEntity?
    ) {
        categoryEntity?.let {
            _uiState.update {
                it.copy(
                    quickEntity = it.quickEntity.copy(
                        categoryId = categoryEntity.id,
                        categoryType = categoryEntity.type,
                        subCategoryId = null
                    )
                )
            }
        }
    }

    fun updateSubCategory(
        subCategoryEntity: TransactionSubEntity?
    ) {
        // 如果没有子分类，则将子分类ID设置为null
        _uiState.update {
            it.copy(
                quickEntity = it.quickEntity.copy(subCategoryId = subCategoryEntity?.id)
            )
        }
    }

    fun updateDate(date: Long?) {
        _uiState.update {
            it.copy(
                quickEntity = it.quickEntity.copy(date = date ?: System.currentTimeMillis())
            )
        }
    }

    fun updatePrice(price: String) {
        _uiState.update {
            it.copy(
                quickEntity = it.quickEntity.copy(price = price)
            )
        }
    }

    fun updateQuickComment(comment: String) {
        _uiState.update {
            it.copy(
                quickEntity = it.quickEntity.copy(quickComment = comment)
            )
        }
    }

    fun updatePayWay(payWayEntity: PayWayEntity?) {
        payWayEntity?.let {
            _uiState.update {
                it.copy(
                    quickEntity = it.quickEntity.copy(
                        payWayId = payWayEntity.id,
                    )
                )
            }
        }
    }

    fun updateSyncCloset(sync: Boolean) {
        _uiState.update { it.copy(quickEntity = it.quickEntity.copy(syncCloset = sync)) }
        if (_uiState.value.quickEntity.syncStock && sync) {
            _uiState.update { it.copy(quickEntity = it.quickEntity.copy(syncStock = false)) }
        }
    }

    fun updateSyncStock(sync: Boolean) {

        _uiState.update { it.copy(quickEntity = it.quickEntity.copy(syncStock = sync)) }
        if (_uiState.value.quickEntity.syncCloset && sync) {
            _uiState.update { it.copy(quickEntity = it.quickEntity.copy(syncCloset = false)) }
        }
    }

    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    fun checkParams(): Boolean {
        val entity = _uiState.value.quickEntity
        return when {
            entity.price.isEmpty() || entity.price.toDouble() <= 0 -> {
                Toaster.show("请输入金额")
                false
            }

            entity.subCategoryId == null -> {
                Toaster.show("请选择分类")
                false
            }

            entity.payWayId == null -> {
                Toaster.show("请选择支付方式")
                false
            }

            else -> true
        }
    }

    fun saveQuickEntityDatabase(onResult: (Boolean) -> Unit) {
        val entity = _uiState.value.quickEntity.copy(bookId = UserCache.bookId)
        if (checkParams()) {
            viewModelScope.launch {
                quickRepository.insert(entity)
                onResult(true)
            }
        }
    }


}