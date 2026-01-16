import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.fanda.homebook.tools.LogUtils
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.math.abs


@SuppressLint("UnrememberedMutableState") @Composable fun DonutChartMPWithLabels(
    data: List<Pair<String, Float>>, modifier: Modifier = Modifier
) {
    // **关键修复1：使用稳定的key，避免不必要的重组**
    val stableData = remember(data) { data }

    // **关键修复2：计算total时避免浮点数精度问题**
    val total = remember(stableData) {
        stableData.sumOf { it.second.toDouble() }
    }

    // **关键修复3：使用rememberUpdatedState来保持对最新数据的引用**
    val currentData by rememberUpdatedState(stableData)

    AndroidView(
        factory = { context ->
        PieChart(context).apply {
            // 配置基础设置
            holeRadius = 50f
            transparentCircleRadius = 55f
            description.isEnabled = false
            legend.isEnabled = false
            setUsePercentValues(false)
            setTouchEnabled(false)
            isHighlightPerTapEnabled = false
            isRotationEnabled = false
            setDrawMarkers(false)

            // 更新图表数据
            updateChartData(currentData, total)
        }
    }, update = { chart ->
        // **关键修复4：只在数据真正变化时才刷新**
        val chartData = chart.data
        if (chartData != null && !isSameData(chartData, currentData, total)) {
            chart.updateChartData(currentData, total)
        }
    }, modifier = modifier
    )
}


// **扩展函数：更新图表数据**
private fun PieChart.updateChartData(data: List<Pair<String, Float>>, total: Double) {
    LogUtils.d("触发圆环图的刷新了！")
    // 创建条目时使用百分比值，确保加起来是100
    val entries = data.map { (label, value) ->
        val percentage = (value / total * 100).toFloat()
        PieEntry(percentage, label)
    }

    val dataSet = PieDataSet(entries, "").apply {
        colors = ColorTemplate.MATERIAL_COLORS.toList()
        sliceSpace = 2f
        valueTextSize = 11f
        valueTextColor = Color.Black.toArgb()

        setDrawEntryLabels(false)
        setEntryLabelColor(Color.Transparent.toArgb())
        setEntryLabelTextSize(0f)

        setDrawValues(true)
        yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        valueLinePart1OffsetPercentage = 80f
        valueLinePart1Length = 0.4f
        valueLinePart2Length = 0.3f
        valueLineColor = Color.Black.toArgb()
        valueLineWidth = 1f
        isValueLineVariableLength = true
    }

    // 格式化器
    dataSet.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = entries.indexOfFirst { it.value == value }
            val label = if (index >= 0 && index < data.size) data[index].first else ""
            return "$label\n${"%.1f".format(value)}%"
        }

        override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
            val label = pieEntry?.label ?: ""
            return "$label\n${"%.1f".format(value)}%"
        }
    }

    this.data = PieData(dataSet).apply {
        setValueTextSize(11f)
        setValueTextColor(Color.Black.toArgb())
        setValueFormatter(dataSet.valueFormatter)
    }

    notifyDataSetChanged()
    invalidate()
    animateY(800)
}

// **辅助函数：检查数据是否相同**
private fun isSameData(
    chartData: PieData, newData: List<Pair<String, Float>>, newTotal: Double
): Boolean {
    val dataSet = chartData.dataSet
    if (dataSet.entryCount != newData.size) return false

    // 检查每个条目
    for (i in 0 until dataSet.entryCount) {
        val entry = dataSet.getEntryForIndex(i) as PieEntry
        val (label, value) = newData[i]

        val newPercentage = (value / newTotal * 100).toFloat()

        // 检查标签
        if (entry.label != label) return false

        // 检查百分比值（允许微小误差）
        if (abs(entry.value - newPercentage) > 0.01f) return false
    }

    return true
}