import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.viewinterop.AndroidView
import com.dylanc.longan.getCompatColor
import com.dylanc.longan.getString
import com.dylanc.longan.topActivity
import com.fanda.homebook.tools.LogUtils
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.math.abs
import com.fanda.homebook.R
/**
 * 圆环图组件（使用MPAndroidChart库）
 * 显示分类数据的百分比分布，带有外部标签
 *
 * @param data 数据列表，每个元素为Pair<标签, 数值>
 * @param modifier Compose修饰符
 */
@SuppressLint("UnrememberedMutableState") @Composable fun DonutChartMPWithLabels(
    data: List<Pair<String, Float>>, modifier: Modifier = Modifier
) {
    // **关键修复1：使用稳定的key，避免不必要的重组**
    // 通过remember记住数据，避免每次重组都创建新对象
    val stableData = remember(data) { data }

    // **关键修复2：计算total时避免浮点数精度问题**
    // 使用Double类型求和，避免浮点数精度损失
    val total = remember(stableData) {
        stableData.sumOf { it.second.toDouble() }
    }

    // **关键修复3：使用rememberUpdatedState来保持对最新数据的引用**
    // 确保在更新函数中访问的是最新的数据
    val currentData by rememberUpdatedState(stableData)

    AndroidView(
        factory = { context ->
        PieChart(context).apply {
            // 配置基础设置
            holeRadius = 50f                     // 圆环半径（中间空心部分）
            transparentCircleRadius = 55f        // 透明圆半径（圆环边框）
            description.isEnabled = false        // 禁用描述文本
            legend.isEnabled = false             // 禁用图例
            setUsePercentValues(false)           // 不使用百分比值（后面手动计算）
            setTouchEnabled(true)                // 启用触摸交互
            isHighlightPerTapEnabled = false     // 禁用点击高亮
            isRotationEnabled = true             // 启用旋转
            setDrawMarkers(false)                // 禁用标记点

            // 初始化图表数据
            updateChartData(currentData, total)
        }
    }, update = { chart ->
        // **关键修复4：只在数据真正变化时才刷新**
        // 比较当前图表数据和新的数据，避免不必要的刷新
        val chartData = chart.data
        if (chartData != null && !isSameData(chartData, currentData, total)) {
            chart.updateChartData(currentData, total)
        }
    }, modifier = modifier
    )
}

/**
 * 扩展函数：更新PieChart的数据
 *
 * @param data 新的数据列表
 * @param total 数据总和
 */
private fun PieChart.updateChartData(data: List<Pair<String, Float>>, total: Double) {
    LogUtils.d("触发圆环图的刷新了！")

    // 创建PieEntry列表，计算每个数据项的百分比
    val entries = data.map { (label, value) ->
        val percentage = (value / total * 100).toFloat()  // 计算百分比
        PieEntry(percentage, label)                      // 创建饼图条目
    }

    // 创建数据集并配置样式
    val dataSet = PieDataSet(entries, "").apply {
        colors = ColorTemplate.MATERIAL_COLORS.toList()  // 使用Material Design颜色
        sliceSpace = 2f                                  // 切片之间的间距
        valueTextSize = 11f                              // 数值文字大小
        valueTextColor = topActivity.getCompatColor(R.color.color_84878C)            // 数值文字颜色

        setDrawEntryLabels(false)                        // 不绘制条目标签
        setEntryLabelColor(Color.Transparent.toArgb())   // 条目标签颜色透明
        setEntryLabelTextSize(0f)                        // 条目标签文字大小为0

        setDrawValues(true)                              // 绘制数值
        yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE  // Y值位置在切片外部
        xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE  // X值位置在切片外部

        valueLinePart1OffsetPercentage = 80f             // 值线第一部分偏移百分比
        valueLinePart1Length = 0.4f                      // 值线第一部分长度
        valueLinePart2Length = 0.3f                      // 值线第二部分长度
        valueLineColor = topActivity.getCompatColor(R.color.color_84878C)            // 值线颜色
        valueLineWidth = 1f                              // 值线宽度
        isValueLineVariableLength = true                 // 值线可变长度
    }

    // 数值格式化器：显示标签和百分比
    dataSet.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            // 根据值查找对应的标签
            val index = entries.indexOfFirst { it.value == value }
            val label = if (index >= 0 && index < data.size) data[index].first else ""
            return "$label\n${"%.1f".format(value)}%"  // 格式：标签\n百分比%
        }

        override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
            val label = pieEntry?.label ?: ""
            return "$label\n${"%.1f".format(value)}%"  // 格式：标签\n百分比%
        }
    }

    // 设置图表数据
    this.data = PieData(dataSet).apply {
        setValueTextSize(11f)                            // 数值文字大小
        setValueTextColor(topActivity.getCompatColor(R.color.color_84878C))          // 数值文字颜色
        setValueFormatter(dataSet.valueFormatter)        // 数值格式化器
    }

    // 刷新图表
    notifyDataSetChanged()  // 通知数据已变更
    invalidate()            // 强制重绘
    animateY(800)           // Y轴方向动画，持续时间800ms
}

/**
 * 检查图表数据是否与新数据相同
 *
 * @param chartData 当前图表数据
 * @param newData 新的数据列表
 * @param newTotal 新的数据总和
 * @return 如果数据相同返回true，否则返回false
 */
private fun isSameData(
    chartData: PieData, newData: List<Pair<String, Float>>, newTotal: Double
): Boolean {
    val dataSet = chartData.dataSet

    // 检查数据数量是否相同
    if (dataSet.entryCount != newData.size) return false

    // 检查每个条目的标签和百分比值
    for (i in 0 until dataSet.entryCount) {
        val entry = dataSet.getEntryForIndex(i) as PieEntry
        val (label, value) = newData[i]

        // 计算新的百分比值
        val newPercentage = (value / newTotal * 100).toFloat()

        // 检查标签是否相同
        if (entry.label != label) return false

        // 检查百分比值是否相同（允许0.01的微小误差）
        if (abs(entry.value - newPercentage) > 0.01f) return false
    }

    return true
}