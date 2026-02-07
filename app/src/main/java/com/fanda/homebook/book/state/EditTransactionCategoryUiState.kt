package com.fanda.homebook.book.state

import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 编辑交易分类页面UI状态数据类
 * 管理交易分类编辑页面的所有UI状态，包括分类选择、弹窗显示等
 *
 * @property categoryId 当前选中的主分类ID，默认值为1
 * @property subCategoryId 当前选中的子分类ID，null表示未选中子分类
 * @property sheetType 当前显示的底部弹窗类型，默认NONE表示不显示
 * @property addDialog 是否显示添加分类对话框，true表示显示
 * @property deleteOrEditDialog 是否显示删除或编辑分类对话框，true表示显示
 * @property editDialog 是否显示编辑分类对话框，true表示显示
 */
data class EditTransactionCategoryUiState(
    // 分类选择状态
    val categoryId: Int = 1,              // 当前选中的主分类ID
    val subCategoryId: Int? = null,      // 当前选中的子分类ID，可为null

    // 弹窗显示状态
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,  // 底部弹窗类型
    val addDialog: Boolean = false,       // 添加分类对话框显示状态
    val deleteOrEditDialog: Boolean = false,  // 删除/编辑分类对话框显示状态
    val editDialog: Boolean = false,      // 编辑分类对话框显示状态
)