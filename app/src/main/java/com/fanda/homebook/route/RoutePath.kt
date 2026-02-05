package com.fanda.homebook.route

/*
 * 所有页面的路由地址
 * 使用密封类定义，确保路由类型安全
 */
sealed class RoutePath(val route: String) {
    // ==================== 嵌套导航根路由（对应底部Tab）====================

    // 账本模块导航根
    data object BookGraph : RoutePath("book_graph")

    // 资产管理模块导航根
    data object MoneyGraph : RoutePath("money_graph")

    // 衣橱管理模块导航根
    data object ClosetGraph : RoutePath("closet_graph")

    // 囤货管理模块导航根
    data object StockGraph : RoutePath("stock_graph")

    // ==================== 账本模块子页面 ====================

    // 账本首页
    data object BookHome : RoutePath("book_graph/book_home")

    // 数据看板（Dashboard）页面
    data object DashBoard : RoutePath("book_graph/dashboard")

    // 看板排行榜页面
    data object DashBoardRank : RoutePath("book_graph/dashboard_rank")

    // 看板详情页面
    data object DashBoardDetail : RoutePath("book_graph/dashboard_detail")

    // ==================== 资产/资金模块子页面 ====================

    // 资产首页
    data object MoneyHome : RoutePath("money_graph/money_home") // 已修正路径前缀

    // ==================== 衣橱模块子页面 ====================

    // 衣橱首页
    data object ClosetHome : RoutePath("closet_graph/closet_home")

    // 添加衣橱物品页面
    data object AddCloset : RoutePath("closet_graph/add_closet")

    // 查看和编辑衣橱物品页面
    data object WatchAndEditCloset : RoutePath("closet_graph/watch_and_edit_closet")

    // 衣橱分类页面
    data object ClosetCategory : RoutePath("closet_graph/closet_category")

    // 衣橱详细分类页面
    data object ClosetDetailCategory : RoutePath("closet_graph/closet_detail_category")

    // ==================== 囤货模块子页面 ====================

    // 囤货首页
    data object StockHome : RoutePath("stock_graph/stock_home")

    // 查看和编辑囤货页面
    data object WatchAndEditStock : RoutePath("stock_graph/watch_and_edit_stock")

    // 添加囤货页面
    data object AddStock : RoutePath("stock_graph/add_stock")

    // ==================== 全局页面（不属于任何Tab）====================

    // 快速添加页面（可能用于快速记账或添加物品）
    data object QuickAdd : RoutePath("quick_add")

    // 查看和编辑快速添加项页面
    data object WatchAndEditQuick : RoutePath("watch_and_edit_quick")

    // 编辑分类页面
    data object EditCategory : RoutePath("edit_category")

    // 编辑交易分类页面
    data object EditTransactionCategory : RoutePath("edit_transaction_category")

    // 编辑颜色页面
    data object EditColor : RoutePath("edit_color")

    // 编辑商品页面
    data object EditProduct : RoutePath("edit_product")

    // 编辑支付方式页面
    data object EditPayWay : RoutePath("edit_pay_way")

    // 添加颜色页面
    data object AddColor : RoutePath("add_color")

    // 编辑尺寸页面
    data object EditSize : RoutePath("edit_size")

    // 编辑子分类页面
    data object EditSubCategory : RoutePath("edit_sub_category")
}

// ==================== 辅助常量定义 ====================

/**
 * 所有底部Tab对应的导航图路由
 * 用于判断当前页面是否在底部Tab的导航栈内
 * 主要用途：控制底部导航栏显示/隐藏，处理返回键逻辑
 */
val bottomTabGraphs = setOf(
    RoutePath.BookGraph.route,
    RoutePath.MoneyGraph.route,
    RoutePath.ClosetGraph.route,
    RoutePath.StockGraph.route
)

/**
 * 所有底部Tab的根页面路由
 * 用于判断是否应该拦截返回键（避免直接退出应用）
 * 以及确定何时显示底部导航栏
 */
val tabRootRoutes = setOf(
    RoutePath.BookHome.route,
    RoutePath.MoneyHome.route,
    RoutePath.ClosetHome.route,
    RoutePath.StockHome.route
)