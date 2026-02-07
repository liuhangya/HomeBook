package com.fanda.homebook.closet.state

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.fanda.homebook.data.color.ColorTypeEntity

/**
 * 颜色管理界面状态类
 *
 * 用于管理颜色分类的界面状态
 *
 * @property entity 当前选中的颜色实体对象，可能为空
 * @property renameOrDeleteBottomSheet 是否显示重命名/删除底部弹窗，默认为false
 * @property addEntity 用于添加新颜色的临时实体对象，默认颜色为绿色
 */
data class ColorUiState(
    val entity: ColorTypeEntity? = null,                        // 当前选中的颜色实体
    val renameOrDeleteBottomSheet: Boolean = false,             // 重命名/删除弹窗显示状态
    val addEntity: ColorTypeEntity = ColorTypeEntity(           // 添加颜色的临时实体
        name = "",
        color = Color.Green.toArgb().toLong()                   // 默认颜色值（绿色）
    ),
)