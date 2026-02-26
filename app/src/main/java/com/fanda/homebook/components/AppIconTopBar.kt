package com.fanda.homebook.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R

/**
 * 自定义顶部应用栏组件，支持图标和文本按钮
 *
 * @param title 居中显示的标题文本
 * @param modifier 修饰符，用于自定义布局
 * @param showBackButton 是否显示返回按钮，默认为true
 * @param backIconPainter 返回按钮的图标资源，默认为R.mipmap.icon_back
 * @param onBackClick 返回按钮点击回调函数，可为null
 * @param rightText 右侧文本按钮的文本，可为null（不显示）
 * @param rightIconPainter 右侧图标按钮的图标资源，可为null（不显示）
 * @param rightNextIconPainter 右侧第二个图标按钮的图标资源，可为null（不显示）
 * @param onRightActionClick 右侧动作点击回调（isTextButton为true时表示文本按钮）
 * @param onRightNextActionClick 右侧第二个图标按钮点击回调
 * @param titleStyle 标题文本样式，默认18sp黑色中等字体
 * @param backgroundColor 背景颜色，默认为透明
 * @param contentColor 内容颜色，默认为MaterialTheme中的onSurface颜色
 */
@Composable fun TopIconAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    backIconPainter: Painter = painterResource(id = R.mipmap.icon_back),
    onBackClick: (() -> Unit)? = null,
    rightText: String? = null,
    rightIconPainter: Painter? = null,
    rightNextIconPainter: Painter? = null,
    onRightActionClick: ((isTextButton: Boolean) -> Unit)? = null,
    onRightNextActionClick: (() -> Unit)? = null,
    // 样式参数
    titleStyle: TextStyle = TextStyle.Default.copy(
        fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Medium
    ),
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    // 主容器：固定高度，带背景和内边距
    Box(
        modifier = modifier
            .height(64.dp) // 固定高度
            .background(color = backgroundColor) // 背景颜色
            .padding(start = 4.dp, end = 12.dp) // 水平内边距
            .fillMaxWidth() // 填满宽度
    ) {
        // 左侧：返回按钮
        if (showBackButton) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier
                    .size(48.dp) // 固定大小
                    .clip(CircleShape) // 圆形裁剪
                    .align(Alignment.CenterStart) // 居左对齐
                    .clickable(enabled = onBackClick != null) { // 点击事件
                        onBackClick?.invoke()
                    }) {
                Image(
                    painter = backIconPainter, contentDescription = "Back", // 无障碍描述
                    contentScale = ContentScale.Fit, // 缩放方式
                    modifier = Modifier.size(24.dp) // 图标大小
                )
            }
        }

        // 居中：标题文本
        Text(
            text = title, style = titleStyle, // 文本样式
            color = contentColor, // 文本颜色
            maxLines = 1, // 单行显示
            modifier = Modifier.align(Alignment.Center) // 居中对齐
        )

        // 右侧：动作区域（图标/文本按钮）
        Row(
            horizontalArrangement = Arrangement.SpaceBetween, // 水平排列
            verticalAlignment = Alignment.CenterVertically, // 垂直居中
            modifier = Modifier.align(Alignment.CenterEnd) // 居右对齐
        ) {
            // 第一个图标按钮（最右侧）
            if (rightIconPainter != null) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .size(44.dp) // 固定大小
                    .then(
                        if (onRightActionClick != null) { // 条件添加点击事件
                        Modifier.clickable { onRightActionClick(false) } // false表示图标按钮
                    } else Modifier)) {
                    Image(
                        painter = rightIconPainter, contentDescription = "Action", // 无障碍描述
                        contentScale = ContentScale.Fit, // 缩放方式
                        modifier = Modifier.size(24.dp) // 图标大小
                    )
                }
            }

            // 第二个图标按钮（第一个的左侧）
            if (rightNextIconPainter != null) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .size(44.dp) // 固定大小
                    .then(
                        if (onRightNextActionClick != null) { // 条件添加点击事件
                        Modifier.clickable { onRightNextActionClick() }
                    } else Modifier)) {
                    Image(
                        painter = rightNextIconPainter, contentDescription = "Action", // 无障碍描述
                        contentScale = ContentScale.Fit, // 缩放方式
                        modifier = Modifier.size(24.dp) // 图标大小
                    )
                }
            }

            // 文本按钮（最左侧）
            if (!rightText.isNullOrBlank()) {
                TextButton(
                    onClick = {
                        onRightActionClick?.invoke(true) // true表示文本按钮
                    }) {
                    Text(
                        text = rightText, style = TextStyle.Default.copy(
                            fontSize = 16.sp, color = colorResource(id = R.color.color_333333) // 使用资源颜色
                        ),
                        // 这里原本有条件修饰符但逻辑相同，已简化
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览组件效果
 */
@Composable @Preview(showBackground = true) private fun CustomTopAppBarPreview() {
    // 预览示例：显示所有可能的UI元素
    TopIconAppBar(
        title = "记一笔", // 标题
        onBackClick = {}, // 返回按钮回调
        rightIconPainter = painterResource(R.mipmap.icon_add_grady), // 右侧图标
        rightNextIconPainter = painterResource(R.mipmap.icon_edit_menu), // 右侧第二个图标
        onRightActionClick = {}, // 右侧动作回调
        backIconPainter = painterResource(R.mipmap.icon_back), // 返回图标
    )
}