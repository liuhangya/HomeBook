package com.fanda.homebook.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R

/**
 * 可选择圆角按钮组件
 *
 * @param text 按钮显示的文本
 * @param selected 按钮是否处于选中状态
 * @param modifier 修饰符，用于自定义按钮布局
 * @param onClick 按钮点击回调函数
 * @param contentPadding 按钮内边距，默认为水平8dp，垂直4dp
 * @param cornerSize 圆角大小，默认为4.dp
 * @param fontSize 文本字体大小，默认为14.sp
 * @param selectedBackgroundColor 选中状态的背景颜色，默认为黑色
 * @param unselectedBackgroundColor 未选中状态的背景颜色，默认为color_D7DEE9
 * @param selectedContentColor 选中状态的文本颜色，默认为白色
 * @param unselectedContentColor 未选中状态的文本颜色，默认为黑色
 * @param imageRes 图标资源ID（可选），显示在文本右侧
 * @param interaction 是否启用点击交互效果，true时显示默认涟漪效果，false时移除点击效果
 */
@Composable fun SelectableRoundedButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    cornerSize: Dp = 4.dp,
    fontSize: TextUnit = 14.sp,
    selectedBackgroundColor: Color = Color.Black,
    unselectedBackgroundColor: Color = colorResource(R.color.color_D7DEE9),
    selectedContentColor: Color = Color.White,
    unselectedContentColor: Color = Color.Black,
    @DrawableRes imageRes: Int? = null,
    interaction: Boolean = false
) {
    // 使用Box容器实现自定义圆角按钮
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerSize)) // 裁剪为圆角矩形
            .background(if (selected) selectedBackgroundColor else unselectedBackgroundColor) // 根据选中状态设置背景色
            .then(
                // 根据interaction参数决定是否显示点击效果
                if (interaction) {
                    Modifier.clickable {
                        onClick()
                    }
                } else Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    onClick()
                }),
        contentAlignment = Alignment.Center, // 内容居中对齐
    ) {
        // 水平排列文本和图标
        Row(
            verticalAlignment = Alignment.CenterVertically, // 垂直居中对齐
            horizontalArrangement = Arrangement.Center,     // 水平居中对齐
            modifier = Modifier.padding(contentPadding)     // 应用内边距
        ) {
            // 按钮文本
            Text(
                text = text, fontSize = fontSize, color = if (selected) selectedContentColor else unselectedContentColor, // 根据选中状态设置文本颜色
                style = TextStyle.Default,  // 使用默认样式，避免字体间距问题
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal // 选中时使用中等字重
            )

            // 图标（可选）
            if (imageRes != null) {
                Image(
                    painter = painterResource(id = imageRes), contentDescription = null, // 无障码描述
                    modifier = Modifier.padding(start = 4.dp) // 图标左侧间距
                )
            }
        }
    }
}