//package com.fanda.homebook.book.ui
//
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.viewinterop.AndroidView
//import com.github.mikephil.charting.charts.BarChart
//import com.github.mikephil.charting.components.AxisBase
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.BarData
//import com.github.mikephil.charting.data.BarDataSet
//import com.github.mikephil.charting.data.BarEntry
//import com.github.mikephil.charting.formatter.ValueFormatter
//import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
//import java.text.DecimalFormat
//
//data class MonthlyBarData(
//    val month: String, val value: Float, val color: Color = Color.Blue
//)
//
//@Composable fun MonthlyBarChart(
//    barData: List<MonthlyBarData>, modifier: Modifier = Modifier
//) {
//    AndroidView(
//        factory = { context ->
//            BarChart(context).apply {
//                description.isEnabled = false
//                legend.isEnabled = false
//                setDrawGridBackground(false)
//
//                // 禁用交互
//                setTouchEnabled(false)
//                setPinchZoom(false)
//                setScaleEnabled(false)
//
//                // 创建Bar数据
//                val barEntries = barData.mapIndexed { index, item ->
//                    BarEntry(index.toFloat(), item.value, item.month)
//                }
//
//                val barDataSet = BarDataSet(barEntries, "月度数据").apply {
//                    colors = barData.map { it.color.toArgb() }
//                    setDrawValues(true)
//                    valueTextSize = 10f
//                    valueTextColor = Color.Black.toArgb()
//                    valueFormatter = object : ValueFormatter() {
//                        private val format = DecimalFormat("#,###")
//
//                        override fun getFormattedValue(value: Float): String {
//                            return "¥" + format.format(value)
//                        }
//                    }
//                }
//
//                val dataSets = mutableListOf<IBarDataSet>()
//                dataSets.add(barDataSet)
//
//                val barDataObj = BarData(dataSets).apply {
//                    barWidth = 0.6f
//                }
//
//                this.data = barDataObj
//
//                // X轴设置
//                val xAxis = xAxis
//                xAxis.position = XAxis.XAxisPosition.BOTTOM
//                xAxis.setDrawGridLines(false)
//                xAxis.textSize = 12f
//                xAxis.granularity = 1f
//                xAxis.valueFormatter = object : ValueFormatter() {
//                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
//                        val index = value.toInt()
//                        return if (index >= 0 && index < barData.size) {
//                            barData[index].month
//                        } else {
//                            ""
//                        }
//                    }
//                }
//
//                // Y轴设置
//                axisLeft.setDrawGridLines(true)
//                axisLeft.gridColor = Color.LightGray.copy(alpha = 0.3f).toArgb()
//                axisLeft.axisMinimum = 0f  // 设置Y轴最小值为0，这样柱子就会贴着X轴
//                axisLeft.textSize = 12f
//                axisRight.isEnabled = false
//
//                animateY(1000)
//            }
//        }, modifier = modifier
//    )
//}
//
