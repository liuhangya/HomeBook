package com.fanda.homebook.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fanda.homebook.R
import kotlinx.coroutines.delay

/**
 * 自定义底部弹出式对话框（仿 BottomSheet）
 *
 * 具有平滑的滑动动画和渐变背景，适合展示选项菜单或表单内容
 *
 * @param visible 控制对话框的显示/隐藏状态
 * @param onDismiss 对话框关闭回调函数，点击外部遮罩或按返回键时触发
 * @param content 对话框内容区域的可组合函数
 */
@Composable fun CustomBottomSheet(
    visible: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit
) {
    // 控制对话框的实际显示状态（用于动画协调）
    var shouldShowDialog by remember { mutableStateOf(false) }

    // 垂直偏移量动画：从底部滑入/滑出
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 600.dp, // 可见时位于屏幕底部，不可见时在屏幕外
        animationSpec = tween(durationMillis = 300), // 300毫秒动画时长
        label = "slide_animation"
    )

    // 遮罩层透明度动画
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.5f else 0f,     // 可见时半透明遮罩，不可见时完全透明
        animationSpec = tween(durationMillis = 300), label = "scrim_alpha"
    )

    // 监听visible状态变化，协调显示/隐藏时机
    LaunchedEffect(visible) {
        if (visible) {
            // 立即显示对话框（开始滑入动画）
            shouldShowDialog = true
        } else {
            // 等待动画完成后再真正隐藏对话框
            delay(300) // 等待300毫秒动画完成
            shouldShowDialog = false
        }
    }

    // 如果不需要显示对话框，直接返回
    if (!shouldShowDialog) return

    // 使用Dialog作为容器，确保显示在系统UI上方
    Dialog(
        onDismissRequest = onDismiss, // 点击外部关闭
        properties = DialogProperties(
            usePlatformDefaultWidth = false,   // 不使用平台默认宽度
            decorFitsSystemWindows = false     // 对话框内容延伸到系统UI下方
        )
    ) {
        // 处理物理返回键
        BackHandler { onDismiss() }

        // 主容器：全屏布局
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(), // 考虑系统导航栏
            contentAlignment = Alignment.BottomCenter
        ) {
            // 遮罩层：半透明黑色背景，点击关闭
            Box(
                modifier = Modifier
                    .matchParentSize() // 填充整个对话框
                .clickable(
                    // 禁用默认点击效果
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) { onDismiss() }

            )

            // 底部弹窗内容区域
            Box(
                modifier = Modifier
                    .fillMaxWidth() // 填充宽度
                .heightIn(min = 135.dp, max = 480.dp) // 高度范围限制
                .offset(y = offsetY) // 应用垂直偏移动画
                .background(
                    // 垂直渐变背景：浅蓝色到白色
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.color_E3EBF5), // 顶部：浅蓝色
                            Color.White                          // 底部：白色
                        )
                    ), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) // 裁剪圆角
                .border(1.dp, color = Color.White) // 白色边框
                .clickable(enabled = false) {} // 禁用点击穿透（防止点击内容区域关闭对话框）
            ) {
                // 显示传入的内容
                content()
            }
        }
    }
}