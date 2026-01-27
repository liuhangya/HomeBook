package com.fanda.homebook.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.fanda.homebook.tools.LogUtils

@Composable
fun HSVColorPicker(
    modifier: Modifier = Modifier,
    onColorSelected: (Color) -> Unit,
    initialColor: Color = Color.Red,
    selectorSize: Dp = 15.dp
) {
    val hsv = FloatArray(3)
    LogUtils.d("initialColor: ${initialColor.toArgb()}")
    android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)

    // 默认居中：S=0.5, V=0.5
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1].takeIf { it > 0f } ?: 0.5f) }
    var value by remember { mutableFloatStateOf(hsv[2].takeIf { it > 0f } ?: 0.5f) }

    // 添加用户交互标志
    var isUserInteraction by remember { mutableStateOf(false) }

    // 存储上一次的 initialColor，用于比较
    var previousInitialColor by remember { mutableStateOf(initialColor) }

    // 当 initialColor 改变且不是用户交互时，更新内部状态
    LaunchedEffect(initialColor) {
        if (initialColor != previousInitialColor && !isUserInteraction) {
            LogUtils.d("外部 initialColor 改变，更新内部状态")
            val newHsv = FloatArray(3)
            android.graphics.Color.colorToHSV(initialColor.toArgb(), newHsv)

            hue = newHsv[0]
            saturation = newHsv[1].takeIf { it > 0f } ?: 0.5f
            value = newHsv[2].takeIf { it > 0f } ?: 0.5f

            previousInitialColor = initialColor
        }
    }

    val currentColor = remember(hue, saturation, value) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }

    // 只在用户交互时触发回调
    LaunchedEffect(currentColor) {
        if (isUserInteraction) {
            LogUtils.d("用户交互，触发回调: $currentColor")
            onColorSelected(currentColor)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            // SV 平面
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .border(1.dp, Color.Gray)
            ) {
                SVPlane(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onSaturationValueChange = { s, v ->
                        isUserInteraction = true  // 标记为用户交互
                        saturation = s.coerceIn(0f, 1f)
                        value = v.coerceIn(0f, 1f)
                    },
                    selectorSize = selectorSize
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Hue 条
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(290.dp)
                    .border(1.dp, Color.Gray)
            ) {
                HueBar(
                    hue = hue,
                    onHueChange = { newHue ->
                        isUserInteraction = true  // 标记为用户交互
                        hue = newHue.coerceIn(0f, 360f)
                    }
                )
            }
        }
    }
}

@Composable
private fun SVPlane(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChange: (saturation: Float, value: Float) -> Unit,
    selectorSize: Dp
) {
    var isDragging by remember { mutableStateOf(false) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        updateSVFromPosition(offset, size, onSaturationValueChange)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        updateSVFromPosition(offset, size, onSaturationValueChange)
                    },
                    onDrag = { change, _ ->
                        updateSVFromPosition(change.position, size, onSaturationValueChange)
                    },
                    onDragEnd = {
                        isDragging = false
                    }
                )
            }
    ) {
        // 绘制 S-V 平面
        val pureColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))

        // 先绘制从左到右的渐变：白色 -> 纯色
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White, pureColor),
                startX = 0f,
                endX = size.width
            )
        )

        // 再从上到下叠加黑色渐变
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to Color.Transparent,
                1.0f to Color.Black
            ),
            blendMode = androidx.compose.ui.graphics.BlendMode.Multiply
        )

        // 计算选择器位置
        val pointX = saturation * size.width
        val pointY = (1f - value) * size.height

        // 绘制选择器
        val outerRadius = selectorSize.toPx() / 2
        val middleRadius = selectorSize.toPx() * 0.35f
        val innerRadius = selectorSize.toPx() * 0.2f

        // 外圈1（白色粗边框）
        drawCircle(
            color = Color.White,
            radius = outerRadius,
            center = Offset(pointX, pointY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
        )

        // 外圈2（黑色细边框）
        drawCircle(
            color = Color.Black,
            radius = outerRadius - 1.5f,
            center = Offset(pointX, pointY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )

        // 中圈（当前颜色）
        val currentColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
        drawCircle(
            color = currentColor,
            radius = middleRadius,
            center = Offset(pointX, pointY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )

        // 内圈（当前颜色填充）
        drawCircle(
            color = currentColor,
            radius = middleRadius - 2f,
            center = Offset(pointX, pointY)
        )
    }
}

private fun updateSVFromPosition(position: Offset, size: IntSize, onSaturationValueChange: (Float, Float) -> Unit) {
    if (size.width <= 0 || size.height <= 0) return

    val clampedX = position.x.coerceIn(0f, size.width.toFloat())
    val clampedY = position.y.coerceIn(0f, size.height.toFloat())

    val s = clampedX / size.width
    val v = 1f - (clampedY / size.height)

    onSaturationValueChange(s, v)
}

@Composable
private fun HueBar(
    hue: Float,
    onHueChange: (hue: Float) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        updateHueFromPosition(offset, size, onHueChange)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        updateHueFromPosition(offset, size, onHueChange)
                    },
                    onDrag = { change, _ ->
                        updateHueFromPosition(change.position, size, onHueChange)
                    }
                )
            }
    ) {
        // 绘制 Hue 渐变条
        val hueColors = listOf(
            Color.Red,          // 0°
            Color.Yellow,       // 60°
            Color.Green,        // 120°
            Color.Cyan,         // 180°
            Color.Blue,         // 240°
            Color.Magenta,      // 300°
            Color.Red           // 360°
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = hueColors,
                startY = 0f,
                endY = size.height
            )
        )

        // 绘制当前 Hue 指示器
        val indicatorY = (hue / 360f) * size.height

        // 绘制指示器
        drawHueIndicator(indicatorY, size)
    }
}

private fun updateHueFromPosition(position: Offset, size: IntSize, onHueChange: (Float) -> Unit) {
    if (size.height <= 0) return

    val clampedY = position.y.coerceIn(0f, size.height.toFloat())
    val hue = (clampedY / size.height) * 360f

    onHueChange(hue)
}

private fun DrawScope.drawHueIndicator(y: Float, size: Size) {
    // 绘制指示器外框
    drawRect(
        color = Color.White.copy(alpha = 0.8f),
        topLeft = Offset(0f, y - 3f),
        size = Size(size.width, 6f)
    )

    // 绘制指示器内框
    drawRect(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(0f, y - 2.5f),
        size = Size(size.width, 5f)
    )

    // 绘制中心线1（白色）
    drawLine(
        color = Color.White,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 3f
    )

    // 绘制中心线2（黑色）
    drawLine(
        color = Color.Black,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 1.5f
    )

    // 在两侧绘制小三角形
    val triangleSize = 6f
    // 左侧三角形
    drawPath(
        path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, y - triangleSize)
            lineTo(triangleSize, y)
            lineTo(0f, y + triangleSize)
            close()
        },
        color = Color.White
    )

    // 右侧三角形
    drawPath(
        path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width, y - triangleSize)
            lineTo(size.width - triangleSize, y)
            lineTo(size.width, y + triangleSize)
            close()
        },
        color = Color.White
    )
}