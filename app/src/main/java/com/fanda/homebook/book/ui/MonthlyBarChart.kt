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
import com.fanda.homebook.book.viewmodel.MonthTransactionData
import com.fanda.homebook.tools.LogUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.DecimalFormat


/**
 * 通用函数：配置并更新 BarChart 的数据与样式
 */
private fun configureBarChart(chart: BarChart, barData: List<MonthTransactionData>, visibleCount: Int, onBarClick: ((MonthTransactionData) -> Unit)) {
    val formatter = object : ValueFormatter() {
        private val format = DecimalFormat("#,###")
        override fun getFormattedValue(value: Float): String {
            return "¥${format.format(value)}"
        }

        override fun getBarLabel(barEntry: BarEntry?): String {
            return "¥${format.format(barEntry?.y ?: 0f)}"
        }
    }

    val entries = barData.mapIndexed { index, item ->
        BarEntry(index.toFloat(), item.totalAmount.toFloat())
    }

    val dataSet = BarDataSet(entries, "").apply {
        colors = barData.map { it.color.toArgb() }
        setDrawValues(true)
        valueTextSize = 12f
        valueTextColor = Color.Black.toArgb()
        valueFormatter = formatter
    }

    chart.data = BarData(dataSet).apply {
        barWidth = 0.7f
        setValueTextSize(12f)
        setValueFormatter(formatter)
        setValueTextColor(Color.Black.toArgb())
    }

    // X 轴配置
    with(chart.xAxis) {
        position = XAxis.XAxisPosition.BOTTOM
        setDrawGridLines(false)
        setDrawAxisLine(true)
        axisLineColor = Color.Black.toArgb()
        axisLineWidth = 1f
        textSize = 14f
        textColor = Color.Black.toArgb()
        granularity = 1f
        labelCount = visibleCount
        setCenterAxisLabels(false)
        axisMinimum = -0.5f
        axisMaximum = barData.size.toFloat() - 0.5f

        valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val index = value.toInt()
                return if (index in barData.indices) barData[index].monthName else ""
            }
        }
    }

    // Y 轴配置
    with(chart.axisLeft) {
        axisMinimum = 0f
        isEnabled = false
    }
    chart.axisRight.isEnabled = false

    // 图表基础设置
    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.setDrawGridBackground(false)
    chart.setDrawBorders(false)

    // 交互设置
    chart.apply {
        setTouchEnabled(true)
//        setPinchZoom(true)
//        setScaleEnabled(true)
        setDragEnabled(true)
        isDoubleTapToZoomEnabled = false
        isAutoScaleMinMaxEnabled = false
        setExtraOffsets(20f, 10f, 20f, 20f)
        setFitBars(true)
        setDrawValueAboveBar(true)
    }

    // 可见范围 & 初始位置
    chart.setVisibleXRangeMaximum(visibleCount.toFloat())
    chart.setVisibleXRangeMinimum(visibleCount.toFloat())
    chart.moveViewToX(-0.5f)

    // 动画 & 刷新
    chart.animateY(800)
    chart.notifyDataSetChanged()
    chart.invalidate()

    // 设置点击监听器
    chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            e?.let { entry ->
                val index = entry.x.toInt()
                if (index in barData.indices) {
                    val selectedData = barData[index]
                    LogUtils.d("柱状图点击：${selectedData}")
                    onBarClick(selectedData)
                }
            }
        }

        override fun onNothingSelected() {
            // 点击空白区域时清除选中状态
            chart.highlightValues(null)
        }
    })

    // 延迟确保布局完成后再定位（尤其首次加载）
    chart.postDelayed({
        chart.setVisibleXRangeMaximum(visibleCount.toFloat())
        chart.moveViewToX(-0.5f)
        chart.invalidate()
    }, 100)
}

/**
 * 可横向滚动的柱状图 + 指示器（实际指示器未实现，但预留扩展）
 *
 * @param barData 柱状图数据，按月顺序排列
 * @param modifier Compose 修饰符
 * @param visibleCount 同时可见的柱子数量（默认 6 个）
 */
@Composable fun ScrollableBarChartWithIndicator(
    barData: List<MonthTransactionData>, modifier: Modifier = Modifier, visibleCount: Int = 6, onBarClick: ((MonthTransactionData) -> Unit)
) {
    // 缓存上一次的数据，用于 diff 判断是否需要更新
    var lastBarData by remember { mutableStateOf<List<MonthTransactionData>?>(null) }
    var lastVisibleCount by remember { mutableStateOf<Int?>(null) }

    AndroidView(
        factory = { context ->
        BarChart(context).apply {
            configureBarChart(this, barData, visibleCount, onBarClick)
            // 初始化后记录状态
            lastBarData = barData.toList()
            lastVisibleCount = visibleCount
        }
    }, update = { chart ->
        // 🔍 仅当数据或可见数量真正变化时才更新图表
        val shouldUpdate = lastBarData != barData || lastVisibleCount != visibleCount

        if (shouldUpdate) {
            LogUtils.d("月对比柱状图数据变更，执行刷新！")
            configureBarChart(chart, barData, visibleCount, onBarClick)

            // 更新缓存（创建副本防止外部修改干扰下次比较）
            lastBarData = barData.toList()
            lastVisibleCount = visibleCount
        } else {
            LogUtils.d("月对比柱状图数据未变，跳过刷新")
        }
    }, modifier = modifier
    )
}