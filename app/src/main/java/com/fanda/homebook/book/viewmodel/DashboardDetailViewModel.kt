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

class DashboardDetailViewModel(savedStateHandle: SavedStateHandle, val quickRepository: QuickRepository) : ViewModel() {

    private val title = savedStateHandle.get<String>("title") ?: ""

    // 私有可变对象，用于保存UI状态
    private val _uiState = MutableStateFlow(DashboardDetailUiState())

    // 公开只读对象，用于读取UI状态
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(title = title, data = UserCache.categoryQuickList)
        }
    }

    fun refresh(quickId: Int) {
        LogUtils.d("刷新数据...: $quickId")
        if (quickId <= 0) return
        viewModelScope.launch {
            val quickEntity = quickRepository.getQuickById(quickId)
            val updatedData = uiState.value.data.map { existingItem ->
                if (existingItem.quick.id == quickEntity.quick.id) {
                    quickEntity  // 替换为新的对象
                } else {
                    existingItem  // 保持不变
                }
            }
            _uiState.update {
                it.copy(data = updatedData)
            }
        }
    }

    fun deleteQuickDatabase(quick: QuickEntity) {
        viewModelScope.launch {
            quickRepository.delete(quick)
            val updatedData = uiState.value.data.filter { it.quick.id != quick.id }
            _uiState.update {
                it.copy(data = updatedData)
            }
        }
    }


    fun updateQueryWay(queryWay: QueryWay) {
        _uiState.update {
            it.copy(queryWay = queryWay)
        }
    }

}