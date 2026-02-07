package com.fanda.homebook.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * HSV颜色选择器组件
 *
 * 基于HSV（色相、饱和度、明度）颜色模型的交互式颜色选择器，
 * 包含一个S-V平面（饱和度-明度）和一个Hue条（色相）
 *
 * @param modifier 修饰符，用于自定义布局
 * @param onColorSelected 颜色选择回调函数，当用户选择颜色时触发
 * @param initialColor 初始颜色，默认为红色
 * @param selectorSize 选择器指示器的尺寸，默认为15.dp
 */
@Composable fun HSVColorPicker(
    modifier: Modifier = Modifier, onColorSelected: (Color) -> Unit, initialColor: Color = Color.Red, selectorSize: Dp = 15.dp
) {
    // 将初始颜色转换为HSV数组
    val hsv = FloatArray(3)
    LogUtils.d("initialColor: ${initialColor.toArgb()}")
    android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)

    // HSV状态管理
    // 使用takeIf处理0值，提供默认值（S=0.5, V=0.5）
    var hue by remember { mutableFloatStateOf(hsv[0]) }                  // 色相（0-360度）
    var saturation by remember { mutableFloatStateOf(hsv[1].takeIf { it > 0f } ?: 0.5f) }  // 饱和度（0-1）
    var value by remember { mutableFloatStateOf(hsv[2].takeIf { it > 0f } ?: 0.5f) }       // 明度（0-1）

    // 用户交互标志：用于区分是用户交互还是外部初始值变化
    var isUserInteraction by remember { mutableStateOf(false) }

    // 存储上一次的初始颜色，用于比较
    var previousInitialColor by remember { mutableStateOf(initialColor) }

    /**
     * 监听initialColor变化：仅当不是用户交互且颜色真正变化时更新内部状态
     *
     * 这样可以避免用户正在选择颜色时，外部传入的颜色变化干扰用户操作
     */
    LaunchedEffect(initialColor) {
        if (initialColor != previousInitialColor && !isUserInteraction) {
            LogUtils.d("外部 initialColor 改变，更新内部状态")
            val newHsv = FloatArray(3)
            android.graphics.Color.colorToHSV(initialColor.toArgb(), newHsv)

            // 更新HSV状态
            hue = newHsv[0]
            saturation = newHsv[1].takeIf { it > 0f } ?: 0.5f
            value = newHsv[2].takeIf { it > 0f } ?: 0.5f

            previousInitialColor = initialColor
        }
    }

    // 计算当前颜色（基于当前的HSV值）
    val currentColor = remember(hue, saturation, value) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }

    /**
     * 监听当前颜色变化：仅当是用户交互时才触发回调
     *
     * 避免外部初始值变化时也触发onColorSelected回调
     */
    LaunchedEffect(currentColor) {
        if (isUserInteraction) {
            LogUtils.d("用户交互，触发回调: $currentColor")
            onColorSelected(currentColor)
        }
    }

    // 主布局：水平排列S-V平面和Hue条
    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)
        ) {
            // S-V平面（饱和度-明度）
            Box(
                modifier = Modifier
                    .size(290.dp) // 固定尺寸
                    .border(1.dp, Color.Gray) // 灰色边框
            ) {
                SVPlane(
                    hue = hue, saturation = saturation, value = value, onSaturationValueChange = { s, v ->
                        isUserInteraction = true  // 标记为用户交互
                        saturation = s.coerceIn(0f, 1f)
                        value = v.coerceIn(0f, 1f)
                    }, selectorSize = selectorSize
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Hue条（色相）
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(290.dp)
                    .border(1.dp, Color.Gray) // 灰色边框
            ) {
                HueBar(
                    hue = hue, onHueChange = { newHue ->
                        isUserInteraction = true  // 标记为用户交互
                        hue = newHue.coerceIn(0f, 360f)
                    })
            }
        }
    }
}

/**
 * 饱和度-明度（S-V）平面组件
 *
 * 显示当前色相下的所有饱和度和明度组合
 *
 * @param hue 当前色相值（0-360度）
 * @param saturation 当前饱和度（0-1）
 * @param value 当前明度（0-1）
 * @param onSaturationValueChange 饱和度/明度变化回调
 * @param selectorSize 选择器尺寸
 */
