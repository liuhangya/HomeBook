package com.fanda.homebook.book.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.book.state.DashboardDetailUiState
import com.fanda.homebook.book.state.QueryWay
import com.fanda.homebook.data.quick.QuickEntity
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.UserCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 仪表板详情页面ViewModel
 * 管理仪表板详情页的数据状态和业务逻辑
 *
 * @param savedStateHandle 用于保存和恢复状态的句柄
 * @param quickRepository 快速记账数据仓库
 */
class DashboardDetailViewModel(
    savedStateHandle: SavedStateHandle, val quickRepository: QuickRepository
) : ViewModel() {

    // 从导航参数中获取页面标题，默认为空字符串
    private val title = savedStateHandle.get<String>("title") ?: ""

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(DashboardDetailUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    init {
        // 初始化UI状态，设置标题和从缓存中获取数据
        _uiState.update {
            it.copy(
                title = title,                    // 设置页面标题
                data = UserCache.categoryQuickList  // 从用户缓存中获取分类交易列表
            )
        }
    }

    /**
     * 刷新指定交易记录
     * 当交易记录被编辑后调用，更新本地缓存中的数据
     *
     * @param quickId 要刷新的交易记录ID
     */
    fun refresh(quickId: Int) {
        LogUtils.d("刷新数据...: $quickId")
        if (quickId <= 0) return

        viewModelScope.launch {
            // 从数据库获取更新后的交易记录
            val quickEntity = quickRepository.getQuickById(quickId)

            // 更新本地数据：用新的交易记录替换旧的
            val updatedData = uiState.value.data.map { existingItem ->
                if (existingItem.quick.id == quickEntity.quick.id) {
                    quickEntity  // 找到匹配的记录，替换为新的对象
                } else {
                    existingItem  // 不是目标记录，保持不变
                }
            }

            // 更新UI状态
            _uiState.update {
                it.copy(data = updatedData)
            }
        }
    }

    /**
     * 删除交易记录
     * 从数据库删除记录，并从本地列表中移除
     *
     * @param quick 要删除的交易记录
     */
    fun deleteQuickDatabase(quick: QuickEntity) {
        viewModelScope.launch {
            // 从数据库删除记录
            quickRepository.delete(quick)

            // 从本地列表中过滤掉已删除的记录
            val updatedData = uiState.value.data.filter { it.quick.id != quick.id }

            // 更新UI状态
            _uiState.update {
                it.copy(data = updatedData)
            }
        }
    }

    /**
     * 更新查询方式
     * 切换数据显示方式（按金额或按时间）
     *
     * @param queryWay 查询方式枚举值
     */
    fun updateQueryWay(queryWay: QueryWay) {
        _uiState.update {
            it.copy(queryWay = queryWay)
        }
    }
}