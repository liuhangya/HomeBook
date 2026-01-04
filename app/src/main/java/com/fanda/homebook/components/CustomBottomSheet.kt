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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.fanda.homebook.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch


@Composable fun CustomBottomSheet(
    visible: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit
) {
    if (visible){
        BackHandler { onDismiss() }
    }
    // 全屏容器（始终存在，用于布局）
    Box(modifier = Modifier.fillMaxSize()) {
        // ✅ 背景 scrim：只做淡入淡出
        AnimatedVisibility(
            visible = visible, enter = fadeIn(animationSpec = tween(200)), exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() })
        }

        // ✅ BottomSheet 内容：只做滑入滑出
        AnimatedVisibility(
            visible = visible, enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(300, delayMillis = 50) // 可加轻微延迟更自然
            ), exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight }, animationSpec = tween(250)
            ), modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // 使用 Box 自定义背景
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