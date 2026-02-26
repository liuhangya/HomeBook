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
import com.fanda.homebook.book.viewmodel.DailyTransactionData
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
 * 配置每日交易数据柱状图
 *
 * @param chart BarChart 实例
 * @param expenses 每日交易数据列表
 * @param visibleDays 同时可见的天数
 * @param onBarClick 柱子点击回调函数（可选）
 */
private fun configureDailyChart(
    chart: BarChart, expenses: List<DailyTransactionData>, visibleDays: Int, onBarClick: ((DailyTransactionData) -> Unit)? = null
) {
    // 柱状图颜色 - 固定为蓝色
    val barColor = Color(0xFF2196F3).toArgb()

    // 数值格式化器（用于柱子顶部显示）
    val formatter = object : ValueFormatter() {
        private val format = DecimalFormat("#,###")
        override fun getFormattedValue(value: Float): String {
            return format.format(value)
        }

        override fun getBarLabel(barEntry: BarEntry?): String {
            return format.format(barEntry?.y ?: 0f)
        }
    }

    // Y轴数值格式化器（带¥符号）
    val yValueFormatter = object : ValueFormatter() {
        private val format = DecimalFormat("#,###")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return "¥${format.format(value)}"
        }
    }

    // 构建数据：将每日交易数据转换为BarEntry列表
    val entries = expenses.mapIndexed { index, expense ->
        BarEntry(index.toFloat(), expense.totalAmount.toFloat())
    }

    // 创建数据集并配置样式
    val dataSet = BarDataSet(entries, "").apply {
        color = barColor                     // 柱子颜色
        setDrawValues(true)                  // 显示柱子上的数值
        isHighlightEnabled = true            // 启用高亮效果
        valueTextColor = Color.Black.toArgb()  // 数值文字颜色
        valueTextSize = 10f                  // 数值文字大小
        valueFormatter = formatter           // 数值格式化器
    }

    // 配置图表数据
    chart.data = BarData(dataSet).apply {
        barWidth = 0.5f                      // 柱子宽度
        setValueTextSize(10f)                // 数值文字大小
        setValueFormatter(formatter)         // 数值格式化器
        setValueTextColor(topActivity.getCompatColor(R.color.color_84878C))  // 数值文字颜色
    }

    // === X 轴配置 ===
    with(chart.xAxis) {
        position = XAxis.XAxisPosition.BOTTOM   // X轴在底部显示
        setDrawGridLines(false)                 // 不绘制网格线
        setDrawAxisLine(true)                   // 绘制轴线
        axisLineColor = topActivity.getCompatColor(R.color.color_84878C)       // 轴线颜色
        axisLineWidth = 1f                      // 轴线宽度
        textSize = 11f                          // 轴标签文字大小
        textColor = topActivity.getCompatColor(R.color.color_84878C)        // 轴标签文字颜色
        granularity = 1f                        // 最小刻度间隔
        labelCount = visibleDays                // 显示的标签数量
        setCenterAxisLabels(false)              // 不居中显示轴标签
        axisMinimum = -0.5f                     // X轴最小值
        axisMaximum = expenses.size.toFloat() - 0.5f  // X轴最大值

        // X轴标签格式化器，显示日期
        valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val index = value.toInt()
                return if (index in expenses.indices) expenses[index].date else ""
            }
        }
    }

    // === Y 轴（左侧）配置 ===
    with(chart.axisLeft) {
        setDrawGridLines(true)                       // 绘制网格线
        gridColor = Color.LightGray.copy(alpha = 0.3f).toArgb()  // 网格线颜色（浅灰色半透明）
        gridLineWidth = 0.5f                         // 网格线宽度
        setDrawAxisLine(true)                        // 绘制轴线
        axisLineColor = topActivity.getCompatColor(R.color.color_84878C)         // 轴线颜色
        axisLineWidth = 1f                           // 轴线宽度
        textSize = 11f                               // 轴标签文字大小
        textColor = topActivity.getCompatColor(R.color.color_84878C)         // 轴标签文字颜色
        axisMinimum = 0f                             // Y轴最小值从0开始
        valueFormatter = yValueFormatter             // Y轴数值格式化器（带¥符号）
    }

    // === Y 轴（右侧）配置 ===
    chart.axisRight.apply {
        setDrawGridLines(false)    // 不绘制网格线
        setDrawAxisLine(false)     // 不绘制轴线
        setDrawLabels(false)       // 不显示标签
    }

    // === 图表基础设置 ===
    chart.apply {
        description.isEnabled = false          // 禁用描述文本
        legend.isEnabled = false               // 禁用图例
        setDrawGridBackground(false)           // 不绘制网格背景
        setDrawBorders(false)                  // 不绘制边框

        // 交互设置
        setTouchEnabled(true)                  // 启用触摸交互
        setDragEnabled(true)                   // 启用拖拽
        isDoubleTapToZoomEnabled = false       // 禁用双击缩放
        isAutoScaleMinMaxEnabled = false       // 禁用自动缩放

        // 布局设置
        setExtraOffsets(6f, 20f, 15f, 20f)     // 设置额外边距（左6，上20，右15，下20）
        setFitBars(true)                       // 自动调整柱子宽度适应屏幕
        setDrawValueAboveBar(true)             // 在柱子上方显示数值
        setDrawBarShadow(false)                // 不绘制柱子阴影

        // 可见范围设置
        setVisibleXRangeMaximum(visibleDays.toFloat())  // 最大可见范围
        setVisibleXRangeMinimum(visibleDays.toFloat())  // 最小可见范围

        // 初始位置：从最左侧开始
        moveViewToX(-0.5f)

        // 动画效果
        animateY(800)

        // 设置点击监听器
        onBarClick?.let { callback ->
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let { entry ->
                        val index = entry.x.toInt()
                        if (index in expenses.indices) {
                            val selectedData = expenses[index]
                            LogUtils.d("柱状图点击：${selectedData}")  // 日志记录点击事件
                            callback(selectedData)                    // 执行点击回调
                        }
                    }
                }

                override fun onNothingSelected() {
                    // 点击空白区域时清除选中状态
                    highlightValues(null)
                }
            })
        }

        // 刷新图表
        notifyDataSetChanged()
        invalidate()

        // 延迟确保布局正确（尤其首次加载）
        postDelayed({
            setVisibleXRangeMaximum(visibleDays.toFloat())
            moveViewToX(-0.5f)
            invalidate()
        }, 100)
    }
}

