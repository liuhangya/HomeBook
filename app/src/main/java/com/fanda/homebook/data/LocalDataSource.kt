package com.fanda.homebook.data

import com.fanda.homebook.R
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.data.closet.CategoryBottomMenuEntity
import com.fanda.homebook.data.stock.StockMenuEntity

/**
 * 本地数据源
 * 包含应用中使用的静态数据和配置
 */
object LocalDataSource {

    /**
     * 衣橱分类底部菜单列表
     * 用于显示衣橱分类页面的底部操作菜单
     */
    val closetCategoryBottomMenuList = listOf(
        CategoryBottomMenuEntity("全选", R.mipmap.icon_bottom_all_select, ShowBottomSheetType.ALL_SELECTED),
        CategoryBottomMenuEntity("复制", R.mipmap.icon_bottom_copy, ShowBottomSheetType.COPY),
        CategoryBottomMenuEntity("移动", R.mipmap.icon_bottom_move, ShowBottomSheetType.MOVE),
        CategoryBottomMenuEntity("删除", R.mipmap.icon_bottom_delete, ShowBottomSheetType.DELETE)
    )

    /**
     * 库存商品菜单列表
     * 用于显示库存商品的操作菜单
     */
    val stockMenuList = listOf(
        StockMenuEntity("编辑商品", ShowBottomSheetType.EDIT), StockMenuEntity("复制商品", ShowBottomSheetType.COPY), StockMenuEntity("删除商品", ShowBottomSheetType.DELETE)
    )

    /**
     * 剩余量选项数据
     * 用于表示商品的剩余状态
     */
    val remainData = listOf(
        "空瓶",    // 商品已用完
        "较少",    // 商品剩余量较少
        "较多",    // 商品剩余量较多
    )

    /**
     * 使用感受选项数据
     * 用于记录用户对商品的使用评价
     */
    val feelData = listOf(
        "不好用",  // 使用体验差，不推荐
        "一般",    // 使用体验普通
        "好用",    // 使用体验良好
        "回购",    // 使用体验优秀，会再次购买
    )

    val sortWayData = listOf(
        Pair(1,"添加时间"),
        Pair(2,"穿着次数"),
        Pair(3,"服饰价格"),
        Pair(4,"购买时间"),
    )

}