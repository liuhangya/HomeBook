package com.fanda.homebook.closet.state

import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 衣橱分类界面状态类
 *
 * 用于管理衣橱分类添加/编辑时的界面状态
 *
 * @property categoryEntity 分类实体对象，存储分类的基本信息
 * @property sheetType 当前显示的底部弹窗类型，默认为NONE（不显示）
 */
data class CategoryClosetUiState(
    val categoryEntity: CategoryEntity = CategoryEntity(name = ""),     // 分类实体
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,      // 弹窗类型
)