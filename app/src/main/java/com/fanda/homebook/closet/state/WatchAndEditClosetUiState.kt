package com.fanda.homebook.closet.state

import android.net.Uri
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 查看与编辑衣橱界面状态类
 *
 * 用于管理查看和编辑衣橱物品时的界面状态
 *
 * @property closetEntity 衣橱实体对象，存储衣橱物品的详细信息
 * @property imageUri 衣橱物品的图片Uri（可选）
 * @property sheetType 当前显示的底部弹窗类型，默认为NONE（不显示）
 * @property isEditState 是否处于编辑状态，默认为false（查看模式）
 * @property seasonIds 季节ID列表，表示该物品适用的季节
 */
data class WatchAndEditClosetUiState(
    val closetEntity: ClosetEntity = ClosetEntity(),             // 衣橱实体
    val imageUri: Uri? = null,                                   // 图片Uri
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE, // 弹窗类型
    val isEditState: Boolean = false,                            // 编辑状态
    val seasonIds : List<Int> = listOf(),                        // 季节ID列表
)