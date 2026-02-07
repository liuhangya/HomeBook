package com.fanda.homebook.closet.state

import com.fanda.homebook.data.category.SubCategoryEntity

/**
 * 子分类管理界面状态类
 *
 * 用于管理子分类（二级分类）的界面状态
 *
 * @property entity 当前选中的子分类实体对象，可能为空
 * @property renameOrDeleteBottomSheet 是否显示重命名/删除底部弹窗，默认为false
 * @property editDialog 是否显示编辑子分类对话框，默认为false
 * @property addDialog 是否显示添加子分类对话框，默认为false
 * @property addEntity 用于添加新子分类的临时实体对象，默认关联到categoryId=1的父分类
 * @property categoryName 父分类的名称，默认为"子分类管理"
 */
data class SubCategoryUiState(
    val entity: SubCategoryEntity? = null,                     // 当前选中的子分类实体
    val renameOrDeleteBottomSheet: Boolean = false,            // 重命名/删除弹窗显示状态
    val editDialog: Boolean = false,                           // 编辑对话框显示状态
    val addDialog: Boolean = false,                            // 添加对话框显示状态
    val addEntity: SubCategoryEntity = SubCategoryEntity(      // 添加子分类的临时实体
        name = "",
        categoryId = 1                                         // 默认父分类ID
    ),
    val categoryName: String = "子分类管理"                    // 父分类名称
)