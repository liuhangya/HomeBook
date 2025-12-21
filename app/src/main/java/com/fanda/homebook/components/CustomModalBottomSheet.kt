package com.fanda.homebook.components

import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.fanda.homebook.R

@Composable
fun SimpleBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val defaultColor = MaterialTheme.colorScheme.background

    LaunchedEffect(visible) {
        if (visible) {
            systemUiController.setStatusBarColor(
                color = Color.Black.copy(alpha = 0.3f),
                darkIcons = useDarkIcons
            )
            systemUiController.setNavigationBarColor(
                color = Color.Black.copy(alpha = 0.3f),
                darkIcons = useDarkIcons
            )
        } else {
            systemUiController.setStatusBarColor(
                color = defaultColor,
                darkIcons = useDarkIcons
            )
            systemUiController.setNavigationBarColor(
                color = defaultColor,
                darkIcons = useDarkIcons
            )
        }
    }

    // 全屏容器（始终存在，用于布局）
    Box(modifier = Modifier.fillMaxSize()) {
        // ✅ 背景 scrim：只做淡入淡出
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(0)),
            exit = fadeOut(animationSpec = tween(0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { onDismiss() }
            )
        }

        // ✅ BottomSheet 内容：只做滑入滑出
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(300, delayMillis = 50) // 可加轻微延迟更自然
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(250)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // 使用 Box 自定义背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.color_E3EBF5), // 顶部颜色（浅蓝）
                                Color.White  // 底部颜色（白）
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(1.dp, color = Color.White)
                    .clickable(enabled = false) {}
            ) {
                content()
            }
        }
    }
}

//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.nestedscroll.nestedScroll
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
//
//@Composable
//fun CustomModalBottomSheet(
//    visible: Boolean,
//    onDismiss: () -> Unit,
//    sheetShape: RoundedCornerShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
//    sheetMaxHeight: Dp = 400.dp,
//    content: @Composable () -> Unit
//) {
//    if (!visible) return
//
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(
//            usePlatformDefaultWidth = false, // ← 允许全宽
//            dismissOnBackPress = true,
//            dismissOnClickOutside = false
//        )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .clickable { onDismiss() }
//        ) {
//            AnimatedVisibility(
//                visible = visible,
//                enter = slideInVertically(
//                    initialOffsetY = { fullHeight -> fullHeight }, // 从底部外开始
//                    animationSpec = tween(1000)
//                ),
//                exit = slideOutVertically(
//                    targetOffsetY = { fullHeight -> fullHeight },
//                    animationSpec = tween(1000)
//                ),
//                modifier = Modifier.align(Alignment.BottomCenter)
//            ) {
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .heightIn(max = sheetMaxHeight),
//                    shape = sheetShape,
//                    color = Color.White,
//                    tonalElevation = 8.dp
//                ) {
//                    Column(modifier = Modifier.fillMaxWidth()) {
//                        content()
//                    }
//                }
//            }
//        }
//    }
//}