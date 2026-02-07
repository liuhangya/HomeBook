package com.fanda.homebook.quick.state

import com.fanda.homebook.data.quick.QuickEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 查看和编辑记账页面UI状态数据类
 * 管理记账记录查看和编辑页面的所有UI状态
 *
 * @property quickEntity 当前正在查看或编辑的记账实体，默认包含分类ID为1
 * @property sheetType 当前显示的底部弹窗类型，默认NONE表示不显示
 */
data class WatchAndEditQuickUiState(
    // 记账实体状态
    val quickEntity: QuickEntity = QuickEntity(categoryId = 1),

    // 弹窗显示状态
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE
)