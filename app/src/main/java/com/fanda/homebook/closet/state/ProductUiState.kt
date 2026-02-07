package com.fanda.homebook.closet.state

import com.fanda.homebook.data.product.ProductEntity

/**
 * 产品管理界面状态类
 *
 * 用于管理产品分类的界面状态
 *
 * @property entity 当前选中的产品实体对象，可能为空
 * @property renameOrDeleteBottomSheet 是否显示重命名/删除底部弹窗，默认为false
 * @property editDialog 是否显示编辑产品对话框，默认为false
 * @property addDialog 是否显示添加产品对话框，默认为false
 * @property addEntity 用于添加新产品的临时实体对象
 */
data class ProductUiState(
    val entity: ProductEntity? = null,                        // 当前选中的产品实体
    val renameOrDeleteBottomSheet: Boolean = false,           // 重命名/删除弹窗显示状态
    val editDialog: Boolean = false,                          // 编辑对话框显示状态
    val addDialog: Boolean = false,                           // 添加对话框显示状态
    val addEntity: ProductEntity = ProductEntity(             // 添加产品的临时实体
        name = "",
    ),
)