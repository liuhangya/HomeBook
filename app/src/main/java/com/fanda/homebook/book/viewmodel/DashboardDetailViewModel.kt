package com.fanda.homebook.book.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.fanda.homebook.book.state.DashboardDetailUiState
import com.fanda.homebook.book.state.QueryWay
import com.fanda.homebook.tools.UserCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DashboardDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

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


    fun updateQueryWay(queryWay: QueryWay) {
        _uiState.update {
            it.copy(queryWay = queryWay)
        }
    }

}