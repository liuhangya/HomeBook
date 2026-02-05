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

data class BottomTabEntity(
    val route: String, @DrawableRes val icon: Int, @DrawableRes val iconSelected: Int, var selected: Boolean = false
)


/*
* 底部导航栏
* */
@Composable fun CustomBottomBar(
    selectedTab: String, onTabClick: (BottomTabEntity) -> Unit, onQuickAddClick: () -> Unit, modifier: Modifier = Modifier
) {
    val leftTabs = listOf(
        BottomTabEntity(RoutePath.BookGraph.route, R.mipmap.icon_book, R.mipmap.icon_book_selected),
        BottomTabEntity(RoutePath.MoneyGraph.route, R.mipmap.icon_dashboard, R.mipmap.icon_dashboard_selected)
    )
    val rightTabs = listOf(
        BottomTabEntity(RoutePath.ClosetGraph.route, R.mipmap.icon_closet, R.mipmap.icon_closet_selected),
        BottomTabEntity(RoutePath.StockGraph.route, R.mipmap.icon_stock, R.mipmap.icon_stock_selected)
    )

    // 用于按压时的交互动画
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 点击时的缩放值
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f, animationSpec = tween(durationMillis = 100), label = ""
    )

    Card(
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp, 16.dp, 16.dp)
            .height(64.dp),
        colors = CardDefaults.cardColors().copy(containerColor = colorResource(R.color.color_99FFFFFF)),
        border = BorderStroke(1.dp, color = Color.White)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()
        ) {
            leftTabs.forEach { tab ->
                BottomBarItem(tab = tab, isSelected = tab.route == selectedTab, onClick = {
                    onTabClick(tab)
                }, modifier = Modifier.weight(1f))
            }

            Image(
                painter = painterResource(id = R.mipmap.icon_quick_add),
                contentDescription = "记一笔",
                modifier = Modifier
                    .size(56.dp)
                    .scale(scale)
                    .weight(1f)
                    .clickable(interactionSource = interactionSource, indication = null, onClick = {
                        onQuickAddClick()
                    })
            )

            rightTabs.forEach { tab ->
                BottomBarItem(tab = tab, isSelected = tab.route == selectedTab, onClick = {
                    onTabClick(tab)
                }, modifier = Modifier.weight(1f))
            }
        }
    }
}


@Composable private fun BottomBarItem(
    tab: BottomTabEntity, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = rememberRipple( // 圆形涟漪效果
                    bounded = true, radius = 28.dp / 2, color = Color.Gray
                ), onClick = onClick
            ), contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = if (isSelected) tab.iconSelected else tab.icon), contentDescription = tab.route, modifier = Modifier.size(28.dp)
        )
    }
}

@Composable @Preview(showBackground = true) fun CustomBottomBarPreview() {
    HomeBookTheme {
        CustomBottomBar( selectedTab = RoutePath.BookGraph.route, onTabClick = {}, onQuickAddClick = {})
    }
}