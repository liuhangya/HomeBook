package com.fanda.homebook.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fanda.homebook.R
import kotlinx.coroutines.delay

@Composable
fun CustomRightDrawerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    // 用于控制 Dialog 是否真正渲染（动画结束后才销毁）
    var shouldShowDialog by remember { mutableStateOf(false) }

    // 当 visible 变为 true 时，立即显示 Dialog
    LaunchedEffect(visible) {
        if (visible) {
            shouldShowDialog = true
        } else {
            // 先不立即隐藏，等退出动画完成后再销毁
            // 这里可以加一个短延迟（或监听动画结束）
            delay(250) // 与 exit 动画时长一致
            shouldShowDialog = false
        }
    }

    if (!shouldShowDialog) return // ✅ 真正的销毁点：动画结束后才 return

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            // 遮罩
            AnimatedVisibility(
                visible = visible, // 注意：这里仍用原始 visible
                enter = androidx.compose.animation.fadeIn(tween(100)),
                exit = androidx.compose.animation.fadeOut(tween(100))
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onDismiss() }
                )
            }

            // 抽屉内容（带动画）
            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(250)
                )
            ) {
                val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
                Box(
                    modifier = Modifier
                        .width(screenWidthDp * 2 / 3)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorResource(R.color.color_E3EBF5), Color.White
                                )
                            )
                        )
                ) {
                    content()
                }
            }
        }
    }
}