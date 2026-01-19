package com.fanda.homebook.closet.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanda.homebook.closet.state.AddClosetUiState
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.color.ColorTypeRepository
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.season.SeasonRepository
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.TIMEOUT_MILLIS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddClosetViewModel(
    savedStateHandle: SavedStateHandle,
    private val colorTypeRepository: ColorTypeRepository,
    closetRepository: ClosetRepository,
    seasonRepository: SeasonRepository
) : ViewModel() {

    init {
//        LogUtils.i("AddClosetViewModel init")
//        viewModelScope.launch {
//            if (colorTypeRepository.getCount() == 0) {
//                closetRepository.insert(ClosetEntity(name = "默认"))
//            }
//        }
    }

    // 私有可变对象，用于保存UI状态
    private val _addClosetUiState = MutableStateFlow(AddClosetUiState())

    // 公开只读对象，用于读取UI状态
    val addClosetUiState = _addClosetUiState.asStateFlow()

    // 颜色列表
    val colorTypes: StateFlow<List<ColorTypeEntity>> = colorTypeRepository.getColorTypes().stateIn(
        scope = viewModelScope,
        SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        emptyList()
    )

    var seasons by mutableStateOf(emptyList<SeasonEntity>())
        private set

    init {
        viewModelScope.launch {
            seasons = seasonRepository.getSeasons()
        }
    }

    fun updateClosetColor(colorType: ColorTypeEntity?) {
        colorType?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(colorTypeId = colorType.id),
                    colorType = colorType
                )
            }
        }

    }

    fun updateClosetSeason(season: SeasonEntity?) {
        season?.let {
            _addClosetUiState.update {
                it.copy(
                    closetEntity = it.closetEntity.copy(seasonId = season.id),
                    season = season
                )
            }
        }
    }
}