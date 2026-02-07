package com.fanda.homebook.closet.state

import android.net.Uri
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 添加衣橱界面状态类
 *
 * 用于管理添加/编辑衣橱物品时的界面状态
 *
 * @property closetEntity 衣橱实体对象，存储衣橱物品的基本信息
 * @property imageUri 衣橱物品的图片Uri（可选）
 * @property seasonIds 季节ID列表，表示该物品适用的季节
 * @property sheetType 当前显示的底部弹窗类型，默认为NONE（不显示）
 */
data class AddClosetUiState(
    val closetEntity: ClosetEntity = ClosetEntity(),                    // 衣橱实体
    val imageUri: Uri? = null,                                          // 图片Uri
    val seasonIds : List<Int> = listOf(),                               // 季节ID列表
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,      // 弹窗类型
)