package com.fanda.homebook.book

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.fanda.homebook.book.state.QueryWay
import com.fanda.homebook.book.ui.DailyAmountItemWidget
import com.fanda.homebook.book.viewmodel.DashboardDetailViewModel
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.tools.EventManager
import com.fanda.homebook.tools.EventType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.UserCache
import com.fanda.homebook.tools.roundToString

@OptIn(ExperimentalMaterial3Api::class) @Composable fun DashBoarDetailPage(
    modifier: Modifier = Modifier, navController: NavController, dashboardDetailViewModel: DashboardDetailViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 收集UI状态
    val uiState by dashboardDetailViewModel.uiState.collectAsState()

    // 处理返回导航：清除缓存数据并返回上一页
    val handleBackNavigation = {
        LogUtils.d("移除分类列表数据")
        UserCache.categoryQuickList = emptyList()  // 清空用户缓存中的分类数据
        navController.navigateUp()  // 返回上一页
    }

    // 监听事件总线，接收刷新数据事件
    LaunchedEffect(Unit) {
        EventManager.events.collect { event ->
            when (event.type) {
                EventType.REFRESH -> {
                    LogUtils.d("收到刷新数据事件")
                    dashboardDetailViewModel.refresh(event.data as Int)
                }

                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            // 使用TopAppBar将背景色扩展到状态栏区域
            TopAppBar(
                title = {}, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.color_CDD6E4)  // 设置状态栏背景色
                ), navigationIcon = {
                    // 返回按钮
                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                handleBackNavigation()
                            }) {
                        Image(
                            painter = painterResource(id = R.mipmap.icon_back), contentDescription = "Back", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                        )
                    }
                })
        }) { padding ->
        // 拦截系统返回键和手势返回
        BackHandler {
            handleBackNavigation()
        }

        LazyColumn(modifier = modifier.padding(padding)) {
            // 头部：标题和总金额
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(colorResource(R.color.color_CDD6E4))  // 与状态栏相同背景色
                        .padding(bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 页面标题
                    Text(
                        text = uiState.title, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                    )

                    // 总金额
                    Text(
                        text = uiState.data.sumOf { it.quick.price.toDouble() }.toFloat().roundToString(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 32.sp,
                        color = Color.Black,
                        modifier = modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 查询方式切换按钮
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // 按金额排序按钮
                    SelectableRoundedButton(
                        fontSize = 14.sp, text = "按金额", selected = uiState.queryWay == QueryWay.AMOUNT, onClick = {
                            dashboardDetailViewModel.updateQueryWay(QueryWay.AMOUNT)
                        })

                    // 按时间排序按钮
                    SelectableRoundedButton(
                        modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp, text = "按时间", selected = uiState.queryWay == QueryWay.TIME, onClick = {
                            dashboardDetailViewModel.updateQueryWay(QueryWay.TIME)
                        })
                }
            }

            // 数据列表：根据查询方式排序
            val filterData = when (uiState.queryWay) {
                QueryWay.AMOUNT -> uiState.data.sortedByDescending { it.quick.price.toDouble() }  // 按金额降序
                QueryWay.TIME -> uiState.data.sortedByDescending { it.quick.date }  // 按时间降序（最新的在前）
            }

            items(filterData, key = { it.quick.id }) { addQuickEntity ->
                DailyAmountItemWidget(item = addQuickEntity, onItemClick = { addQuickEntity ->
                    // 可以添加点击查看/编辑功能
                    // navController.navigate("${RoutePath.WatchAndEditQuick.route}?quickId=${addQuickEntity.quick.id}")
                }, onDelete = { addQuickEntity ->
                    // 可以添加删除功能
                    // dashboardDetailViewModel.deleteQuickDatabase(addQuickEntity.quick)
                })
            }
        }
    }
}

@Composable @Preview(showBackground = true) fun DashBoarDetailPagePreview() {
    DashBoarDetailPage(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray), navController = rememberNavController()
    )
}