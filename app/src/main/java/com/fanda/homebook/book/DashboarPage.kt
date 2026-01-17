package com.fanda.homebook.book

import DonutChartMPWithLabels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.book.sheet.YearMonthBottomSheet
import com.fanda.homebook.book.ui.DailyAmountItemWidget
import com.fanda.homebook.book.ui.DailyExpense
import com.fanda.homebook.book.ui.DailyExpenseBarChart
import com.fanda.homebook.book.ui.MonthlyBarData
import com.fanda.homebook.book.ui.ScrollableBarChartWithIndicator
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.DashBoarItemEntity
import com.fanda.homebook.entity.TransactionType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.formatYearMonth
import java.time.LocalDate
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class) @Composable fun DashBoarPage(modifier: Modifier = Modifier, navController: NavController) {
    var showSelectYearMonthBottomSheet by remember { mutableStateOf(false) }
    val currentDate = LocalDate.now()
    var selectedYear by remember { mutableIntStateOf(currentDate.year) }
    var selectedMonth by remember { mutableIntStateOf(currentDate.monthValue) }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    var chartData by remember { mutableStateOf(generateRandomExpenseData()) }
    var expenses by remember { mutableStateOf(generateDailyExpenses()) }
    var barData by remember { mutableStateOf(generateMonthData()) }


    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController.popBackStack()
                    }) {
                    Image(
                        painter = painterResource(id = R.mipmap.icon_back), contentDescription = "Back", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(64.dp)
                        .statusBarsPadding()
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(25.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() }, indication = null
                            ) {
                                showSelectYearMonthBottomSheet = true
                            }
                            .padding(start = 4.dp, top = 14.dp, bottom = 12.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatYearMonth(selectedYear, selectedMonth), fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                        )
                        Image(
                            painter = painterResource(id = R.mipmap.icon_down), contentDescription = null, modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                    ) {

                        SelectableRoundedButton(
                            fontSize = 12.sp, text = "支出", selected = transactionType == TransactionType.EXPENSE, onClick = {
                                transactionType = TransactionType.EXPENSE
                            })
                        SelectableRoundedButton(
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 12.sp,
                            text = "入账",
                            selected = transactionType == TransactionType.INCOME,
                            onClick = { transactionType = TransactionType.INCOME })
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = colorResource(R.color.color_CDD6E4)),
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 顶部金额布局
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(colorResource(R.color.color_CDD6E4))
                    .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "总支出", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                )
                Text(
                    text = "5600", fontWeight = FontWeight.Medium, fontSize = 32.sp, color = Color.Black, modifier = modifier.padding(top = 8.dp), textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.color_E3EBF5))
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                PieChatWidget(chartData) {
                    navController.navigate(RoutePath.DashBoarDetail.route)
                }
                DailyBarChatWidget(expenses = expenses)
                MonthBarChatWidget(barData = barData)
                MonthRankWidget() {
                    navController.navigate(RoutePath.DashBoarRank.route)
                }
            }
        }

    }


    YearMonthBottomSheet(year = selectedYear, month = selectedMonth, visible = showSelectYearMonthBottomSheet, onDismiss = {
        showSelectYearMonthBottomSheet = false
    }) { year, month ->
        showSelectYearMonthBottomSheet = false
        selectedYear = year
        selectedMonth = month
        LogUtils.d("选中的年月${year}-${month}")
        chartData = generateRandomExpenseData()
        expenses = generateDailyExpenses()
        barData = generateMonthData()
    }
}

@Composable fun MonthRankWidget(modifier: Modifier = Modifier, onAllClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "10月支出排行", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "全部",
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = colorResource(R.color.color_84878C),
            modifier = modifier     // 先设置点击事件，再设置 padding ，这样才能扩大点击区域
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    onAllClick()
                }
                .padding(start = 50.dp))
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LocalDataSource.rankList.forEach {
            DailyAmountItemWidget(item = it)
        }
    }

}


// 每月对比柱状图
@Composable fun MonthBarChatWidget(modifier: Modifier = Modifier, barData: List<MonthlyBarData>) {
    Text(
        text = "月度对比", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier.padding(top = 24.dp, bottom = 12.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {

        ScrollableBarChartWithIndicator(
            barData = barData, modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 5.dp, bottom = 12.dp)
                .height(250.dp)
        )
    }
}

// 每日对比柱状图
@Composable fun DailyBarChatWidget(modifier: Modifier = Modifier, expenses: List<DailyExpense>) {
    Text(
        text = "每日对比", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier.padding(top = 24.dp, bottom = 12.dp)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        DailyExpenseBarChart(
            expenses = expenses, modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 12.dp, top = 15.dp, bottom = 12.dp)
                .height(250.dp), visibleDays = 7
        )
    }
}

