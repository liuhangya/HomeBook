package com.fanda.homebook.book

import DonutChartMPWithLabels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.book.sheet.YearMonthBottomSheet
import com.fanda.homebook.book.ui.DailyAmountItemWidget
import com.fanda.homebook.book.ui.DailyBarChart
import com.fanda.homebook.book.ui.ScrollableBarChartWithIndicator
import com.fanda.homebook.book.viewmodel.DailyTransactionData
import com.fanda.homebook.book.viewmodel.DashboardSubCategoryGroupData
import com.fanda.homebook.book.viewmodel.DashboardViewModel
import com.fanda.homebook.book.viewmodel.MonthTransactionData
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.entity.TransactionAmountType
import com.fanda.homebook.quick.ui.getCategoryIcon
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.EventManager
import com.fanda.homebook.tools.EventType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.formatYearMonth
import com.fanda.homebook.tools.roundToString

@OptIn(ExperimentalMaterial3Api::class) @Composable fun DashBoardPage(
    modifier: Modifier = Modifier, navController: NavController, dashboardViewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 收集状态流
    val uiState by dashboardViewModel.uiState.collectAsState()
    val transactionDataByDate by dashboardViewModel.transactionDataByDate.collectAsState()
    val transactionDataByCategory by dashboardViewModel.transactionDataByCategory.collectAsState()
    val transactionDataByDaily by dashboardViewModel.transactionDataByDaily.collectAsState()
    val transactionDataByMonth by dashboardViewModel.transactionDataByMonth.collectAsState()

    LogUtils.d("uiState: $uiState")
    LogUtils.d("transactionDataByDate: $transactionDataByDate")
    LogUtils.d("transactionDataByCategory: $transactionDataByCategory")
    LogUtils.d("transactionDataByDaily: $transactionDataByDaily")
    LogUtils.d("transactionDataByMonth: $transactionDataByMonth")

    // 监听事件总线，接收刷新数据事件
    LaunchedEffect(Unit) {
        EventManager.events.collect { event ->
            when (event.type) {
                EventType.REFRESH -> {
                    LogUtils.d("收到刷新数据事件")
                    dashboardViewModel.refresh()
                }

                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    // 返回按钮
                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                navController.popBackStack()  // 返回上一页
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
                        // 左侧：年月选择器
                        Row(
                            modifier = Modifier
                                .wrapContentWidth()
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(25.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() }, indication = null
                                ) {
                                    dashboardViewModel.updateSheetType(ShowBottomSheetType.YEAR_MONTH)
                                }
                                .padding(start = 4.dp, top = 14.dp, bottom = 12.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatYearMonth(uiState.year, uiState.month), fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                            )
                            Image(
                                painter = painterResource(id = R.mipmap.icon_down), contentDescription = null, modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // 右侧：交易类型切换按钮
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                        ) {
                            // 支出按钮
                            SelectableRoundedButton(
                                fontSize = 12.sp, text = "支出", selected = uiState.transactionAmountType == TransactionAmountType.EXPENSE, onClick = {
                                    dashboardViewModel.updateTransactionAmountType(TransactionAmountType.EXPENSE)
                                })
                            // 收入按钮
                            SelectableRoundedButton(
                                modifier = Modifier.padding(start = 8.dp), fontSize = 12.sp, text = "入账", selected = uiState.transactionAmountType == TransactionAmountType.INCOME, onClick = {
                                    dashboardViewModel.updateTransactionAmountType(TransactionAmountType.INCOME)
                                })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = colorResource(R.color.color_CDD6E4)),
            )
        }) { padding ->
        // 主内容区域
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 顶部金额统计区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(colorResource(R.color.color_CDD6E4))
                    .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dashboardViewModel.getTotalAmountTitle(), fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                )
                Text(
                    text = dashboardViewModel.getTotalAmountText(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 32.sp,
                    color = Color.Black,
                    modifier = modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            // 数据可视化区域
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.color_E3EBF5))
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                // 饼图：分类构成
                if (transactionDataByCategory.isNotEmpty()) {
                    PieChatWidget(
                        data = transactionDataByCategory, title = dashboardViewModel.getPieChatTitle()
                    ) { categoryData ->
                        dashboardViewModel.saveCategoryDataList(categoryData.data)
                        navController.navigate(
                            "${RoutePath.DashBoardDetail.route}?title=${dashboardViewModel.getCategoryDetailTitle(categoryData.category.name)}"
                        )
                    }
                }

                // 柱状图：每日对比
                DailyBarChatWidget(
                    data = transactionDataByDaily
                ) { dailyData ->
                    if (dailyData.data.isEmpty()) return@DailyBarChatWidget
                    dashboardViewModel.saveCategoryDataList(dailyData.data)
                    navController.navigate(
                        "${RoutePath.DashBoardDetail.route}?title=${dashboardViewModel.getDayCategoryDetailTitle(dailyData.displayDate)}"
                    )
                }

                // 柱状图：月度对比
                MonthBarChatWidget(
                    barData = transactionDataByMonth
                ) { monthData ->
                    if (monthData.data.isEmpty()) return@MonthBarChatWidget
                    dashboardViewModel.saveCategoryDataList(monthData.data)
                    navController.navigate(
                        "${RoutePath.DashBoardDetail.route}?title=${dashboardViewModel.getDayCategoryDetailTitle(monthData.monthName)}"
                    )
                }

                // 排行榜：交易排行
                if (transactionDataByDate.isNotEmpty()) {
                    MonthRankWidget(
                        data = transactionDataByDate.take(10),  // 取前10条
                        title = dashboardViewModel.getRankTitle(), onAllClick = {
                            // 查看全部排行
                            navController.navigate(
                                "${RoutePath.DashBoardRank.route}?year=${uiState.year}&month=${uiState.month}&type=${uiState.transactionAmountType.ordinal}&title=${dashboardViewModel.getRankTitle()}"
                            )
                        }, onItemClick = { addQuickEntity ->
                            // 点击单条交易记录（可扩展编辑功能）
                            // navController.navigate("${RoutePath.WatchAndEditQuick.route}?quickId=${addQuickEntity.quick.id}")
                        }, onDelete = { addQuickEntity ->
                            // 删除交易记录（可扩展删除功能）
                            // dashboardViewModel.deleteQuickDatabase(addQuickEntity.quick)
                        })
                }
            }
        }
    }

    // 年月选择底部弹窗
    YearMonthBottomSheet(
        year = uiState.year, month = uiState.month, visible = dashboardViewModel.showBottomSheet(ShowBottomSheetType.YEAR_MONTH), onDismiss = {
            dashboardViewModel.dismissBottomSheet()
        }) { year, month ->
        dashboardViewModel.dismissBottomSheet()
        dashboardViewModel.updateQueryDate(year, month)
        LogUtils.d("选中的年月${year}-${month}")
    }
}

