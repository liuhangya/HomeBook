package com.fanda.homebook.quick.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.data.pay.PayWayEntity
import com.fanda.homebook.data.pay.PayWayRepository
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.data.transaction.TransactionEntity
import com.fanda.homebook.data.transaction.TransactionRepository
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType
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

/**
 * 快速记账ViewModel
 * 管理快速记账页面的数据状态和业务逻辑
 *
 * @param savedStateHandle 用于保存和恢复状态的句柄
 * @param transactionRepository 交易分类数据仓库
 * @param payWayRepository 支付方式数据仓库
 * @param quickRepository 快速记账数据仓库
 */
class AddQuickViewModel(
    savedStateHandle: SavedStateHandle, private val transactionRepository: TransactionRepository, private val payWayRepository: PayWayRepository, private val quickRepository: QuickRepository
) : ViewModel() {

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(AddQuickUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    // 当前选中的一级分类（主分类）
    @OptIn(ExperimentalCoroutinesApi::class) val category: StateFlow<TransactionEntity?> = _uiState.map { it.quickEntity.categoryId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->
            transactionRepository.getItemById(id ?: 0)  // 如果为null则传0
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 当前选中的二级分类（子分类）
    @OptIn(ExperimentalCoroutinesApi::class) val subCategory: StateFlow<TransactionSubEntity?> = _uiState.map { it.quickEntity.subCategoryId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->
            transactionRepository.getSubItemById(id ?: -1)  // 如果为null则传-1
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 子分类列表（根据选中的主分类动态变化）
    @OptIn(ExperimentalCoroutinesApi::class) val subCategories: StateFlow<List<TransactionSubEntity>?> = _uiState.map { it.quickEntity.categoryId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->
            transactionRepository.getSubItemsById(id ?: 0)  // 根据主分类ID获取子分类
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
        )

    // 当前选中的支付方式
    @OptIn(ExperimentalCoroutinesApi::class) val payWay: StateFlow<PayWayEntity?> = _uiState.map { it.quickEntity.payWayId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->
            payWayRepository.getItemById(id ?: 0)  // 如果为null则传0
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = null
        )

    // 主分类列表
    val categories: StateFlow<List<TransactionEntity>> = transactionRepository.getItems().stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    // 支付方式列表
    val payWays: StateFlow<List<PayWayEntity>> = payWayRepository.getItems().stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = emptyList()
    )

    /**
     * 更新选中的主分类
     *
     * @param categoryEntity 主分类实体
     */
    fun updateCategory(categoryEntity: TransactionEntity?) {
        categoryEntity?.let {
            _uiState.update { currentState ->
                currentState.copy(
                    quickEntity = currentState.quickEntity.copy(
                        categoryId = categoryEntity.id,        // 更新主分类ID
                        categoryType = categoryEntity.type,    // 更新分类类型
                        subCategoryId = null                   // 清空子分类ID
                    )
                )
            }
        }
    }

    /**
     * 更新选中的子分类
     *
     * @param subCategoryEntity 子分类实体
     */
    fun updateSubCategory(subCategoryEntity: TransactionSubEntity?) {
        _uiState.update { currentState ->
            currentState.copy(
                quickEntity = currentState.quickEntity.copy(subCategoryId = subCategoryEntity?.id)
            )
        }
    }

    /**
     * 更新交易日期
     *
     * @param date 日期时间戳，null时使用当前时间
     */
    fun updateDate(date: Long?) {
        _uiState.update { currentState ->
            currentState.copy(
                quickEntity = currentState.quickEntity.copy(date = date ?: System.currentTimeMillis())
            )
        }
    }

    /**
     * 更新交易金额
     *
     * @param price 金额字符串
     */
    fun updatePrice(price: String) {
        _uiState.update { currentState ->
            currentState.copy(
                quickEntity = currentState.quickEntity.copy(price = price)
            )
        }
    }

    /**
     * 更新交易备注
     *
     * @param comment 备注文本
     */
    fun updateQuickComment(comment: String) {
        _uiState.update { currentState ->
            currentState.copy(
                quickEntity = currentState.quickEntity.copy(quickComment = comment)
            )
        }
    }

    /**
     * 更新支付方式
     *
     * @param payWayEntity 支付方式实体
     */
    fun updatePayWay(payWayEntity: PayWayEntity?) {
        payWayEntity?.let {
            _uiState.update { currentState ->
                currentState.copy(
                    quickEntity = currentState.quickEntity.copy(payWayId = payWayEntity.id)
                )
            }
        }
    }

    /**
     * 更新同步到衣橱状态
     * 与同步到囤货状态互斥
     *
     * @param sync 是否同步到衣橱
     */
    fun updateSyncCloset(sync: Boolean) {
        _uiState.update { currentState ->
            var updatedEntity = currentState.quickEntity.copy(syncCloset = sync)
            // 如果开启同步到衣橱，则关闭同步到囤货（互斥）
            if (currentState.quickEntity.syncStock && sync) {
                updatedEntity = updatedEntity.copy(syncStock = false)
            }
            currentState.copy(quickEntity = updatedEntity)
        }
    }

    /**
     * 更新同步到囤货状态
     * 与同步到衣橱状态互斥
     *
     * @param sync 是否同步到囤货
     */
    fun updateSyncStock(sync: Boolean) {
        _uiState.update { currentState ->
            var updatedEntity = currentState.quickEntity.copy(syncStock = sync)
            // 如果开启同步到囤货，则关闭同步到衣橱（互斥）
            if (currentState.quickEntity.syncCloset && sync) {
                updatedEntity = updatedEntity.copy(syncCloset = false)
            }
            currentState.copy(quickEntity = updatedEntity)
        }
    }

    /**
     * 更新底部弹窗类型
     *
     * @param type 弹窗类型
     */
    fun updateSheetType(type: ShowBottomSheetType) {
        _uiState.update { it.copy(sheetType = type) }
    }

    /**
     * 检查是否显示指定类型的底部弹窗
     *
     * @param type 弹窗类型
     * @return 如果当前显示的弹窗类型匹配则返回true
     */
    fun showBottomSheet(type: ShowBottomSheetType) = _uiState.value.sheetType == type

    /**
     * 关闭底部弹窗
     */
    fun dismissBottomSheet() {
        _uiState.update { it.copy(sheetType = ShowBottomSheetType.NONE) }
    }

    /**
     * 检查参数是否有效
     *
     * @return 如果所有必填参数都有效返回true，否则返回false并显示提示
     */
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

    /**
     * 保存记账实体到数据库
     *
     * @param onResult 保存结果回调，true表示成功，false表示失败
     */
    fun saveQuickEntityDatabase(onResult: (Boolean) -> Unit) {
        // 从用户缓存获取账本ID，并创建完整的记账实体
        val entity = _uiState.value.quickEntity.copy(bookId = UserCache.bookId)

        if (checkParams()) {
            viewModelScope.launch {
                // 插入到数据库
                quickRepository.insert(entity)
                onResult(true)
            }
        }
    }
}