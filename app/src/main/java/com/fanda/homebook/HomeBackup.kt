//package com.fanda.homebook
//
//package com.fanda.homebook
//
//import android.app.Activity
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.compose.BackHandler
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.LinearOutSlowInEasing
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.slideInHorizontally
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutHorizontally
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBarsPadding
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.statusBars
//import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.layout.windowInsetsBottomHeight
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableFloatStateOf
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableLongStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.navigation
//import androidx.navigation.compose.rememberNavController
//import com.fanda.homebook.closet.ClosetHomePage
//import com.fanda.homebook.closet.EditClosetCategoryPage
//import com.fanda.homebook.components.CustomBottomBar
//import com.fanda.homebook.quick.QuickHomePage
//import com.fanda.homebook.route.RoutePath
//import com.fanda.homebook.route.bottomTabGraphs
//import com.fanda.homebook.route.tabRootRoutes
//import kotlinx.coroutines.delay
//
//
///*
//* 应用入口
//* */
//@Composable fun HomeBookApp() {
//    val context = LocalContext.current
//    // 获取导航控制器
//    val navController: NavHostController = rememberNavController()
//    //  记录上次的返回时间
//    var lastBackPressed by remember { mutableLongStateOf(0L) }
//    // 记录当前选中的 Tab 和是否在 tab 的一级页面
//    val (selectedTab, isTabRoute) = rememberSelectedTab(navController)
//
//    // 拦截返回键
//    BackHandler(enabled = isTabRoute) {
//        val now = System.currentTimeMillis()
//        if (now - lastBackPressed < 2000) {
//            (context as? Activity)?.finishAffinity()
//        } else {
//            lastBackPressed = now
//            Toast.makeText(context, "再按一次退出应用", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    Scaffold(bottomBar = {
//        // 一级页面才显示底部导航栏
//        AnimatedVisibility(visible = isTabRoute, enter = slideInVertically(animationSpec = tween(300)) { it }, exit = slideOutVertically(animationSpec = tween(300)) { it }) {
//            CustomBottomBar(navHostController = navController, selectedTab = selectedTab, onTabClick = {
//                navController.navigate(it.route) {
//                    // 避免多次创建相同的页面入栈
//                    launchSingleTop = true
//                    // 保存页面的信息
//                    restoreState = true
//                    // 移除之前栈中的页面，很重要，达到 tab 嵌套栈独立的效果，tab 之间的栈页面不会混乱
//                    popUpTo(navController.graph.id) { saveState = true }
//                }
//            }, onQuickAddClick = {
//                navController.navigate(RoutePath.QuickAdd.route)
//            })
//        }
//    }, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
//        // 页面容器
//        NavHost(
//            navController = navController, startDestination = RoutePath.BookGraph.route, modifier = Modifier.padding(padding)
//        ) {
//            // ====== 账本 Tab 嵌套 Graph ======
//            navigation(
//                startDestination = RoutePath.BookHome.route, route = RoutePath.BookGraph.route
//            ) {
//                composable(RoutePath.BookHome.route) {
//                    Text(
//                        "账本首页", style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp), modifier = Modifier
//                            .fillMaxSize()
//                            .statusBarsPadding()
//                    )
//                    // 示例：跳转详情
////                    Button(onClick = {
////                        navController.navigate(RoutePath.BookDetail("123").route)
////                    }) {
////                        Text("打开账本详情")
////                    }
//                }
////                composable("book_detail/{id}") { backStackEntry ->
////                    val id = backStackEntry.arguments?.getString("id") ?: "unknown"
////                    Text("账本详情: $id")
////                    Button(onClick = { navController.popBackStack() }) {
////                        Text("返回")
////                    }
////                }
//            }
//
//            // ====== 看板 Tab 嵌套 Graph ======
//            navigation(
//                startDestination = RoutePath.DashboardHome.route, route = RoutePath.DashboardGraph.route
//            ) {
//                composable(RoutePath.DashboardHome.route) {
//                    Text(
//                        "看板首页", modifier = Modifier
//                            .fillMaxSize()
//                            .statusBarsPadding()
//                    )
////                    Button(onClick = {
////                        navController.navigate(RoutePath.DashboardSettings.route)
////                    }) {
////                        Text("进入设置")
////                    }
//                }
////                composable(RoutePath.DashboardSettings.route) {
////                    Text("看板设置")
////                    Button(onClick = { navController.popBackStack() }) {
////                        Text("返回")
////                    }
////                }
//            }
//
//            // ====== 衣橱 Tab ======
//            navigation(
//                startDestination = RoutePath.ClosetHome.route, route = RoutePath.ClosetGraph.route
//            ) {
//                composable(RoutePath.ClosetHome.route) {
//                    ClosetHomePage(
//                        modifier = Modifier.fillMaxSize(), navController = navController
//                    )
//                }
//                composable(
//                    RoutePath.ClosetEditCategory.route,
////                    enterTransition = { slideInHorizontally() },
////                    exitTransition = { slideOutHorizontally() },
////                    popEnterTransition = { slideInHorizontallyBack() },
////                    popExitTransition = { slideOutHorizontallyBack() })
//                ) {
//                    EditClosetCategoryPage(
//                        modifier = Modifier.fillMaxSize(), navController = navController
//                    )
//                }
//            }
//
//            // ====== 囤货 Tab ======
//            navigation(
//                startDestination = RoutePath.StockHome.route, route = RoutePath.StockGraph.route
//            ) {
//                composable(RoutePath.StockHome.route) {
//                    Text(
//                        "囤货页面", modifier = Modifier
//                            .fillMaxSize()
//                            .statusBarsPadding()
//                    )
//                }
//            }
//
//            // ====== 全局页面（不属于任何 Tab）======
//            composable(RoutePath.QuickAdd.route) {
//                QuickHomePage(modifier = Modifier.fillMaxSize(), navController)
//            }
//
//        }
//    }
//}
//
///**
// * 记住当前选中的底部 Tab。
// *
// * @param navController 导航控制器
// * @param defaultTab 默认选中的 Tab（通常为首页）
// */
//@Composable private fun rememberSelectedTab(
//    navController: NavController, defaultTab: String = RoutePath.BookGraph.route
//): Pair<String, Boolean> {
//    Log.d("HomeBookApp", "11111")
//    val currentEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = currentEntry?.destination?.route ?: ""
//    val routePrefix = currentRoute.split("/").firstOrNull() ?: defaultTab
//    var selectedTab by remember { mutableStateOf(defaultTab) }
//    var isTabRoute by remember { mutableStateOf(true) }
//    // 如果子路由不属于 tab 页面任一个【根据路由前缀判断】，保持之前选中的 tab 状态，为了独立处理 quick add 路由的情况
//    if (routePrefix in bottomTabGraphs) {
//        selectedTab = routePrefix
//    }
//    isTabRoute = currentRoute in tabRootRoutes
//    Log.d("HomeBookApp", "selectedTab: $selectedTab , isTabRoute: $isTabRoute , currentRoute: $currentRoute")
//
//    return Pair(selectedTab, isTabRoute)
//}
//
//// 自定义左右滑动转场动画
//fun slideInHorizontally() = slideInHorizontally(
//    initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(durationMillis = 300)
//)
//
//fun slideOutHorizontally() = slideOutHorizontally(
//    targetOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(durationMillis = 300)
//)
//
//fun slideInHorizontallyBack() = slideInHorizontally(
//    initialOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(durationMillis = 300)
//)
//
//fun slideOutHorizontallyBack() = slideOutHorizontally(
//    targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(durationMillis = 300)
//)
//
//
//
//