/**
 * 月度排行榜组件
 *
 * @param modifier 修饰符
 * @param title 排行榜标题
 * @param data 交易数据列表
 * @param onAllClick 查看全部点击回调
 * @param onItemClick 单条记录点击回调
 * @param onDelete 删除记录回调
 */
@Composable fun MonthRankWidget(
    modifier: Modifier = Modifier, title: String, data: List<AddQuickEntity>, onAllClick: () -> Unit, onItemClick: (AddQuickEntity) -> Unit, onDelete: (AddQuickEntity) -> Unit
) {
    // 标题行
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "全部", fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, fontSize = 12.sp, color = colorResource(R.color.color_84878C), modifier = modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    onAllClick()
                }
                .padding(start = 50.dp))
    }

    // 交易记录列表
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEach { addQuickEntity ->
            DailyAmountItemWidget(
                item = addQuickEntity, onItemClick = onItemClick, onDelete = onDelete
            )
        }
    }
}

/**
 * 月度对比柱状图组件
 *
 * @param modifier 修饰符
 * @param barData 月度交易数据
 * @param onBarClick 柱子点击回调
 */
@Composable fun MonthBarChatWidget(
    modifier: Modifier = Modifier, barData: List<MonthTransactionData>, onBarClick: ((MonthTransactionData) -> Unit)
) {
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
                .height(250.dp), onBarClick = onBarClick
        )
    }
}

/**
 * 每日对比柱状图组件
 *
 * @param modifier 修饰符
 * @param data 每日交易数据
 * @param onBarClick 柱子点击回调
 */
@Composable fun DailyBarChatWidget(
    modifier: Modifier = Modifier, data: List<DailyTransactionData>, onBarClick: ((DailyTransactionData) -> Unit)
) {
    Text(
        text = "每日对比", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier.padding(top = 24.dp, bottom = 12.dp)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        DailyBarChart(
            data = data, modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 12.dp, top = 15.dp, bottom = 12.dp)
                .height(250.dp), visibleDays = 7,  // 显示7天的数据
            onBarClick = onBarClick
        )
    }
}

/**
 * 圆环图组件（分类构成）
 *
 * @param data 分类分组数据
 * @param title 图表标题
 * @param modifier 修饰符
 * @param onItemClick 分类项点击回调
 */
@Composable fun PieChatWidget(
    data: List<DashboardSubCategoryGroupData>, title: String, modifier: Modifier = Modifier, onItemClick: (DashboardSubCategoryGroupData) -> Unit
) {
    Text(
        text = title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier.padding(top = 24.dp, bottom = 12.dp)
    )

    // 圆环图容器
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        // 准备圆环图数据：分类名称和占比
        val chatData = data.map { Pair(it.category.name, it.ratio) }

        // 显示圆环图
        DonutChartMPWithLabels(
            data = chatData, modifier = Modifier
                .size(300.dp)
                .padding(50.dp)
                .align(Alignment.Center)
        )
    }

    // 分类详情列表
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp)
    ) {
        data.forEach { categoryData ->
            GradientRoundedBoxWithStroke(
                colors = listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.2f)), modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onItemClick(categoryData)
                    }
                    .padding(start = 15.dp)
                    .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                    // 分类图标
                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = getCategoryIcon(categoryData.category.type)), contentDescription = null, modifier = Modifier.scale(0.8f)
                        )
                    }

                    // 分类信息和进度条
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .weight(1f), verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = categoryData.category.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black
                                )
                                Text(
                                    text = "${data.size}笔", fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colorResource(R.color.color_84878C), modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            // 进度条：显示分类占比
                            LinearProgressIndicator(
                                progress = { categoryData.ratio },
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color.Black,
                                trackColor = Color.Transparent
                            )
                        }

                        // 分类总金额
                        Text(
                            textAlign = TextAlign.End,
                            text = categoryData.totalAmount.toFloat().roundToString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .widthIn(70.dp),  // 最小宽度
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable @Preview(showBackground = true) fun DashBoardPagePreview() {
    DashBoardPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController(), dashboardViewModel = viewModel(factory = AppViewModelProvider.factory)
    )
}