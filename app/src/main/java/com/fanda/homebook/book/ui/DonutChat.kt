import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun DonutChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    holeRadiusFraction: Float = 0.6f,
    strokeSize: Dp = 32.dp,
    labelColor: Color = Color.Black,
    labelFontSize: Float = 12f,
    showPercentage: Boolean = true,
    labelDistance: Dp = 48.dp
) {
    val density = LocalDensity.current

    Canvas(modifier = modifier.fillMaxSize()) {
        // === 1. 计算总值 ===
        var total = 0f
        for (slice in slices) total += slice.value
        if (total <= 0f) return@Canvas

        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = (size.minDimension / 2f) - strokeSize.toPx() / 2f
        val strokeWidthPx = strokeSize.toPx()
        val labelDistancePx = labelDistance.toPx()
        val fontSizePx = labelFontSize * density.density // 转为 px

        var startAngle = -90f
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = fontSizePx
            color = labelColor.toArgb()
        }

        // === 2. 绘制扇区 + 标签 ===
        slices.forEach { slice ->
            val sweepAngle = (slice.value / total) * 360f
            if (sweepAngle <= 0f) {
                startAngle += sweepAngle
                return@forEach
            }

            // 绘制圆环扇区
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(centerX - radius, centerY - radius)
            )

            // 计算标签位置
            val midAngleDeg = startAngle + sweepAngle / 2
            val midAngleRad = midAngleDeg.toRadians()
            val labelRadius = radius + labelDistancePx
            val labelX = centerX + labelRadius * cos(midAngleRad)
            val labelY = centerY + labelRadius * sin(midAngleRad)

            // 百分比文本
            val percentage = if (showPercentage) {
                "%.2f%%".format(slice.value / total * 100)
            } else ""
            val text = "${slice.label} $percentage".trim()

            // 判断左右侧
            val isLeft = labelX < centerX
            paint.textAlign = if (isLeft) {
                Paint.Align.RIGHT
            } else {
                Paint.Align.LEFT
            }

            // 绘制连线
            val lineStartRadius = radius + strokeWidthPx / 2
            val lineStartX = centerX + lineStartRadius * cos(midAngleRad).toFloat()
            val lineStartY = centerY + lineStartRadius * sin(midAngleRad).toFloat()

            val cornerOffset = if (isLeft) -10.dp.toPx() else 10.dp.toPx()
            val cornerX = (labelX + cornerOffset).toFloat()
            val cornerY = labelY.toFloat()

            // 连线：扇区 → 拐角 → 标签
            drawContext.canvas.nativeCanvas.apply {
                drawLine(
                    lineStartX,
                    lineStartY,
                    cornerX,
                    cornerY,
                    Paint().apply {
                        color = labelColor.copy(alpha = 0.5f).toArgb()
                        strokeWidth = 1.dp.toPx()
                    }
                )
                drawLine(
                    cornerX,
                    cornerY,
                    labelX.toFloat(),
                    labelY.toFloat(),
                    Paint().apply {
                        color = labelColor.copy(alpha = 0.5f).toArgb()
                        strokeWidth = 1.dp.toPx()
                    }
                )
            }

            // 绘制文本（y 需微调，因为 drawText 基线在底部）
            drawContext.canvas.nativeCanvas.drawText(
                text,
                labelX.toFloat(),
                (labelY - (fontSizePx / 3)).toFloat(), // 微调垂直居中
                paint
            )

            startAngle += sweepAngle
        }
    }
}



// 扩展：Float 度转弧度
private fun Float.toRadians(): Double = Math.toRadians(this.toDouble())