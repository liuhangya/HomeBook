package com.fanda.homebook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fanda.homebook.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 自定义底部弹出式对话框（使用官方 ModalBottomSheet）
 *
 * 具有平滑的滑动动画和渐变背景，适合展示选项菜单或表单内容
 * 覆盖状态栏和导航栏，自适应高度，最高不超过480.dp
 *
 * @param visible 控制对话框的显示/隐藏状态
 * @param onDismiss 对话框关闭回调函数，点击外部遮罩或按返回键时触发
 * @param maxHeight 弹窗最大高度，默认480.dp
 * @param minHeight 弹窗最小高度，默认135.dp
 * @param content 对话框内容区域的可组合函数
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    maxHeight: Dp = 480.dp,
    minHeight: Dp = 135.dp,
    content: @Composable () -> Unit
) {
    // 使用 sheetState 来控制显示状态
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val scope = rememberCoroutineScope()

    // 监听 visible 变化来控制 sheet 的显示和隐藏
    LaunchedEffect(visible) {
        if (visible) {
            sheetState.show()
        } else {
            // 如果 sheet 是可见的，则隐藏它
            if (sheetState.isVisible) {
                sheetState.hide()
                // 等待动画完成后再回调
                delay(50) // 给动画一点时间
                onDismiss()
            }
        }
    }

    // 获取屏幕高度用于计算最大高度
    val screenHeight = with(LocalDensity.current) {
        androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    }

    // 计算可用的最大高度
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val availableMaxHeight = screenHeight - statusBarHeight - navigationBarHeight

    // 实际使用的最大高度
    val actualMaxHeight = minOf(availableMaxHeight, maxHeight)

    // 只有当 sheetState 正在显示或隐藏时才显示 ModalBottomSheet
    if (sheetState.isVisible || visible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    delay(50)
                    onDismiss()
                }
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.Transparent,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            dragHandle = null,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight, max = actualMaxHeight)
        ) {
            // 内容区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.color_E3EBF5),
                                Color.White
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(
                        top = 8.dp,
                        bottom = navigationBarHeight + 16.dp
                    )
            ) {
                content()
            }
        }
    }
}
