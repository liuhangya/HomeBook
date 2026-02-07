package com.fanda.homebook.closet.state

import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 衣橱首页界面状态类
 *
 * 用于管理衣橱首页的界面状态
 *
 * @property closetEntity 衣橱实体对象，存储当前显示的衣橱物品信息
 * @property sheetType 当前显示的底部弹窗类型，默认为NONE（不显示）
 */
data class HomeClosetUiState(
    val closetEntity: ClosetEntity = ClosetEntity(),               // 衣橱实体
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE, // 弹窗类型
)