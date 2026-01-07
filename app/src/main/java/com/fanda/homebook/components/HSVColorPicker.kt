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

@Composable
fun HSVColorPicker(
    modifier: Modifier = Modifier,
    onColorSelected: (Color) -> Unit,
    initialColor: Color = Color.Red,
    selectorSize: Dp = 15.dp  // 稍微增大选择器大小
) {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)

    // 默认居中：S=0.5, V=0.5
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1].takeIf { it > 0f } ?: 0.5f) }
    var value by remember { mutableFloatStateOf(hsv[2].takeIf { it > 0f } ?: 0.5f) }

    val currentColor = remember(hue, saturation, value) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }

    LaunchedEffect(currentColor) {
        onColorSelected(currentColor)
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
                        saturation = s.coerceIn(0f, 1f)
                        value = v.coerceIn(0f, 1f)
                    },
                    selectorSize = selectorSize
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Hue 条 - 增加宽度
            Box(
                modifier = Modifier
                    .width(80.dp)  // 从30dp增加到40dp，更容易点击
                    .height(290.dp)
                    .border(1.dp, Color.Gray)
            ) {
                HueBar(
                    hue = hue,
                    onHueChange = { newHue ->
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

        // 方法1：使用多个矩形叠加实现 S-V 平面
        // 先绘制从左到右的渐变：白色 -> 纯色
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White, pureColor),
                startX = 0f,
                endX = size.width
            )
        )

        // 再从上到下叠加黑色渐变（使用 alpha 通道模拟混合）
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to Color.Transparent,
                1.0f to Color.Black
            ),
            // 使用 Compose 的方式叠加颜色
            blendMode = androidx.compose.ui.graphics.BlendMode.Multiply
        )

        // 计算选择器位置
        val pointX = saturation * size.width
        val pointY = (1f - value) * size.height  // 注意：Y轴方向，顶部V=1，底部V=0

        // 绘制选择器
        val outerRadius = selectorSize.toPx() / 2
        val middleRadius = selectorSize.toPx() * 0.35f  // 中间圈半径
        val innerRadius = selectorSize.toPx() * 0.2f    // 内圈半径

        // 外圈1（白色粗边框）
        drawCircle(
            color = Color.White,
            radius = outerRadius,
            center = Offset(pointX, pointY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
        )

        // 外圈2（黑色细边框，增加层次感）
        drawCircle(
            color = Color.Black,
            radius = outerRadius - 1.5f,
            center = Offset(pointX, pointY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )

        // 中圈（当前颜色，增加边框效果）
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

        // 中心点（对比色）
//        val centerColor = if (value > 0.5f) Color.Black else Color.White
//        drawCircle(
//            color = centerColor,
//            radius = innerRadius,
//            center = Offset(pointX, pointY)
//        )
    }
}

private fun updateSVFromPosition(position: Offset, size: IntSize, onSaturationValueChange: (Float, Float) -> Unit) {
    if (size.width <= 0 || size.height <= 0) return

    // 确保坐标在画布范围内
    val clampedX = position.x.coerceIn(0f, size.width.toFloat())
    val clampedY = position.y.coerceIn(0f, size.height.toFloat())

    val s = clampedX / size.width
    val v = 1f - (clampedY / size.height)  // 翻转Y轴

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
    // 绘制指示器外框（全宽度）
    drawRect(
        color = Color.White.copy(alpha = 0.8f),
        topLeft = Offset(0f, y - 3f),  // 增加高度
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

    // 绘制中心线2（黑色，更明显）
    drawLine(
        color = Color.Black,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 1.5f
    )

    // 在两侧绘制小三角形增强可见性
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