/**
 * 每日交易数据柱状图组件
 *
 * @param data 每日交易数据列表
 * @param modifier Compose修饰符
 * @param visibleDays 同时可见的天数，默认5天
 * @param onBarClick 柱子点击回调函数
 */
@Composable fun DailyBarChart(
    data: List<DailyTransactionData>, modifier: Modifier = Modifier, visibleDays: Int = 5, onBarClick: ((DailyTransactionData) -> Unit)
) {
    // 缓存上一次的输入，用于比较是否需要更新
    var lastExpenses by remember { mutableStateOf<List<DailyTransactionData>>(emptyList()) }
    var lastVisibleDays by remember { mutableStateOf<Int?>(null) }

    AndroidView(
        factory = { context ->
        BarChart(context).apply {
            configureDailyChart(this, data, visibleDays, onBarClick)
            lastExpenses = data.toList()
            lastVisibleDays = visibleDays
        }
    }, update = { chart ->
        // 检查是否需要更新：数据或可见天数发生变化
        val shouldUpdate = lastExpenses != data || lastVisibleDays != visibleDays

        if (shouldUpdate) {
            LogUtils.d("每日支出柱状图数据变更，执行刷新！")  // 日志记录更新事件
            configureDailyChart(chart, data, visibleDays, onBarClick)
            // 更新缓存（创建副本防止外部修改干扰下次比较）
            lastExpenses = data.toList()
            lastVisibleDays = visibleDays
        } else {
            LogUtils.d("每日支出柱状图数据未变，跳过刷新")  // 日志记录跳过事件
        }
    }, modifier = modifier
    )
}