package com.fanda.homebook

import android.app.Activity
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fanda.homebook.book.BookHomePage
import com.fanda.homebook.book.DashBoarDetailPage
import com.fanda.homebook.book.DashBoarRankPage
import com.fanda.homebook.book.DashBoardPage
import com.fanda.homebook.book.EditTransactionCategoryPage
import com.fanda.homebook.closet.AddClosetPage
import com.fanda.homebook.closet.ClosetCategoryDetailPage
import com.fanda.homebook.closet.ClosetCategoryPage
import com.fanda.homebook.closet.ClosetHomePage
import com.fanda.homebook.closet.EditCategoryPage
import com.fanda.homebook.closet.EditSubCategoryPage
import com.fanda.homebook.closet.WatchAndEditClosetPage
import com.fanda.homebook.common.AddColorPage
import com.fanda.homebook.common.EditColorPage
import com.fanda.homebook.common.EditPayWayPage
import com.fanda.homebook.common.EditProductPage
import com.fanda.homebook.common.EditSizePage
import com.fanda.homebook.components.CustomBottomBar
import com.fanda.homebook.quick.AddQuickHomePage
import com.fanda.homebook.quick.WatchAndEditQuickPage
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.route.bottomTabGraphs
import com.fanda.homebook.route.tabRootRoutes
import com.fanda.homebook.stock.AddStockPage
import com.fanda.homebook.stock.StockHomePage
import com.fanda.homebook.stock.WatchAndEditStockPage
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster
import kotlinx.coroutines.launch

/*
 * 应用入口组件，负责管理整个应用的导航结构、抽屉菜单、底部导航栏等核心功能。
 * 主要职责：
 * 1. 管理应用全局导航栈
 * 2. 处理侧边抽屉菜单的显示/隐藏
 * 3. 管理底部导航栏的状态和切换
 * 4. 拦截系统返回键实现双击退出和抽屉关闭
 * 5. 协调各功能模块的页面跳转
 */
