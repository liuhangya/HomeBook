package com.fanda.homebook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fanda.homebook.R

/**
 * 带边框的彩色圆形组件
 *
 * 常用于表示颜色选择器、标签、状态指示器等场景
 *
 * @param modifier 修饰符，用于自定义布局或添加额外属性
 * @param color 圆形内部填充颜色，默认为透明
 * @param borderColor 边框颜色，默认为颜色资源 R.color.color_CCFFFFFF（半透明白色）
 * @param borderWidth 边框宽度，默认为 1.dp
 * @param size 圆形组件的整体尺寸，默认为 16.dp
 */
@Composable fun ColoredCircleWithBorder(
    modifier: Modifier = Modifier, color: Color = Color.Transparent, borderColor: Color = colorResource(id = R.color.color_CCFFFFFF), borderWidth: Dp = 1.dp, size: Dp = 16.dp
) {
    Box(
        // 按顺序应用多个修饰符：
        // 1. 设置组件大小
        // 2. 裁剪为圆形形状
        // 3. 设置背景填充颜色
        // 4. 添加圆形边框
        modifier = modifier
            .size(size)                    // 设置组件尺寸
            .clip(CircleShape)             // 裁剪为圆形
            .background(color)             // 设置填充背景色
            .border(
                width = borderWidth,       // 边框宽度
                color = borderColor,       // 边框颜色
                shape = CircleShape        // 边框形状（与裁剪形状一致）
            )
    )
    // Box组件为空容器，仅展示颜色和边框效果
}