package com.fanda.homebook.stock.state

import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.stock.StockStatusEntity
import com.fanda.homebook.data.stock.StockUseStatus
import com.fanda.homebook.common.entity.ShowBottomSheetType

/**
 * 库存状态列表常量
 * 预定义的库存物品使用状态列表，包含四种状态：
 * - 全部：显示所有状态的物品
 * - 使用中：当前正在使用的物品
 * - 未开封：尚未开封使用的物品
 * - 已用完：已经使用完毕的物品
 */
val stockStatusList = listOf(
    StockStatusEntity(
        useStatus = StockUseStatus.ALL, name = "全部", count = 0
    ), StockStatusEntity(
        useStatus = StockUseStatus.USING, name = "使用中", count = 0
    ), StockStatusEntity(
        useStatus = StockUseStatus.NO_USE, name = "未开封", count = 0
    ), StockStatusEntity(
        useStatus = StockUseStatus.USED, name = "已用完", count = 0
    )
)

/**
 * 库存首页UI状态数据类
 * 管理库存首页的所有UI相关状态
 */
data class StockHomeUiState(
    /**
     * 当前选中的货架子分类
     * 用于筛选特定子分类下的库存物品
     * 为空时表示未选择子分类（显示所有子分类）
     */
    val curSelectRackSubCategory: RackSubCategoryEntity? = null,

    /**
     * 当前显示的底部弹窗类型
     * 控制首页中显示的底部弹窗，如筛选弹窗、分类选择弹窗等
     */
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,

    /**
     * 当前选中的使用状态
     * 默认选中第一个状态（"全部"状态）
     * 用于筛选特定使用状态的库存物品
     */
    val curSelectUseStatus: StockStatusEntity = stockStatusList.first(),

    /**
     * 当前选中的货架ID
     * 用于标识用户当前浏览的货架
     * 默认值为1，表示默认货架或第一个货架
     */
    val rackId: Int = 1,

    /**
     * 当前筛选条件下的物品总数
     * 根据选中的货架、子分类和使用状态动态计算
     * 用于显示统计信息
     */
    val count: Int = 0
)

/**
 * 库存首页货架UI状态数据类
 * 专门管理库存首页中货架相关的UI状态
 * 与StockHomeUiState分离，实现状态职责分离
 */
data class StockHomeRackUiState(
    /**
     * 库存状态列表
     * 包含所有可用的库存使用状态及其数量统计
     * 默认使用预定义的stockStatusList
     */
    val statusList: List<StockStatusEntity> = stockStatusList,

    /**
     * 当前选中的货架ID
     * 与StockHomeUiState中的rackId保持一致
     * 用于货架相关的状态管理
     */
    val rackId: Int = 1
)