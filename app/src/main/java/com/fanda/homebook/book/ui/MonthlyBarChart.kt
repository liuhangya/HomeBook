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
import com.dylanc.longan.getCompatColor
import com.dylanc.longan.topActivity
import com.fanda.homebook.R
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
 * 配置并更新 BarChart 的数据与样式
 *
 * @param chart BarChart 实例
 * @param barData 柱状图数据列表，按月份顺序排列
 * @param visibleCount 同时可见的柱子数量
 * @param onBarClick 柱子点击回调函数
 */
private fun configureBarChart(
    chart: BarChart, barData: List<MonthTransactionData>, visibleCount: Int, onBarClick: ((MonthTransactionData) -> Unit)
) {
    // 自定义数值格式化器，用于显示金额（带¥符号和千位分隔符）
    val formatter = object : ValueFormatter() {
        private val format = DecimalFormat("#,###")

        override fun getFormattedValue(value: Float): String {
            return "¥${format.format(value)}"
        }

        override fun getBarLabel(barEntry: BarEntry?): String {
            return "¥${format.format(barEntry?.y ?: 0f)}"
        }
    }

    // 将数据转换为BarEntry列表，索引作为X轴位置，总金额作为Y轴值
    val entries = barData.mapIndexed { index, item ->
        BarEntry(index.toFloat(), item.totalAmount.toFloat())
    }

    // 创建数据集并配置样式
    val dataSet = BarDataSet(entries, "").apply {
        colors = barData.map { it.color.toArgb() }  // 设置每个柱子的颜色
        setDrawValues(true)                         // 显示柱子上的数值
        valueTextSize = 12f                         // 数值文字大小
        valueTextColor = Color.Black.toArgb()       // 数值文字颜色
        valueFormatter = formatter                  // 数值格式化器
    }

    // 配置图表数据
    chart.data = BarData(dataSet).apply {
        barWidth = 0.7f                             // 柱子宽度
        setValueTextSize(12f)                       // 数值文字大小
        setValueFormatter(formatter)                // 数值格式化器
        setValueTextColor(Color.Black.toArgb())     // 数值文字颜色
    }

    // X 轴配置
    with(chart.xAxis) {
        position = XAxis.XAxisPosition.BOTTOM       // X轴在底部显示
        setDrawGridLines(false)                     // 不绘制网格线
        setDrawAxisLine(true)                       // 绘制轴线
        axisLineColor = topActivity.getCompatColor(R.color.color_84878C)           // 轴线颜色
        axisLineWidth = 1f                          // 轴线宽度
        textSize = 14f                              // 轴标签文字大小
        textColor = topActivity.getCompatColor(R.color.color_84878C)              // 轴标签文字颜色
        granularity = 1f                            // 最小刻度间隔
        labelCount = visibleCount                   // 显示的标签数量
        setCenterAxisLabels(false)                  // 不居中显示轴标签
        axisMinimum = -0.5f                         // X轴最小值
        axisMaximum = barData.size.toFloat() - 0.5f // X轴最大值

        // X轴标签格式化器，显示月份名称
        valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val index = value.toInt()
                return if (index in barData.indices) barData[index].monthName else ""
            }
        }
    }

    // 左Y轴配置
    with(chart.axisLeft) {
        axisMinimum = 0f                            // Y轴最小值从0开始
        isEnabled = false                           // 禁用左Y轴显示
    }
    chart.axisRight.isEnabled = false               // 禁用右Y轴显示

    // 图表基础设置
    chart.description.isEnabled = false             // 禁用描述文本
    chart.legend.isEnabled = false                  // 禁用图例
    chart.setDrawGridBackground(false)              // 不绘制网格背景
    chart.setDrawBorders(false)                     // 不绘制边框

    // 交互设置
    chart.apply {
        setTouchEnabled(true)                       // 启用触摸交互
        setDragEnabled(true)                        // 启用拖拽
        isDoubleTapToZoomEnabled = false            // 禁用双击缩放
        isAutoScaleMinMaxEnabled = false            // 禁用自动缩放
        setExtraOffsets(20f, 10f, 20f, 20f)         // 设置额外边距（左，上，右，下）
        setFitBars(true)                            // 自动调整柱子宽度适应屏幕
        setDrawValueAboveBar(true)                  // 在柱子上方显示数值
    }

    // 设置可见范围及初始位置
    chart.setVisibleXRangeMaximum(visibleCount.toFloat())  // 最大可见范围
    chart.setVisibleXRangeMinimum(visibleCount.toFloat())  // 最小可见范围
    chart.moveViewToX(-0.5f)                               // 初始显示位置

    // 动画效果及刷新
    chart.animateY(800)                            // Y轴方向动画，持续时间800ms
    chart.notifyDataSetChanged()                   // 通知数据已变更
    chart.invalidate()                             // 强制重绘

    // 设置点击监听器
    chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            e?.let { entry ->
                val index = entry.x.toInt()
                if (index in barData.indices) {
                    val selectedData = barData[index]
                    LogUtils.d("柱状图点击：${selectedData}")  // 日志记录点击事件
                    onBarClick(selectedData)                  // 执行点击回调
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
 * 可横向滚动的柱状图组件
 *
 * @param barData 柱状图数据，按月顺序排列
 * @param modifier Compose修饰符
 * @param visibleCount 同时可见的柱子数量，默认6个
 * @param onBarClick 柱子点击回调函数
 */
@Composable fun ScrollableBarChartWithIndicator(
    barData: List<MonthTransactionData>, modifier: Modifier = Modifier, visibleCount: Int = 6, onBarClick: ((MonthTransactionData) -> Unit)
) {
    // 缓存上一次的数据和可见数量，用于比较是否需要更新
    var lastBarData by remember { mutableStateOf<List<MonthTransactionData>?>(null) }
    var lastVisibleCount by remember { mutableStateOf<Int?>(null) }

    AndroidView(
        factory = { context ->
        BarChart(context).apply {
            // 初始化配置图表
            configureBarChart(this, barData, visibleCount, onBarClick)
            // 初始化后记录状态
            lastBarData = barData.toList()
            lastVisibleCount = visibleCount
        }
    }, update = { chart ->
        // 仅当数据或可见数量真正变化时才更新图表，避免不必要的重绘
        val shouldUpdate = lastBarData != barData || lastVisibleCount != visibleCount

        if (shouldUpdate) {
            LogUtils.d("月对比柱状图数据变更，执行刷新！")  // 日志记录更新事件
            configureBarChart(chart, barData, visibleCount, onBarClick)

            // 更新缓存（创建副本防止外部修改干扰下次比较）
            lastBarData = barData.toList()
            lastVisibleCount = visibleCount
        } else {
            LogUtils.d("月对比柱状图数据未变，跳过刷新")  // 日志记录跳过事件
        }
    }, modifier = modifier
    )
}