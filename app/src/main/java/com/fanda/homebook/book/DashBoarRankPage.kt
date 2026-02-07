package com.fanda.homebook.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.book.ui.DailyAmountItemWidget
import com.fanda.homebook.book.viewmodel.DashboardViewModel
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.tools.EventManager
import com.fanda.homebook.tools.EventType
import com.fanda.homebook.tools.LogUtils

/**
 * 仪表板排行页面
 * 显示交易数据的排行榜列表，按时间顺序或金额大小排序
 *
 * @param modifier Compose修饰符
 * @param navController 导航控制器
 * @param dashboardViewModel 仪表板ViewModel
 */
@Composable fun DashBoarRankPage(
    modifier: Modifier = Modifier, navController: NavController, dashboardViewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 收集UI状态和数据
    val uiState by dashboardViewModel.uiState.collectAsState()
    val transactionDataByDate by dashboardViewModel.transactionDataByDate.collectAsState()

    LogUtils.d("transactionDataByDate: $transactionDataByDate")

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
        modifier = modifier.statusBarsPadding(), topBar = {
            // 自定义顶部导航栏
            TopIconAppBar(
                title = uiState.title, onBackClick = {
                    navController.navigateUp()  // 返回上一页
                })
        }) { padding ->
        // 交易列表
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp
            ), verticalArrangement = Arrangement.spacedBy(8.dp),  // 列表项间距
            modifier = modifier.padding(padding)
        ) {
            // 遍历交易数据并创建列表项
            items(transactionDataByDate, key = { it.quick.id }  // 使用交易ID作为key，优化重组性能
            ) { addQuickEntity ->
                DailyAmountItemWidget(item = addQuickEntity, onItemClick = { addQuickEntity ->
                    // 点击交易项：可以扩展查看/编辑功能
                    // navController.navigate("${RoutePath.WatchAndEditQuick.route}?quickId=${addQuickEntity.quick.id}")
                }, onDelete = { addQuickEntity ->
                    // 删除交易项：可以扩展删除功能
                    // dashboardViewModel.deleteQuickDatabase(addQuickEntity.quick)
                })
            }
        }
    }
}

@Composable @Preview(showBackground = true) fun DashBoarRankPagePreview() {
    DashBoarRankPage(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray), navController = rememberNavController(), viewModel(factory = AppViewModelProvider.factory)
    )
}