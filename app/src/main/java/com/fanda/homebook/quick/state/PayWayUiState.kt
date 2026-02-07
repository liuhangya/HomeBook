package com.fanda.homebook.quick.state

import com.fanda.homebook.data.pay.PayWayEntity

/**
 * 支付方式管理页面UI状态数据类
 * 管理支付方式编辑页面的所有UI状态，包括选中项和弹窗显示状态
 *
 * @property entity 当前选中的支付方式实体，null表示未选中
 * @property renameOrDeleteBottomSheet 是否显示重命名或删除底部弹窗
 * @property editDialog 是否显示编辑对话框
 * @property addDialog 是否显示添加对话框
 * @property addEntity 用于添加新支付方式的临时实体，包含默认值
 */
data class PayWayUiState(
    // 选中的支付方式
    val entity: PayWayEntity? = null,

    // 弹窗显示状态
    val renameOrDeleteBottomSheet: Boolean = false,
    val editDialog: Boolean = false,
    val addDialog: Boolean = false,

    // 添加新支付方式时的临时数据
    val addEntity: PayWayEntity = PayWayEntity(
        name = "",  // 默认空名称
    ),
)