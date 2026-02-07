package com.fanda.homebook.stock.state

import android.net.Uri
import com.fanda.homebook.data.stock.StockEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 添加/编辑库存物品页面的UI状态数据类
 * 用于管理添加或编辑库存物品时的所有UI相关状态
 */
data class AddStockUiState(
    /**
     * 库存物品实体数据
     * 包含库存物品的所有业务数据
     * 默认值为空的StockEntity()，表示新建状态
     */
    val stockEntity: StockEntity = StockEntity(),

    /**
     * 物品图片的Uri
     * 用户从相册选择或相机拍摄的图片Uri
     * 可为空，表示用户未选择图片
     */
    val imageUri: Uri? = null,

    /**
     * 当前显示的底部弹窗类型
     * 用于控制不同类型的底部弹窗显示状态
     * 默认值为ShowBottomSheetType.NONE，表示不显示任何弹窗
     */
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
)