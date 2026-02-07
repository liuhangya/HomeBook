package com.fanda.homebook.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fanda.homebook.R
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 确认对话框组件
 *
 * 用于展示需要用户确认的操作，包含标题、取消和确定按钮
 *
 * @param title 对话框标题文本
 * @param modifier 修饰符，用于自定义对话框内容区域的布局
 * @param cancelText 取消按钮文本，默认为"取消"
 * @param confirmText 确认按钮文本，默认为"确定"
 * @param onDismissRequest 对话框关闭回调，点击取消按钮或外部区域时触发
 * @param onConfirm 确认按钮点击回调，执行确认操作
 */
@Composable fun ConfirmDialog(
    title: String, modifier: Modifier = Modifier, cancelText: String = "取消", confirmText: String = "确定", onDismissRequest: () -> Unit, onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest, // 对话框关闭回调
        properties = DialogProperties(
            dismissOnBackPress = true,     // 允许按返回键关闭
            dismissOnClickOutside = false   // 禁止点击外部关闭（只能通过按钮操作）
        )
    ) {
        // 渐变圆角背景容器
        GradientRoundedBoxWithStroke(
            modifier = modifier, colors = listOf(
                colorResource(R.color.color_E3EBF5), // 渐变色1：浅蓝色
                Color.White                           // 渐变色2：白色
            )
        ) {
            // 对话框内容区域
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, // 水平居中
                modifier = Modifier.padding(horizontal = 24.dp)    // 水平内边距
            ) {
                // 标题文本
                Text(
                    text = title, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(
                        horizontal = 16.dp,  // 水平内边距
                        vertical = 40.dp     // 垂直内边距（上下）
                    )
                )

                // 按钮行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()               // 填充宽度
                        .padding(bottom = 20.dp)      // 底部内边距
                ) {
                    // 取消按钮
                    SelectableRoundedButton(
                        interaction = true,            // 启用交互
                        text = cancelText,            // 按钮文本
                        selected = false,             // 非选中状态（默认样式）
                        onClick = onDismissRequest,   // 点击触发关闭对话框
                        cornerSize = 27.dp,           // 圆角大小
                        contentPadding = PaddingValues(
                            horizontal = 47.dp,       // 水平内边距
                            vertical = 15.dp          // 垂直内边距
                        ), fontSize = 16.sp              // 字体大小
                    )

                    // 按钮间距
                    Spacer(modifier = Modifier.width(12.dp))

                    // 确定按钮
                    SelectableRoundedButton(
                        text = confirmText,           // 按钮文本
                        selected = true,              // 选中状态（高亮样式）
                        onClick = { onConfirm() },    // 点击触发确认操作
                        cornerSize = 27.dp,           // 圆角大小
                        contentPadding = PaddingValues(
                            horizontal = 47.dp,       // 水平内边距
                            vertical = 15.dp          // 垂直内边距
                        ), fontSize = 16.sp              // 字体大小
                    )
                }
            }
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览确认对话框
 */
@Composable @Preview(showBackground = true) fun ConfirmDialogPreview() {
    HomeBookTheme {
        ConfirmDialog(
            onDismissRequest = {},  // 空关闭回调
            onConfirm = {},         // 空确认回调
            title = "添加分类"       // 预览标题
        )
    }
}