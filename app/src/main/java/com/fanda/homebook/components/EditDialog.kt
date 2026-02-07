package com.fanda.homebook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fanda.homebook.R
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 编辑对话框组件
 *
 * 用于弹出一个带有文本输入框的对话框，支持输入内容编辑和字符长度限制
 *
 * @param title 对话框标题文本
 * @param modifier 修饰符，用于自定义对话框内容区域的布局
 * @param placeholder 输入框占位文本，默认为"请输入"
 * @param value 初始输入值
 * @param onDismissRequest 对话框关闭回调，点击取消按钮或外部区域时触发
 * @param onConfirm 确认按钮点击回调，接收输入框的最终文本
 * @param maxChar 最大字符限制，默认为4个字符（仅在showSuffix为true时生效）
 * @param showSuffix 是否显示字符计数后缀（格式：当前长度/最大长度），默认为true
 */
@Composable fun EditDialog(
    title: String, modifier: Modifier = Modifier, placeholder: String = "请输入", value: String, onDismissRequest: () -> Unit, onConfirm: (String) -> Unit, maxChar: Int = 4, showSuffix: Boolean = true
) {
    // 内部状态管理：存储当前输入值
    var newValue by remember { mutableStateOf(value) }

    Dialog(
        onDismissRequest = onDismissRequest, properties = DialogProperties(
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
                    text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(
                        top = 24.dp,   // 顶部内边距
                        bottom = 27.dp // 底部内边距
                    )
                )

                // 文本输入框
                TextField(
                    shape = RoundedCornerShape(12.dp), // 圆角形状
                    colors = TextFieldDefaults.colors().copy(
                        focusedContainerColor = Color.White,        // 聚焦时背景色
                        unfocusedContainerColor = Color.White,      // 未聚焦时背景色
                        focusedIndicatorColor = Color.Transparent,  // 聚焦时下划线透明
                        unfocusedIndicatorColor = Color.Transparent // 未聚焦时下划线透明
                    ), placeholder = {
                        // 占位符文本
                        Text(
                            text = placeholder, fontSize = 16.sp, color = colorResource(id = R.color.color_84878C) // 浅灰色
                        )
                    }, value = newValue, onValueChange = { newText ->
                        // 文本变化处理
                        if (showSuffix) {
                            // 有字符限制时：限制最大长度
                            if (newText.length <= maxChar) {
                                newValue = newText
                            }
                        } else {
                            // 无字符限制时：直接更新
                            newValue = newText
                        }
                    }, singleLine = true, // 单行输入
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp) // 固定高度
                        .background(Color.White, shape = RoundedCornerShape(12.dp)), // 白色背景
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done // 输入法动作：完成
                    ), textStyle = TextStyle.Default.copy(
                        fontSize = 16.sp,
                        color = Color.Black,
                    ), suffix = {
                        // 后缀：字符计数（仅在showSuffix为true时显示）
                        if (showSuffix) {
                            Text(
                                text = "${newValue.length}/$maxChar", // 当前长度/最大长度
                                fontSize = 16.sp, color = colorResource(id = R.color.color_56585B) // 深灰色
                            )
                        }
                    })

                // 按钮行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 20.dp), // 上下内边距
                    horizontalArrangement = Arrangement.Center // 居中对齐
                ) {
                    // 取消按钮
                    SelectableRoundedButton(
                        interaction = true,            // 启用交互
                        text = "取消",                 // 按钮文本
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
                        text = "确定",                 // 按钮文本
                        selected = true,              // 选中状态（高亮样式）
                        onClick = { onConfirm(newValue) }, // 点击触发确认，传递当前输入值
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
 * 预览函数，用于在Android Studio中预览编辑对话框
 */
@Composable @Preview(showBackground = true) fun EditDialogPreview() {
    HomeBookTheme {
        EditDialog(
            onDismissRequest = {},  // 空关闭回调
            onConfirm = {},         // 空确认回调
            title = "添加分类",      // 预览标题
            value = ""              // 空初始值
        )
    }
}