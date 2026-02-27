package com.fanda.homebook.closet.state

import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.season.SeasonEntity

/**
 * 衣橱分类详情界面状态类
 *
 * 用于管理衣橱分类详情页面的界面状态
 *
 * @property categoryId 一级分类ID，默认为-1表示未设置
 * @property subCategoryId 二级分类ID，默认为-1表示未设置
 * @property categoryName 分类名称
 * @property isEditState 是否处于编辑状态，默认为false
 * @property moveToTrash 是否移动到回收站状态，默认为false
 * @property sheetType 当前显示的底部弹窗类型，默认为NONE（不显示）
 */
data class CategoryDetailClosetUiState(
    val categoryId: Int = -1,                                   // 一级分类ID
    val subCategoryId: Int = -1,                                // 二级分类ID
    val categoryName: String = "",                              // 分类名称
    val isEditState: Boolean = false,                           // 编辑状态
    val moveToTrash: Boolean = false,                           // 移动到回收站状态
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE, // 弹窗类型
    val sortWay: Pair<Int, String>? = LocalDataSource.sortWayData.first(),   // 排序方式
    val subCategoryEntity: SubCategoryEntity? = SubCategoryEntity(-1, "全部", categoryId = -1),           // -1表示全部
    val seasonEntity: SeasonEntity? = null,          // null 表示不筛选
    val colorTypeEntity: ColorTypeEntity? = null,          // null 表示不筛选
)