package com.fanda.homebook.route

/*
* 所有页面的路由地址
* */
sealed class RoutePath(val route: String) {
    // 嵌套根路由
    data object BookGraph : RoutePath("book_graph")
    data object DashboardGraph : RoutePath("dashboard_graph")
    data object ClosetGraph : RoutePath("closet_graph")
    data object StockGraph : RoutePath("stock_graph")

    // 子页面必须包含 graph 前缀（隐式）
    data object BookHome : RoutePath("book_graph/book_home")
    data object DashBoar : RoutePath("book_graph/dashboard")
    data object DashBoarRank : RoutePath("book_graph/dashboard_rank")
    data object DashBoarDetail : RoutePath("book_graph/dashboard_detail")

    data object DashboardHome : RoutePath("dashboard_graph/dashboard_home")

    data object ClosetHome : RoutePath("closet_graph/closet_home")

    data object AddCloset : RoutePath("closet_graph/add_closet")

    data object WatchAndEditCloset : RoutePath("closet_graph/watch_and_edit_closet")

    data object ClosetCategory : RoutePath("closet_graph/closet_category")

    data object ClosetDetailCategory : RoutePath("closet_graph/closet_detail_category")

    data object StockHome : RoutePath("stock_graph/stock_home")

    data object WatchAndEditStock : RoutePath("stock_graph/watch_and_edit_stock")

    data object AddStock : RoutePath("stock_graph/add_stock")

    // 全局页面（不属于任何 Tab）
    data object QuickAdd : RoutePath("quick_add")

    data object QuickWatchAndEdit : RoutePath("quick_watch_and_edit")

    data object EditCategory : RoutePath("edit_category")

    data object EditTransactionCategory : RoutePath("edit_transaction_category")

    data object EditColor : RoutePath("edit_color")

    data object EditProduct : RoutePath("edit_product")

    data object EditPayWay : RoutePath("edit_pay_way")

    data object AddColor : RoutePath("add_color")

    data object EditSize : RoutePath("edit_size")

    data object EditSubCategory : RoutePath("edit_sub_category")


}

// 所有 Tab 对应的 Graph route（用于判断是否在 Tab 内）
val bottomTabGraphs = setOf(
    RoutePath.BookGraph.route, RoutePath.DashboardGraph.route, RoutePath.ClosetGraph.route, RoutePath.StockGraph.route
)

// 所有 Tab 的“根页面” route（用于判断是否应拦截返回键和显示底部导航栏）
val tabRootRoutes = setOf(
    RoutePath.BookHome.route, RoutePath.DashboardHome.route, RoutePath.ClosetHome.route, RoutePath.StockHome.route
)