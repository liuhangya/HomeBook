package com.fanda.homebook.book.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.fanda.homebook.book.viewmodel.DailyTransactionData
import com.fanda.homebook.tools.LogUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

data class DailyExpense(
    val date: String,  // 格式如 "9.10"
    val amount: Float, // 金额
)

/**
 * 配置每日支出柱状图的通用逻辑
 */
private fun configureDailyExpenseChart(
    chart: BarChart,
    expenses: List<DailyTransactionData>,
    visibleDays: Int
) {
    val barColor = Color(0xFF2196F3).toArgb()

    // 数值格式化器（用于柱子顶部和Y轴）
    val formatter = object : ValueFormatter() {
        private val format = DecimalFormat("#,###")
        override fun getFormattedValue(value: Float): String {
            return format.format(value)
        }

        override fun getBarLabel(barEntry: BarEntry?): String {
            return format.format(barEntry?.y ?: 0f)
        }
    }

    val yValueFormatter = object : ValueFormatter() {
        private val format = DecimalFormat("#,###")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return "¥${format.format(value)}"
        }
    }

    // 构建数据
    val entries = expenses.mapIndexed { index, expense ->
        BarEntry(index.toFloat(), expense.totalAmount.toFloat())
    }

    val dataSet = BarDataSet(entries, "").apply {
        color = barColor
        setDrawValues(true)
        isHighlightEnabled = true
        valueTextColor = Color.Black.toArgb()
        valueTextSize = 10f
        valueFormatter = formatter
    }

    chart.data = BarData(dataSet).apply {
        barWidth = 0.5f
        setValueTextSize(10f)
        setValueFormatter(formatter)
        setValueTextColor(Color.Black.toArgb())
    }

    // === X 轴配置 ===
    with(chart.xAxis) {
        position = XAxis.XAxisPosition.BOTTOM
        setDrawGridLines(false)
        setDrawAxisLine(true)
        axisLineColor = Color.Black.toArgb()
        axisLineWidth = 1f
        textSize = 11f
        textColor = Color.DarkGray.toArgb()
        granularity = 1f
        labelCount = visibleDays
        setCenterAxisLabels(false)
        axisMinimum = -0.5f
        axisMaximum = expenses.size.toFloat() - 0.5f

        valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val index = value.toInt()
                return if (index in expenses.indices) expenses[index].date else ""
            }
        }
    }

    // === Y 轴（左侧）配置 ===
    with(chart.axisLeft) {
        setDrawGridLines(true)
        gridColor = Color.LightGray.copy(alpha = 0.3f).toArgb()
        gridLineWidth = 0.5f
        setDrawAxisLine(true)
        axisLineColor = Color.Black.toArgb()
        axisLineWidth = 1f
        textSize = 11f
        textColor = Color.DarkGray.toArgb()
        axisMinimum = 0f

        this.valueFormatter = yValueFormatter
    }

    // === Y 轴（右侧）禁用 ===
    chart.axisRight.apply {
        setDrawGridLines(false)
        setDrawAxisLine(false)
        setDrawLabels(false)
    }

    // === 图表基础设置 ===
    chart.apply {
        description.isEnabled = false
        legend.isEnabled = false
        setDrawGridBackground(false)
        setDrawBorders(false)

        // 交互
        setTouchEnabled(true)
//        setPinchZoom(true)
//        setScaleEnabled(true)
        setDragEnabled(true)
        isDoubleTapToZoomEnabled = false
        isAutoScaleMinMaxEnabled = false

        // 布局
        setExtraOffsets(6f, 20f, 15f, 20f) // top=20 给数值留空间
        setFitBars(true)
        setDrawValueAboveBar(true)
        setDrawBarShadow(false)

        // 可见范围
        setVisibleXRangeMaximum(visibleDays.toFloat())
        setVisibleXRangeMinimum(visibleDays.toFloat())

        // 初始位置：从最左侧开始
        moveViewToX(-0.5f)

        // 动画
        animateY(800)

        notifyDataSetChanged()
        invalidate()

        // 延迟确保布局正确
        postDelayed({
            setVisibleXRangeMaximum(visibleDays.toFloat())
            moveViewToX(-0.5f)
            invalidate()
        }, 100)
    }
}

@Composable
fun DailyExpenseBarChart(
    data: List<DailyTransactionData>,
    modifier: Modifier = Modifier,
    visibleDays: Int = 5
) {
    // 缓存上一次的输入，用于 diff
    var lastExpenses by remember { mutableStateOf<List<DailyTransactionData>>(emptyList()) }
    var lastVisibleDays by remember { mutableStateOf<Int?>(null) }

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                configureDailyExpenseChart(this, data, visibleDays)
                lastExpenses = data.toList()
                lastVisibleDays = visibleDays
            }
        },
        update = { chart ->
            val shouldUpdate =
                lastExpenses != data ||
                        lastVisibleDays != visibleDays

            if (shouldUpdate) {
                LogUtils.d("每日支出柱状图数据变更，执行刷新！")
                configureDailyExpenseChart(chart, data, visibleDays)
                lastExpenses = data.toList()
                lastVisibleDays = visibleDays
            } else {
                LogUtils.d("每日支出柱状图数据未变，跳过刷新")
            }
        },
        modifier = modifier
    )
}