package com.fanda.homebook.book

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.VerticalDivider
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
import com.fanda.homebook.book.ui.DailyItemWidget
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.DashBoarItemEntity
import com.fanda.homebook.entity.TransactionType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.formatYearMonth
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class) @Composable fun DashBoarPage(modifier: Modifier = Modifier, navController: NavController) {
    var showSelectYearMonthBottomSheet by remember { mutableStateOf(false) }
    val currentDate = LocalDate.now()
    var selectedYear by remember { mutableIntStateOf(currentDate.year) }
    var selectedMonth by remember { mutableIntStateOf(currentDate.monthValue) }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }


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
                RingChatWidget() {
                    navController.navigate(RoutePath.DashBoarDetail.route)
                }
                DailyBarChatWidget()
                MonthBarChatWidget()
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
@Composable fun MonthBarChatWidget(modifier: Modifier = Modifier) {
    Text(
        text = "月度对比", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier.padding(top = 24.dp, bottom = 12.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        Text(
            text = "月度柱状图", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(54.dp)
        )
    }
}

// 每日对比柱状图
@Composable fun DailyBarChatWidget(modifier: Modifier = Modifier) {
    Text(
        text = "每日对比", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier.padding(top = 24.dp, bottom = 12.dp)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        Text(
            text = "每日柱状图", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(54.dp)
        )
    }
}

// 圆环图
@Composable fun RingChatWidget(modifier: Modifier = Modifier, onItemClick: (DashBoarItemEntity) -> Unit) {
    Text(
        text = "支出构成", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = modifier.padding(top = 24.dp, bottom = 12.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
    ) {
        Text(
            text = "圆环图", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(54.dp)
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


@Composable @Preview(showBackground = true) fun DashBoarPagePreview() {
    DashBoarPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}