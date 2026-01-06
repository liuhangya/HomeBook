package com.fanda.homebook

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.book.BookHomePage
import com.fanda.homebook.closet.AddClosetColorPage
import com.fanda.homebook.closet.AddClosetPage
import com.fanda.homebook.closet.ClosetCategoryDetailPage
import com.fanda.homebook.closet.ClosetHomePage
import com.fanda.homebook.closet.EditClosetCategoryPage
import com.fanda.homebook.closet.EditClosetColorPage
import com.fanda.homebook.components.CustomBottomBar
import com.fanda.homebook.quick.QuickHomePage
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.route.bottomTabGraphs
import com.fanda.homebook.route.tabRootRoutes
import com.fanda.homebook.stock.AddStockPage
import com.fanda.homebook.stock.StockHomePage
import com.fanda.homebook.ui.theme.HomeBookTheme
import kotlinx.coroutines.launch


/*
* 应用入口
* */
@Composable fun HomeBookApp() {
    val context = LocalContext.current
    val navController: NavHostController = rememberNavController()
    var lastBackPressed by remember { mutableLongStateOf(0L) }
    val (selectedTab, isTabRoute) = rememberSelectedTab(navController)

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 同步状态
    var isDrawerOpen by remember { mutableStateOf(false) }
    LaunchedEffect(drawerState.currentValue) {
        isDrawerOpen = drawerState.isOpen
    }

    // 抽屉内容
    var drawerContent: @Composable () -> Unit by remember { mutableStateOf({}) }

    // 拦截返回键（仅在 tab 一级页面）
    BackHandler(enabled = isDrawerOpen || isTabRoute) {
        if (isDrawerOpen) {
            scope.launch { drawerState.close() }
        } else {
            // 原有的双击退出逻辑
            val now = System.currentTimeMillis()
            if (now - lastBackPressed < 2000) {
                (context as? Activity)?.finishAffinity()
            } else {
                lastBackPressed = now
                Toast.makeText(context, "再按一次退出应用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalNavigationDrawer(gesturesEnabled = drawerState.isOpen, drawerState = drawerState, drawerContent = {
        ModalDrawerSheet(windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            drawerContainerColor = Color.Transparent, drawerShape = RoundedCornerShape(0.dp)
        ) {
            ModalDrawerSheet(windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp), drawerShape = RoundedCornerShape(0.dp), drawerContainerColor = Color.Transparent) {
                drawerContent()
            }
        }
    }) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 主内容区域：自动占据剩余空间
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                NavHost(
                    navController = navController, startDestination = RoutePath.BookGraph.route, modifier = Modifier.fillMaxSize()
                ) {
                    // ====== 账本 Tab 嵌套 Graph ======
                    navigation(startDestination = RoutePath.BookHome.route, route = RoutePath.BookGraph.route) {
                        composable(RoutePath.BookHome.route) {
                            BookHomePage(modifier = Modifier.fillMaxSize(), navController = navController, onShowDrawer = { content ->
                                drawerContent = content
                                scope.launch { drawerState.open() }
                            }, onCloseDrawer = {
                                scope.launch { drawerState.close() }
                            })
                        }
                    }

                    // ====== 看板 Tab ======
                    navigation(startDestination = RoutePath.DashboardHome.route, route = RoutePath.DashboardGraph.route) {
                        composable(RoutePath.DashboardHome.route) {
                            Text(
                                "看板首页", modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                            )
                        }
                    }

                    // ====== 衣橱 Tab ======
                    navigation(startDestination = RoutePath.ClosetHome.route, route = RoutePath.ClosetGraph.route) {
                        composable(RoutePath.ClosetHome.route) {
                            ClosetHomePage(modifier = Modifier.fillMaxSize(), navController = navController)
                        }
                        composable(RoutePath.AddCloset.route) {
                            AddClosetPage(modifier = Modifier.fillMaxSize(), navController = navController)
                        }
                        composable(RoutePath.ClosetEditCategory.route) {
                            EditClosetCategoryPage(modifier = Modifier.fillMaxSize(), navController = navController)
                        }
                        composable(RoutePath.ClosetEditColor.route) {
                            EditClosetColorPage(modifier = Modifier.fillMaxSize(), navController = navController)
                        }
                        composable(RoutePath.ClosetAddColor.route) {
                            AddClosetColorPage(modifier = Modifier.fillMaxSize(), navController = navController)
                        }
                        composable(RoutePath.ClosetDetailCategory.route) {
                            ClosetCategoryDetailPage(modifier = Modifier.fillMaxSize(), navController = navController)
                        }
                    }

                    // ====== 囤货 Tab ======
                    navigation(startDestination = RoutePath.StockHome.route, route = RoutePath.StockGraph.route) {
                        composable(RoutePath.StockHome.route) {
                            StockHomePage(modifier = Modifier.fillMaxSize(), navController = navController)
                        }
                        composable(RoutePath.AddStock.route) {
                            AddStockPage(modifier = Modifier.fillMaxSize(), navController = navController)
                        }
                    }

                    // ====== 全局页面（不属于任何 Tab）======
                    composable(RoutePath.QuickAdd.route) {
                        QuickHomePage(modifier = Modifier.fillMaxSize(), navController = navController)
                    }
                }
            }

            if (isTabRoute) {
                // 确保在有手势导航的设备上留出安全区
                CustomBottomBar(modifier = Modifier.navigationBarsPadding(), selectedTab = selectedTab, onTabClick = { tab ->
                    navController.navigate(tab.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.id) { saveState = true }
                    }
                }, onQuickAddClick = {
                    navController.navigate(RoutePath.QuickAdd.route)
                })
            }
        }
    }


}

/**
 * 记住当前选中的底部 Tab。
 *
 * @param navController 导航控制器
 * @param defaultTab 默认选中的 Tab（通常为首页）
 */
@Composable private fun rememberSelectedTab(
    navController: NavController, defaultTab: String = RoutePath.BookGraph.route
): Pair<String, Boolean> {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route ?: ""
    val routePrefix = currentRoute.split("/").firstOrNull() ?: defaultTab
    var selectedTab by remember { mutableStateOf(defaultTab) }
    var isTabRoute by remember { mutableStateOf(true) }
    // 如果子路由不属于 tab 页面任一个【根据路由前缀判断】，保持之前选中的 tab 状态，为了独立处理 quick add 路由的情况
    if (routePrefix in bottomTabGraphs) {
        selectedTab = routePrefix
    }
    isTabRoute = currentRoute in tabRootRoutes
    Log.d("HomeBookApp", "selectedTab: $selectedTab , isTabRoute: $isTabRoute , currentRoute: $currentRoute")

    return Pair(selectedTab, isTabRoute)
}

@Composable @Preview(showBackground = true) private fun HomeBookAppPreview() {
    HomeBookTheme {
        HomeBookApp()
    }
}



