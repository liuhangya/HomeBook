package com.fanda.homebook

import android.widget.ImageButton
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.route.RoutePath

private data class BottomTabEntity(val route: String, @DrawableRes val icon: Int, @DrawableRes val iconSelected: Int, var selected: Boolean = false)

/*
* 应用入口
* */
@Composable fun HomeBookApp() {
    // 获取导航控制器
    val navController: NavHostController = rememberNavController()
    // 获取路由状态
    val currentRoute by navController.currentBackStackEntryAsState()

    Scaffold(bottomBar = {
        CustomBottomBar(navHostController = navController, currentRoute = currentRoute?.destination?.route ?: RoutePath.BOOK.route)
    }) { padding ->
        // 页面容器
        NavHost(
            navController = navController, startDestination = RoutePath.BOOK.route, modifier = Modifier.padding(padding)
        ) {
            // 账本页面
            composable(RoutePath.BOOK.route) {
                Text(text = "账本页面")
            }
            // 看板页面
            composable(RoutePath.DASHBOARD.route) {
                Text(text = "看板页面")
            }
            // 记一笔页面
            composable(RoutePath.QUICK_ADD.route) {
                Text(text = "记一笔页面")
            }
            // 衣橱页面
            composable(RoutePath.CLOSET.route) {
                Text(text = "衣橱页面")
            }
            // 囤货页面
            composable(RoutePath.STOCK.route) {
                Text(text = "囤货页面")
            }

        }
    }
}

/*
* 底部导航栏
* */
@Composable private fun CustomBottomBar(navHostController: NavHostController, currentRoute: String, modifier: Modifier = Modifier) {


    val leftTabs = listOf(
        BottomTabEntity(RoutePath.BOOK.route, R.mipmap.icon_book, R.mipmap.icon_book_selected), BottomTabEntity(RoutePath.DASHBOARD.route, R.mipmap.icon_dashboard, R.mipmap.icon_dashboard_selected)
    )

    val rightTabs = listOf(
        BottomTabEntity(RoutePath.CLOSET.route, R.mipmap.icon_closet, R.mipmap.icon_closet_selected), BottomTabEntity(RoutePath.STOCK.route, R.mipmap.icon_stock, R.mipmap.icon_stock_selected)
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 点击时的缩放值
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f, animationSpec = tween(durationMillis = 150), label = ""
    )

    Box(modifier = modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)) {
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, color = Color.White)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                leftTabs.forEach { tab ->
                    BottomBarItem(tab = tab, isSelected = tab.route == currentRoute, onClick = {
                        navHostController.navigate(tab.route) {
                            // 避免多次创建相同的页面入栈
                            launchSingleTop = true
                            // 保存页面的信息
                            restoreState = true
                        }
                    })
                }

                Image(
                    painter = painterResource(id = R.mipmap.icon_quick_add),
                    contentDescription = "记一笔",
                    modifier = Modifier
                        .size(56.dp)
                        .scale(scale)
                        .clickable(interactionSource = interactionSource, indication = null, onClick = {
                            navHostController.navigate(RoutePath.QUICK_ADD.route)
                        })

                )

                rightTabs.forEach { tab ->
                    BottomBarItem(tab = tab, isSelected = tab.route == currentRoute, onClick = {
                        navHostController.navigate(tab.route) {
                            // 避免多次创建相同的页面入栈
                            launchSingleTop = true
                            // 保存页面的信息
                            restoreState = true
                        }
                    })
                }


            }
        }
    }
}

@Composable private fun BottomBarItem(tab: BottomTabEntity, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = rememberRipple( // 圆形涟漪效果
                    bounded = true, radius = 28.dp / 2, color = Color.Gray
                ), onClick = onClick
            ), contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = if (isSelected) tab.iconSelected else tab.icon), contentDescription = tab.route
        )
    }
}