@Composable
fun HomeBookApp() {
    // ========== 初始化状态和控制器 ==========
    val context = LocalContext.current
    val navController: NavHostController = rememberNavController() // 应用全局导航控制器
    var lastBackPressed by remember { mutableLongStateOf(0L) } // 记录上次按下返回键的时间，用于双击退出
    val (selectedTab, isTabRoute) = rememberSelectedTab(navController) // 获取当前选中的底部Tab和是否为Tab页面

    val drawerState = rememberDrawerState(DrawerValue.Closed) // 抽屉状态（默认关闭）
    val scope = rememberCoroutineScope() // 协程作用域，用于异步操作

    // ========== 抽屉状态同步 ==========
    // 监听抽屉状态变化，同步到本地状态变量
    var isDrawerOpen by remember { mutableStateOf(false) }
    LaunchedEffect(drawerState.currentValue) {
        isDrawerOpen = drawerState.isOpen
    }

    // ========== 抽屉内容动态设置 ==========
    // 抽屉内容由各个页面动态设置，这里使用可变状态保存内容
    var drawerContent: @Composable () -> Unit by remember { mutableStateOf({}) }

    // ========== 返回键拦截逻辑 ==========
    // BackHandler 用于拦截系统返回键，仅在抽屉打开或处于Tab页面时生效
    BackHandler(enabled = isDrawerOpen || isTabRoute) {
        if (isDrawerOpen) {
            // 如果抽屉已打开，点击返回键关闭抽屉
            scope.launch { drawerState.close() }
        } else {
            // 如果在Tab页面，实现双击退出功能
            val now = System.currentTimeMillis()
            if (now - lastBackPressed < 2000) {
                // 2秒内再次按下返回键，退出应用
                (context as? Activity)?.finishAffinity()
            } else {
                // 第一次按下返回键，记录时间并提示用户
                lastBackPressed = now
                Toaster.show("再按一次退出应用")
            }
        }
    }

    // ========== 主应用布局：抽屉 + 内容区域 ==========
    ModalNavigationDrawer(
        gesturesEnabled = drawerState.isOpen, // 抽屉打开时启用手势
        drawerState = drawerState, drawerContent = {
            // 抽屉内容区域，使用透明背景和自定义形状
            ModalDrawerSheet(
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp), // 移除系统默认边距
                drawerContainerColor = Color.Transparent, // 透明背景
                drawerShape = RoundedCornerShape(0.dp) // 直角形状
            ) {
                ModalDrawerSheet(
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                    drawerShape = RoundedCornerShape(0.dp),
                    drawerContainerColor = Color.Transparent
                ) {
                    drawerContent() // 动态渲染抽屉内容
                }
            }
        }) {
        // ========== 主内容区域：导航 + 底部栏 ==========
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== 导航内容区域（自动占据剩余空间）==========
            Box(
                modifier = Modifier
                    .weight(1f) // 使用 weight 占据剩余空间
                    .fillMaxWidth()
            ) {
                // 导航图配置，管理所有页面的跳转关系
                NavHost(
                    navController = navController,
                    startDestination = RoutePath.BookGraph.route, // 应用启动时的默认页面（账本首页）
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ====== 账本 Tab 嵌套导航图 ======
                    // 使用嵌套导航图将Tab相关页面分组管理
                    navigation(
                        startDestination = RoutePath.BookHome.route,
                        route = RoutePath.BookGraph.route // 路由：book
                    ) {
                        composable(RoutePath.BookHome.route) {
                            BookHomePage(
                                modifier = Modifier.fillMaxSize(),
                                navController = navController,
                                isDrawerOpen = isDrawerOpen,
                                onShowDrawer = { content ->
                                    // 页面设置抽屉内容并打开抽屉
                                    drawerContent = content
                                    scope.launch { drawerState.open() }
                                },
                                onCloseDrawer = {
                                    // 页面请求关闭抽屉
                                    scope.launch { drawerState.close() }
                                })
                        }
                        composable(
                            // 仪表板页面，带参数：年份、月份、类型
                            route = "${RoutePath.DashBoard.route}?year={year}&month={month}&type={type}",
                            arguments = listOf(
                                navArgument("year") { type = NavType.IntType },
                                navArgument("month") { type = NavType.IntType },
                                navArgument("type") { type = NavType.IntType })
                        ) {
                            DashBoardPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(
                            // 排行榜页面，带参数：年份、月份、类型、标题
                            route = "${RoutePath.DashBoardRank.route}?year={year}&month={month}&type={type}&title={title}",
                            arguments = listOf(
                                navArgument("year") { type = NavType.IntType },
                                navArgument("month") { type = NavType.IntType },
                                navArgument("type") { type = NavType.IntType },
                                navArgument("title") { type = NavType.StringType })
                        ) {
                            DashBoarRankPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(
                            // 详细数据页面，带参数：标题
                            route = "${RoutePath.DashBoardDetail.route}?title={title}",
                            arguments = listOf(
                                navArgument("title") { type = NavType.StringType })
                        ) {
                            DashBoarDetailPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                    }

                    // ====== 看板 Tab 导航图 ======
                    // 注：目前仅占位，实际功能待开发
                    navigation(
                        startDestination = RoutePath.MoneyHome.route,
                        route = RoutePath.MoneyGraph.route // 路由：dashboard
                    ) {
                        composable(RoutePath.MoneyHome.route) {
                            Text(
                                "资产首页-没开发呢...", modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding(), textAlign = TextAlign.Center
                            )
                        }
                    }

                    // ====== 衣橱 Tab 导航图 ======
                    navigation(
                        startDestination = RoutePath.ClosetHome.route,
                        route = RoutePath.ClosetGraph.route // 路由：closet
                    ) {
                        composable(RoutePath.ClosetHome.route) {
                            ClosetHomePage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(
                            // 添加衣橱物品页面，带参数：图片路径
                            route = "${RoutePath.AddCloset.route}?imagePath={imagePath}&categoryId={categoryId}",
                            arguments = listOf(
                                navArgument("imagePath") { type = NavType.StringType },
                                navArgument("categoryId") {
                                    type = NavType.IntType
                                },
                            )
                        ) {
                            AddClosetPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(RoutePath.EditCategory.route) {
                            EditCategoryPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(RoutePath.EditColor.route) {
                            EditColorPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(
                            // 添加颜色页面，带参数：颜色ID
                            "${RoutePath.AddColor.route}?colorId={colorId}", arguments = listOf(
                                navArgument("colorId") { type = NavType.IntType })
                        ) {
                            AddColorPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(
                            // 衣橱分类页面，带参数：分类实体JSON字符串
                            "${RoutePath.ClosetCategory.route}?categoryEntity={categoryEntity}",
                            arguments = listOf(
                                navArgument("categoryEntity") { type = NavType.StringType })
                        ) {
                            ClosetCategoryPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(
                            // 衣橱分类详情页面，带参数：分类ID、子分类ID、分类名称、是否移到回收站
                            "${RoutePath.ClosetDetailCategory.route}?categoryId={categoryId}&subCategoryId={subCategoryId}&categoryName={categoryName}&moveToTrash={moveToTrash}",
                            arguments = listOf(
                                navArgument("categoryId") { type = NavType.IntType },
                                navArgument("subCategoryId") { type = NavType.IntType },
                                navArgument("categoryName") { type = NavType.StringType },
                                navArgument("moveToTrash") { type = NavType.BoolType })
                        ) {
                            ClosetCategoryDetailPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(
                            // 查看和编辑衣橱页面，带参数：衣橱ID
                            "${RoutePath.WatchAndEditCloset.route}?closetId={closetId}",
                            arguments = listOf(
                                navArgument("closetId") { type = NavType.IntType })
                        ) {
                            WatchAndEditClosetPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                    }

                    // ====== 囤货 Tab 导航图 ======
                    navigation(
                        startDestination = RoutePath.StockHome.route,
                        route = RoutePath.StockGraph.route // 路由：stock
                    ) {
                        composable(RoutePath.StockHome.route) {
                            StockHomePage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(
                            // 添加囤货页面，带参数：图片路径
                            route = "${RoutePath.AddStock.route}?imagePath={imagePath}",
                            arguments = listOf(
                                navArgument("imagePath") { type = NavType.StringType })
                        ) {
                            AddStockPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                        composable(
                            // 查看和编辑囤货页面，带参数：囤货ID
                            "${RoutePath.WatchAndEditStock.route}?stockId={stockId}",
                            arguments = listOf(
                                navArgument("stockId") { type = NavType.IntType })
                        ) {
                            WatchAndEditStockPage(
                                modifier = Modifier.fillMaxSize(), navController = navController
                            )
                        }
                    }

                    // ========== 全局页面（不属于任何Tab）==========
                    // 这些页面直接挂在根导航图下，不会显示底部导航栏

                    composable(RoutePath.QuickAdd.route) {
                        AddQuickHomePage(
                            modifier = Modifier.fillMaxSize(), navController = navController
                        )
                    }

                    composable(
                        // 查看和编辑快捷记录页面，带参数：快捷记录ID
                        route = "${RoutePath.WatchAndEditQuick.route}?quickId={quickId}",
                        arguments = listOf(
                            navArgument("quickId") { type = NavType.IntType })
                    ) {
                        WatchAndEditQuickPage(
                            modifier = Modifier.fillMaxSize(), navController = navController
                        )
                    }

                    composable(RoutePath.EditProduct.route) {
                        EditProductPage(
                            modifier = Modifier.fillMaxSize(), navController = navController
                        )
                    }

                    composable(RoutePath.EditSize.route) {
                        EditSizePage(
                            modifier = Modifier.fillMaxSize(), navController = navController
                        )
                    }

                    composable(RoutePath.EditPayWay.route) {
                        EditPayWayPage(
                            modifier = Modifier.fillMaxSize(), navController = navController
                        )
                    }

                    composable(RoutePath.EditTransactionCategory.route) {
                        EditTransactionCategoryPage(
                            modifier = Modifier.fillMaxSize(), navController = navController
                        )
                    }

                    composable(
                        // 编辑子分类页面，带参数：分类ID、分类名称
                        "${RoutePath.EditSubCategory.route}?categoryId={categoryId}&categoryName={categoryName}",
                        arguments = listOf(
                            navArgument("categoryId") { type = NavType.IntType },
                            navArgument("categoryName") { type = NavType.StringType })
                    ) {
                        EditSubCategoryPage(
                            modifier = Modifier.fillMaxSize(), navController = navController
                        )
                    }
                }
            }

            // ========== 底部导航栏 ==========
            // 仅在Tab根页面显示底部导航栏（isTabRoute为true）
            if (isTabRoute) {
                CustomBottomBar(
                    modifier = Modifier.navigationBarsPadding(), // 处理手势导航安全区
                    selectedTab = selectedTab, onTabClick = { tab ->
                        // Tab点击事件处理
                        navController.navigate(tab.route) {
                            launchSingleTop = true    // 单例模式，避免重复创建
                            restoreState = true       // 恢复页面状态
                            popUpTo(navController.graph.id) { saveState = true } // 清除其他页面，保留状态
                        }
                    }, onQuickAddClick = {
                        // 快捷添加按钮点击事件，跳转到全局添加页面
                        navController.navigate(RoutePath.QuickAdd.route)
                    })
            }
        }
    }
}

/**
 * 记住当前选中的底部Tab。
 * 核心逻辑：
 * 1. 监听当前导航路由变化
 * 2. 根据路由前缀判断属于哪个Tab
 * 3. 判断当前是否处于Tab根页面（用于控制底部栏显示）
 *
 * @param navController 导航控制器，用于获取当前路由
 * @param defaultTab 默认选中的Tab，通常为首页对应的Tab
 * @return Pair<选中的Tab路由, 是否处于Tab根页面>
 */
@Composable
private fun rememberSelectedTab(
    navController: NavController, defaultTab: String = RoutePath.BookGraph.route
): Pair<String, Boolean> {
    val currentEntry by navController.currentBackStackEntryAsState() // 监听当前路由变化
    val currentRoute = currentEntry?.destination?.route ?: "" // 获取完整路由路径
    val routePrefix = currentRoute.split("/").firstOrNull() ?: defaultTab // 提取路由前缀

    var selectedTab by remember { mutableStateOf(defaultTab) } // 当前选中的Tab
    var isTabRoute by remember { mutableStateOf(true) } // 是否处于Tab根页面

    // 逻辑判断：如果当前路由前缀在底部Tab列表中，更新选中的Tab
    if (routePrefix in bottomTabGraphs) {
        selectedTab = routePrefix
    }

    // 判断是否为Tab根页面：当前路由是否在tabRootRoutes列表中
    isTabRoute = currentRoute in tabRootRoutes

    LogUtils.d("selectedTab: $selectedTab , isTabRoute: $isTabRoute , currentRoute: $currentRoute")

    return Pair(selectedTab, isTabRoute)
}

@Composable
@Preview(showBackground = true)
private fun HomeBookAppPreview() {
    HomeBookTheme {
        HomeBookApp()
    }
}