// 圆环图
@Composable fun PieChatWidget(data: List<Pair<String, Float>>, modifier: Modifier = Modifier, onItemClick: (DashBoarItemEntity) -> Unit) {
    Text(
        text = "支出构成", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier.padding(top = 24.dp, bottom = 12.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
    ) {

        DonutChartMPWithLabels(
            data = data, modifier = Modifier
                .size(300.dp)
                .padding(50.dp)
                .align(Alignment.Center)
        )


    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, Color.White.copy(0.4f), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        LocalDataSource.dashBoarList.forEach {
            GradientRoundedBoxWithStroke(
                colors = listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.2f)), modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onItemClick(it)
                    }
                    .padding(start = 15.dp)
                    .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier
                            .size(32.dp)
                            .clip(
                                CircleShape
                            )
                            .background(Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.icon_shopping), contentDescription = null, modifier = Modifier.scale(0.8f)

                        )

                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Column(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .weight(1f), verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = it.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // 进度是 0 - 1
                            LinearProgressIndicator(
                                progress = { it.ratio },
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color.Black,
                                trackColor = Color.Transparent
                            )

                        }

                        Text(
                            textAlign = TextAlign.End,
                            text = it.amount.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .widthIn(70.dp),
                            color = Color.Black
                        )

                    }
                }
            }
        }
    }
}


// 数据生成函数
fun generateDailyExpenses(): List<DailyExpense> {
    val expenses = mutableListOf<DailyExpense>()
    repeat(Random.nextInt(7, 30)){
        expenses.add(DailyExpense("9.${it+10}", 450f *Random.nextInt(1, 10)))
    }
    return expenses
}

fun generateMonthData() = (1..Random.nextInt(2, 12)).map { month ->

    val monthName = when (month) {
        1 -> "1月"
        2 -> "2月"
        3 -> "3月"
        4 -> "4月"
        5 -> "5月"
        6 -> "6月"
        7 -> "7月"
        8 -> "8月"
        9 -> "9月"
        10 -> "10月"
        11 -> "11月"
        else -> "12月"
    }
    val value = when (month) {
        1 -> 8000f
        2 -> 12000f
        3 -> 15000f
        4 -> 18000f
        5 -> 12643.54f
        6 -> 19648.21f
        7 -> 14503.99f
        8 -> 9315.84f
        9 -> 9591.34f
        10 -> 1043.54f
        11 -> 6000f
        else -> 9000f
    }
    val color = when (month % 6) {
        0 -> Color(0xFF4CAF50)
        1 -> Color(0xFF2196F3)
        2 -> Color(0xFFFF9800)
        3 -> Color(0xFFF44336)
        4 -> Color(0xFF9C27B0)
        else -> Color(0xFF607D8B)
    }
    MonthlyBarData(monthName, value, Color(0xFF4CAF50))
}


fun generateRandomExpenseData(): List<Pair<String, Float>> {
    val categories = listOf(
        "餐饮", "交通", "服饰", "护肤", "购物", "服务", "娱乐", "生活", "其他"
    )

    // 为每个类别生成随机权重
    val weights = categories.map {
        Random.nextFloat() * 10f + 1f  // 生成1-11之间的随机权重
    }

    val totalWeight = weights.sum()

    // 计算百分比，确保总和为100%
    val percentages = weights.map { weight ->
        (weight / totalWeight * 100f).roundTo(1) // 保留1位小数
    }

    // 调整最后一项以确保总和为100%
    val adjustedPercentages = adjustTo100Percent(percentages)
    LogUtils.d("生成随机圆环图数据")
    return categories.zip(adjustedPercentages)
}

private fun Float.roundTo(decimalPlaces: Int): Float {
    val factor = 10f.pow(decimalPlaces)
    return (this * factor).roundToInt() / factor
}

private fun adjustTo100Percent(percentages: List<Float>): List<Float> {
    val total = percentages.sum()
    val diff = 100f - total

    if (diff == 0f) return percentages

    // 调整最后一项
    val adjusted = percentages.toMutableList()
    adjusted[adjusted.size - 1] = (adjusted.last() + diff).roundTo(1)

    return adjusted
}


@Composable @Preview(showBackground = true) fun DashBoarPagePreview() {
    DashBoarPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}