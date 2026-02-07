package com.fanda.homebook.stock.state

import android.net.Uri
import com.fanda.homebook.data.stock.StockEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 查看和编辑库存物品页面的UI状态数据类
 * 管理查看和编辑库存物品时的所有UI相关状态
 */
data class WatchAndEditStockUiState(
    /**
     * 库存物品实体数据
     * 包含正在查看或编辑的库存物品的所有业务数据
     * 默认值为空的StockEntity()，表示新建状态或数据未加载
     */
    val stockEntity: StockEntity = StockEntity(),

    /**
     * 物品图片的Uri
     * 当前库存物品关联的图片资源标识
     * 可为空，表示物品暂无图片或图片未加载
     */
    val imageUri: Uri? = null,

    /**
     * 当前显示的底部弹窗类型
     * 控制查看/编辑页面中显示的底部弹窗，如编辑分类、选择日期等弹窗
     * 默认值为ShowBottomSheetType.NONE，表示不显示任何弹窗
     */
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,

    /**
     * 是否为编辑状态
     * 标识当前页面是否处于编辑模式
     * true：编辑模式，用户可修改物品信息
     * false：查看模式，用户只能查看物品信息
     */
    val isEditState: Boolean = false,

    /**
     * 是否显示用完日期选择对话框
     * 控制用完日期选择对话框的显示状态
     * true：显示日期选择对话框
     * false：隐藏日期选择对话框
     * 主要用于标记物品为"已用完"状态时选择具体用完日期
     */
    val showUsedUpDateSelectDialog: Boolean = false
)