package com.fanda.homebook.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.fanda.homebook.R

/**
 * 自定义下拉菜单组件（带渐变背景）
 *
 * 仿Material Design的DropdownMenu，但具有自定义的渐变背景和动画效果
 *
 * @param dpOffset 菜单位置偏移量，相对于锚点
 * @param modifier 修饰符，用于自定义菜单容器样式
 * @param width 菜单宽度，默认为136.dp
 * @param expanded 控制菜单展开/收起状态
 * @param onDismissRequest 菜单关闭回调（点击外部或按返回键）
 * @param content 菜单内容区域，使用ColumnScope提供布局上下文
 */
@Composable fun CustomDropdownMenu(
    dpOffset: DpOffset, modifier: Modifier = Modifier, width: Dp = 136.dp, expanded: Boolean, onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit
) {
    // 处理返回键关闭菜单
    if (expanded) {
        BackHandler { onDismissRequest() }
    }

    // 如果菜单未展开，直接返回
    if (!expanded) {
        return
    }

    // 获取屏幕密度以进行dp到px的转换
    val density = LocalDensity.current
    val offset = with(density) {
        IntOffset(
            x = dpOffset.x.roundToPx(), y = dpOffset.y.roundToPx() // 转换为像素偏移量
        )
    }

    // 使用Popup组件实现悬浮菜单
    Popup(
        onDismissRequest = onDismissRequest, properties = PopupProperties(
            dismissOnClickOutside = true, // 点击外部关闭
            dismissOnBackPress = true     // 按返回键关闭
        ), offset = offset // 设置菜单位置
    ) {
        // 淡入淡出动画
        AnimatedVisibility(
            visible = expanded, enter = fadeIn(animationSpec = tween(0)),      // 立即显示
            exit = fadeOut(animationSpec = tween(200))     // 200ms淡出
        ) {
            // 菜单主容器：渐变背景 + 圆角 + 阴影
            Column(
                modifier = modifier
                    .width(width) // 固定宽度
                    .shadow(
                        elevation = 8.dp, shape = RoundedCornerShape(16.dp) // 圆角阴影
                    )
                    .background(
                        // 垂直渐变：浅蓝色到白色
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.color_E3EBF5), // 顶部：浅蓝色
                                Color.White                          // 底部：白色
                            )
                        ), shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp, color = Color.White, // 白色边框
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 8.dp) // 垂直内边距
            ) {
                // 调用传入的内容
                content()
            }
        }
    }
}

/**
 * 自定义菜单项（带选择状态指示器）
 *
 * 包含左侧文本和右侧选中图标
 *
 * @param text 菜单项文本
 * @param selected 是否选中状态
 * @param onClick 点击回调函数
 */
@Composable fun MenuItem(
    text: String, selected: Boolean, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(end = 12.dp), // 右侧内边距为选中图标留空间
        horizontalArrangement = Arrangement.SpaceBetween, // 两端对齐
        verticalAlignment = Alignment.CenterVertically    // 垂直居中
    ) {
        // 菜单文本
        Text(
            text = text, color = Color.Black, fontSize = 14.sp, style = TextStyle.Default, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )

        // 选中状态图标（仅在选中时显示）
        if (selected) {
            Image(
                painter = painterResource(id = R.mipmap.icon_selected), contentDescription = "选中", // 无障碍描述
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * 自定义居中对齐菜单项
 *
 * 文本居中对齐，无选中图标
 *
 * @param text 菜单项文本
 * @param onClick 点击回调函数
 */
@Composable fun MenuCenterItem(
    text: String, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), horizontalArrangement = Arrangement.Center,     // 居中对齐
        verticalAlignment = Alignment.CenterVertically  // 垂直居中
    ) {
        Text(
            text = text, color = Color.Black, fontSize = 14.sp, style = TextStyle.Default, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}

/**
 * 预览函数，用于在Android Studio中预览下拉菜单
 */
@Composable @Preview(showBackground = true) fun CustomGradientDropdownMenuPreview() {
    // 预览一个包含三个居中对齐菜单项的下拉菜单
    CustomDropdownMenu(
        dpOffset = DpOffset(0.dp, 0.dp), // 无偏移
        expanded = true, onDismissRequest = {}) {
        MenuCenterItem(text = "菜单项1") {}
        MenuCenterItem(text = "菜单项2") {}
        MenuCenterItem(text = "菜单项3") {}
    }
}