@Composable private fun SVPlane(
    hue: Float, saturation: Float, value: Float, onSaturationValueChange: (saturation: Float, value: Float) -> Unit, selectorSize: Dp
) {
    var isDragging by remember { mutableStateOf(false) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            // 点击手势
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    updateSVFromPosition(offset, size, onSaturationValueChange)
                })
        }
            // 拖拽手势
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = { offset ->
                isDragging = true
                updateSVFromPosition(offset, size, onSaturationValueChange)
            }, onDrag = { change, _ ->
                updateSVFromPosition(change.position, size, onSaturationValueChange)
            }, onDragEnd = {
                isDragging = false
            })
        }) {
        // 绘制S-V平面

        // 1. 首先绘制水平渐变：从左（白色）到右（当前色相下的纯色）
        val pureColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White, pureColor), startX = 0f, endX = size.width
            )
        )

        // 2. 叠加垂直渐变：从上（透明）到下（黑色），使用Multiply混合模式
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to Color.Transparent, 1.0f to Color.Black
            ), blendMode = androidx.compose.ui.graphics.BlendMode.Multiply
        )

        // 计算选择器位置（基于当前的饱和度和明度）
        val pointX = saturation * size.width
        val pointY = (1f - value) * size.height  // 注意：坐标系中Y轴向下，而明度向上增加

        // 绘制选择器（多层圆形，提升视觉效果）
        val outerRadius = selectorSize.toPx() / 2
        val middleRadius = selectorSize.toPx() * 0.35f
        val innerRadius = selectorSize.toPx() * 0.2f

        // 外圈1：白色粗边框（6px宽）
        drawCircle(
            color = Color.White, radius = outerRadius, center = Offset(pointX, pointY), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
        )

        // 外圈2：黑色细边框（2px宽），向内偏移1.5px
        drawCircle(
            color = Color.Black, radius = outerRadius - 1.5f, center = Offset(pointX, pointY), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )

        // 中圈：当前颜色圆环
        val currentColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
        drawCircle(
            color = currentColor, radius = middleRadius, center = Offset(pointX, pointY), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )

        // 内圈：当前颜色填充圆
        drawCircle(
            color = currentColor, radius = innerRadius, center = Offset(pointX, pointY)
        )
    }
}

/**
 * 根据触摸位置更新饱和度和明度
 *
 * @param position 触摸位置
 * @param size 画布尺寸
 * @param onSaturationValueChange 回调函数
 */
private fun updateSVFromPosition(
    position: Offset, size: IntSize, onSaturationValueChange: (Float, Float) -> Unit
) {
    if (size.width <= 0 || size.height <= 0) return

    // 限制触摸位置在画布范围内
    val clampedX = position.x.coerceIn(0f, size.width.toFloat())
    val clampedY = position.y.coerceIn(0f, size.height.toFloat())

    // 计算饱和度和明度
    val s = clampedX / size.width  // X轴：饱和度（0-1）
    val v = 1f - (clampedY / size.height)  // Y轴：明度（0-1），Y轴方向与明度相反

    onSaturationValueChange(s, v)
}

/**
 * 色相（Hue）条组件
 *
 * 显示0-360度的所有色相，用户可以垂直滑动选择
 *
 * @param hue 当前色相值（0-360度）
 * @param onHueChange 色相变化回调
 */
@Composable private fun HueBar(
    hue: Float, onHueChange: (hue: Float) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            // 点击手势
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    updateHueFromPosition(offset, size, onHueChange)
                })
        }
            // 拖拽手势
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = { offset ->
                updateHueFromPosition(offset, size, onHueChange)
            }, onDrag = { change, _ ->
                updateHueFromPosition(change.position, size, onHueChange)
            })
        }) {
        // 绘制色相渐变条
        val hueColors = listOf(
            Color.Red,      // 0°
            Color.Yellow,   // 60°
            Color.Green,    // 120°
            Color.Cyan,     // 180°
            Color.Blue,     // 240°
            Color.Magenta,  // 300°
            Color.Red       // 360°（回到红色）
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = hueColors, startY = 0f, endY = size.height
            )
        )

        // 绘制当前色相指示器
        val indicatorY = (hue / 360f) * size.height
        drawHueIndicator(indicatorY, size)
    }
}

/**
 * 根据触摸位置更新色相值
 *
 * @param position 触摸位置
 * @param size 画布尺寸
 * @param onHueChange 回调函数
 */
private fun updateHueFromPosition(
    position: Offset, size: IntSize, onHueChange: (Float) -> Unit
) {
    if (size.height <= 0) return

    val clampedY = position.y.coerceIn(0f, size.height.toFloat())
    val hue = (clampedY / size.height) * 360f

    onHueChange(hue)
}

/**
 * 绘制色相条指示器
 *
 * @param y 指示器垂直位置
 * @param size 画布尺寸
 */
private fun DrawScope.drawHueIndicator(y: Float, size: Size) {
    // 1. 绘制指示器外框（半透明白色背景）
    drawRect(
        color = Color.White.copy(alpha = 0.8f), topLeft = Offset(0f, y - 3f), size = Size(size.width, 6f)
    )

    // 2. 绘制指示器内框（半透明黑色边框）
    drawRect(
        color = Color.Black.copy(alpha = 0.6f), topLeft = Offset(0f, y - 2.5f), size = Size(size.width, 5f)
    )

    // 3. 绘制中心线（白色粗线）
    drawLine(
        color = Color.White, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 3f
    )

    // 4. 绘制中心线（黑色细线）
    drawLine(
        color = Color.Black, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.5f
    )

    // 5. 在两侧绘制三角形指示箭头
    val triangleSize = 6f

    // 左侧三角形
    drawPath(
        path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, y - triangleSize)
            lineTo(triangleSize, y)
            lineTo(0f, y + triangleSize)
            close()
        }, color = Color.White
    )

    // 右侧三角形
    drawPath(
        path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width, y - triangleSize)
            lineTo(size.width - triangleSize, y)
            lineTo(size.width, y + triangleSize)
            close()
        }, color = Color.White
    )
}