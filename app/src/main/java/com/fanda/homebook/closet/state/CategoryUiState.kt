package com.fanda.homebook.closet.state

import com.fanda.homebook.data.category.CategoryEntity

/**
 * 分类界面状态类
 *
 * 用于管理分类管理页面的界面状态
 *
 * @property entity 当前选中的分类实体对象，可能为空
 * @property renameOrDeleteBottomSheet 是否显示重命名/删除底部弹窗，默认为false
 * @property editDialog 是否显示编辑分类对话框，默认为false
 * @property addDialog 是否显示添加分类对话框，默认为false
 * @property addEntity 用于添加新分类的临时实体对象
 */
data class CategoryUiState(
    val entity: CategoryEntity? = null,                         // 当前选中的分类实体
    val renameOrDeleteBottomSheet: Boolean = false,             // 重命名/删除弹窗显示状态
    val editDialog: Boolean = false,                            // 编辑对话框显示状态
    val addDialog: Boolean = false,                             // 添加对话框显示状态
    val addEntity: CategoryEntity = CategoryEntity(             // 添加分类的临时实体
        name = "",
    ),
)