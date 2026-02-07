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
import com.fanda.homebook.quick.state.WatchAndEditQuickUiState
import com.fanda.homebook.tools.TIMEOUT_MILLIS
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
 * 查看和编辑记账ViewModel
 * 管理记账记录查看和编辑页面的数据状态和业务逻辑
 *
 * @param savedStateHandle 用于保存和恢复状态的句柄
 * @param transactionRepository 交易分类数据仓库
 * @param payWayRepository 支付方式数据仓库
 * @param quickRepository 快速记账数据仓库
 */
class WatchAndEditQuickViewModel(
    val savedStateHandle: SavedStateHandle, private val transactionRepository: TransactionRepository, private val payWayRepository: PayWayRepository, private val quickRepository: QuickRepository
) : ViewModel() {

    // 从导航参数获取要查看/编辑的记账ID
    private val quickId: Int = savedStateHandle["quickId"] ?: -1

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(WatchAndEditQuickUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    init {
        // 初始化时根据quickId加载记账数据
        viewModelScope.launch {
            val item = quickRepository.getQuickById(quickId)
            _uiState.update {
                it.copy(
                    quickEntity = item.quick,  // 更新UI状态中的记账实体
                )
            }
        }
    }

    // 当前选中的一级分类（主分类）
    @OptIn(ExperimentalCoroutinesApi::class) val category: StateFlow<TransactionEntity?> = _uiState.map { it.quickEntity.categoryId }.distinctUntilChanged()              // 避免重复ID触发
        .flatMapLatest { id ->
            transactionRepository.getItemById(id)
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
            transactionRepository.getSubItemsById(id)  // 根据主分类ID获取子分类
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
     * 获取同步标题
     *
     * @return 根据同步状态返回相应的标题文本
     */
    fun getSyncTitle(): String {
        val entity = _uiState.value.quickEntity
        return when {
            entity.syncCloset -> "同步到衣橱"
            entity.syncStock -> "同步到囤货"
            else -> ""
        }
    }

    /**
     * 检查是否有任何同步
     *
     * @return 如果同步到衣橱或同步到囤货任意一项为true则返回true
     */
    fun isHasAnySync() = _uiState.value.quickEntity.syncCloset || _uiState.value.quickEntity.syncStock

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
     * 更新记账实体到数据库
     *
     * @param onResult 更新结果回调，true表示成功，false表示失败
     */
    fun updateQuickEntityDatabase(onResult: (Boolean) -> Unit) {
        if (checkParams()) {
            viewModelScope.launch {
                quickRepository.update(_uiState.value.quickEntity)
                onResult(true)
            }
        }
    }
}