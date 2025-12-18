package com.fanda.homebook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
 * @param colors 渐变颜色列表（至少两个），默认垂直方向（top → bottom）
 * @param cornerRadius 圆角半径
 * @param strokeColor 描边颜色
 * @param strokeWidth 描边宽度
 * @param modifier 修饰符
 * @param content 内容（可选）
 */
@Composable fun GradientRoundedBoxWithStroke(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(colorResource(R.color.color_66ffffff), colorResource(R.color.color_33ffffff)),
    cornerRadius: Dp = 16.dp,
    strokeColor: Color = colorResource(R.color.color_66ffffff),
    strokeWidth: Dp = 1.dp,
    content: @Composable () -> Unit = {}
) {
    require(colors.size >= 2) { "渐变至少需要两种颜色" }
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape) // 裁剪内容，防止溢出圆角
            .background(Brush.verticalGradient(colors = colors)) // 垂直渐变色
            .border(
                width = strokeWidth, color = strokeColor, shape = shape
            )
    ) {
        content()
    }
}