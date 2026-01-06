package com.fanda.homebook.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.fanda.homebook.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable fun CustomBottomSheet2(
    visible: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit
) {

    if (!visible) return // ✅ 真正的销毁点：动画结束后才 return

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        // BackHandler 仍需要（Dialog 不自动处理返回键？其实会，但保留更安全）
        BackHandler { onDismiss() }

        // 使用 Box 模拟全屏容器（实际是 Dialog 内部）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(), contentAlignment = Alignment.BottomCenter
        ) {
            // ✅ 背景 scrim
            Box(modifier = Modifier
                .matchParentSize()
                .clickable(
                    // 去掉默认的点击效果
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() })

            // ✅ BottomSheet 内容（保持原有动画）
            AnimatedVisibility(
                visible = visible, // 因为外层已控制 visible，这里恒为 true
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(300, delayMillis = 50)
                ), exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(250)
                )
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 135.dp, max = 480.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.color_E3EBF5), Color.White
                            )
                        ), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(1.dp, color = Color.White)
                    .clickable(enabled = false) {}) {
                    content()
                }
            }
        }
    }
}