package com.fanda.homebook.route

/*
* 所有页面的路由地址
* */
enum class RoutePath(val route: String, val title: String = "") {
    BOOK("homeBook"), DASHBOARD("dashboard"), QUICK_ADD("quickAdd", "记一笔"), CLOSET("closet"), STOCK("stock")
}

// 底部导航栏路由，不包括中间的记一笔
val bottomTabRoutes = setOf<RoutePath>(
    RoutePath.BOOK,
    RoutePath.DASHBOARD,
    RoutePath.CLOSET,
    RoutePath.STOCK
)