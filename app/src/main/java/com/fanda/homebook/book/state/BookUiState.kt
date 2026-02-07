package com.fanda.homebook.book.state

import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 账本页面的UI状态数据类
 * 管理账本相关页面的所有UI状态，包括编辑模式、选中项、筛选条件等
 *
 * @property isEditBook 是否为编辑账本模式，true表示编辑模式，false表示查看模式
 * @property curSelectBookId 当前选中的账本ID，默认值为1
 * @property curEditBookEntity 当前正在编辑的账本实体，null表示未在编辑状态
 * @property showEditBookDialog 是否显示编辑账本对话框
 * @property year 当前筛选的年份，默认2026年
 * @property month 当前筛选的月份，默认1月
 * @property refresh 刷新标志，用于触发数据刷新
 * @property categoryId 当前筛选的分类ID，null表示未筛选
 * @property subCategoryId 当前筛选的子分类ID，null表示未筛选
 * @property sheetType 当前显示的底部弹窗类型，默认NONE表示不显示
 */
data class BookUiState(
    // 账本编辑相关状态
    val isEditBook: Boolean = false, val curSelectBookId: Int = 1, val curEditBookEntity: BookEntity? = null, val showEditBookDialog: Boolean = false,

    // 时间筛选相关状态
    val year: Int = 2026, val month: Int = 1,

    // 数据刷新状态
    val refresh: Boolean = false,

    // 分类筛选相关状态
    val categoryId: Int? = null, val subCategoryId: Int? = null,

    // 底部弹窗显示状态
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE
)