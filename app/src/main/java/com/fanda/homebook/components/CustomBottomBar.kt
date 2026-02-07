package com.fanda.homebook.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fanda.homebook.R
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 底部标签页实体类
 *
 * @param route 路由路径，用于标识和导航
 * @param icon 未选中状态的图标资源ID
 * @param iconSelected 选中状态的图标资源ID
 * @param selected 当前是否选中，默认为false
 */
data class BottomTabEntity(
    val route: String, @DrawableRes val icon: Int, @DrawableRes val iconSelected: Int, var selected: Boolean = false
)

/*
* 自定义底部导航栏组件
*
* 包含四个常规标签页和一个居中的快速添加按钮
*
* @param selectedTab 当前选中的标签页路由路径
* @param onTabClick 标签页点击回调函数，接收被点击的BottomTabEntity
* @param onQuickAddClick 快速添加按钮点击回调函数
* @param modifier 修饰符，用于自定义整体布局
*/
@Composable fun CustomBottomBar(
    selectedTab: String, onTabClick: (BottomTabEntity) -> Unit, onQuickAddClick: () -> Unit, modifier: Modifier = Modifier
) {
    // 左侧标签页列表：账本和图表
    val leftTabs = listOf(
        BottomTabEntity(RoutePath.BookGraph.route, R.mipmap.icon_book, R.mipmap.icon_book_selected),
        BottomTabEntity(RoutePath.MoneyGraph.route, R.mipmap.icon_dashboard, R.mipmap.icon_dashboard_selected)
    )

    // 右侧标签页列表：衣柜和库存
    val rightTabs = listOf(
        BottomTabEntity(RoutePath.ClosetGraph.route, R.mipmap.icon_closet, R.mipmap.icon_closet_selected),
        BottomTabEntity(RoutePath.StockGraph.route, R.mipmap.icon_stock, R.mipmap.icon_stock_selected)
    )

    // 用于处理按压状态的交互源
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按压时的缩放动画：按下时缩小到80%，释放时恢复原状
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f, animationSpec = tween(durationMillis = 100), // 100毫秒动画时长
        label = "buttonScaleAnimation"
    )

    // 底部导航栏主容器 - 使用Card实现毛玻璃效果
    Card(
        shape = MaterialTheme.shapes.large, // 使用主题的大圆角形状
        modifier = modifier
            .fillMaxWidth()                         // 填充宽度
            .padding(16.dp, 0.dp, 16.dp, 16.dp)    // 左右边距16dp，底部边距16dp
            .height(64.dp),                        // 固定高度
        colors = CardDefaults.cardColors().copy(
            containerColor = colorResource(R.color.color_99FFFFFF) // 半透明白色背景
        ), border = BorderStroke(1.dp, color = Color.White) // 白色边框
    ) {
        // 水平排列所有按钮
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,  // 均匀分布
            verticalAlignment = Alignment.CenterVertically,   // 垂直居中
            modifier = Modifier.fillMaxSize()                 // 填满父容器
        ) {
            // 左侧两个标签页
            leftTabs.forEach { tab ->
                BottomBarItem(
                    tab = tab, isSelected = tab.route == selectedTab,    // 根据路由判断是否选中
                    onClick = { onTabClick(tab) }, modifier = Modifier.weight(1f)            // 等宽分配
                )
            }

            // 居中快速添加按钮
            Image(
                painter = painterResource(id = R.mipmap.icon_quick_add), contentDescription = "记一笔",  // 无障碍描述
                modifier = Modifier
                    .size(56.dp)                          // 按钮尺寸
                    .scale(scale)                         // 应用缩放动画
                    .weight(1f)                           // 等宽分配
                    .clickable(
                        interactionSource = interactionSource, indication = null,                // 不使用默认波纹效果
                        onClick = { onQuickAddClick() })
            )

            // 右侧两个标签页
            rightTabs.forEach { tab ->
                BottomBarItem(
                    tab = tab, isSelected = tab.route == selectedTab, onClick = { onTabClick(tab) }, modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 底部导航栏单个标签项组件
 *
 * @param tab 标签页数据实体
 * @param isSelected 是否选中状态
 * @param onClick 点击回调函数
 * @param modifier 修饰符，用于自定义布局
 */
@Composable private fun BottomBarItem(
    tab: BottomTabEntity, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)                     // 固定尺寸
            .clip(CircleShape)               // 裁剪为圆形
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = rememberRipple(  // 圆形波纹效果
                    bounded = true,           // 限定在边界内
                    radius = 28.dp / 2,       // 波纹半径（按钮半径的一半）
                    color = Color.Gray        // 灰色波纹
                ), onClick = onClick
            ), contentAlignment = Alignment.Center  // 内容居中对齐
    ) {
        // 根据选中状态显示不同图标
        Image(
            painter = painterResource(id = if (isSelected) tab.iconSelected else tab.icon), contentDescription = tab.route,   // 使用路由作为无障碍描述
            modifier = Modifier.size(28.dp)   // 图标大小
        )
    }
}

/**
 * 预览函数，用于在Android Studio中预览底部导航栏
 */
@Composable @Preview(showBackground = true) fun CustomBottomBarPreview() {
    HomeBookTheme {
        CustomBottomBar(
            selectedTab = RoutePath.BookGraph.route,  // 预览选中的标签页
            onTabClick = {},                         // 空回调函数
            onQuickAddClick = {}                      // 空回调函数
        )
    }
}