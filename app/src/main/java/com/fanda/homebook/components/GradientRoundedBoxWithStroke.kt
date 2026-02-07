package com.fanda.homebook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fanda.homebook.R

/**
 * 带描边的圆角垂直渐变容器
 *
 * 一个通用的渐变背景容器，带有可自定义的圆角和描边，
 * 常用于对话框、卡片、弹窗等需要渐变背景的UI元素
 *
 * @param modifier 修饰符，用于自定义容器布局
 * @param colors 渐变颜色列表（至少两个颜色），默认垂直方向（从上到下）
 *               默认为 [color_66ffffff（半透明白色）, color_33ffffff（更透明白色）]
 * @param cornerRadius 圆角半径，默认为16.dp
 * @param strokeColor 描边颜色，默认为半透明白色（color_66ffffff）
 * @param strokeWidth 描边宽度，默认为1.dp
 * @param content 容器内容，可以是任意Composable组件
 *
 * @throws IllegalArgumentException 当colors列表少于两种颜色时抛出异常
 */
@Composable fun GradientRoundedBoxWithStroke(
    modifier: Modifier = Modifier, colors: List<Color> = listOf(
        colorResource(R.color.color_66ffffff), // 顶部颜色：66%透明度白色
        colorResource(R.color.color_33ffffff)  // 底部颜色：33%透明度白色
    ), cornerRadius: Dp = 16.dp, strokeColor: Color = colorResource(R.color.color_66ffffff), strokeWidth: Dp = 1.dp, content: @Composable () -> Unit = {}
) {
    // 参数验证：渐变至少需要两种颜色
    require(colors.size >= 2) { "渐变至少需要两种颜色" }

    // 定义圆角形状
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth() // 默认填充宽度
            .clip(shape) // 裁剪内容，防止溢出圆角
            .background(
                // 垂直渐变背景：从上到下
                Brush.verticalGradient(colors = colors)
            )
            .border(
                width = strokeWidth, // 描边宽度
                color = strokeColor, // 描边颜色
                shape = shape        // 描边形状（与裁剪形状一致）
            )
    ) {
        // 渲染传入的内容
        content()
    }
}