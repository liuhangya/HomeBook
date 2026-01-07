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

    data class BookDetail(val id: String) : RoutePath("book_graph/book_detail/$id")

    data object DashboardHome : RoutePath("dashboard_graph/dashboard_home")

    data object DashboardSettings : RoutePath("dashboard_graph/dashboard_settings")

    data object ClosetHome : RoutePath("closet_graph/closet_home")

    data object AddCloset : RoutePath("closet_graph/add_closet")

    data object ClosetEditCategory : RoutePath("closet_graph/closet_edit_category")

    data object ClosetEditColor : RoutePath("closet_graph/closet_edit_color")

    data object ClosetAddColor : RoutePath("closet_graph/closet_add_color")

    data object ClosetDetailCategory : RoutePath("closet_graph/closet_detail_category")

    data object StockHome : RoutePath("stock_graph/stock_home")

    data object AddStock : RoutePath("stock_graph/add_stock")

    // 全局页面（不属于任何 Tab）
    data object QuickAdd : RoutePath("quick_add")

    data object EditImage : RoutePath("edit_image")
}

// 所有 Tab 对应的 Graph route（用于判断是否在 Tab 内）
val bottomTabGraphs = setOf(
    RoutePath.BookGraph.route, RoutePath.DashboardGraph.route, RoutePath.ClosetGraph.route, RoutePath.StockGraph.route
)

// 所有 Tab 的“根页面” route（用于判断是否应拦截返回键和显示底部导航栏）
val tabRootRoutes = setOf(
    RoutePath.BookHome.route, RoutePath.DashboardHome.route, RoutePath.ClosetHome.route, RoutePath.StockHome.route
)