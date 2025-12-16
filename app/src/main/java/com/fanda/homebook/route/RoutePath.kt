package com.fanda.homebook.route

/*
* 所有页面的路由地址
* */
enum class RoutePath(val route: String, val title: String = "") {
    BOOK("homeBook"), DASHBOARD("dashboard"), QUICK_ADD("quickAdd"), CLOSET("closet"), STOCK("stock")

}