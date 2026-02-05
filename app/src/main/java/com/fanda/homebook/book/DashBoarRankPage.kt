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
import com.fanda.homebook.book.ui.CustomLongPressTooltip
import com.fanda.homebook.book.viewmodel.DashboardViewModel
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.EventManager
import com.fanda.homebook.tools.EventType
import com.fanda.homebook.tools.LogUtils

@Composable fun DashBoarRankPage(modifier: Modifier = Modifier, navController: NavController, dashboardViewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.factory)) {

    val uiState by dashboardViewModel.uiState.collectAsState()
    val transactionDataByDate by dashboardViewModel.transactionDataByDate.collectAsState()

    LogUtils.d("transactionDataByDate: $transactionDataByDate")

    LaunchedEffect(Unit) {
        EventManager.events.collect {
            when (it.type) {
                EventType.REFRESH -> {
                    LogUtils.d("收到刷新数据事件")
                    dashboardViewModel.refresh()
                }
            }
        }
    }

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = uiState.title, onBackClick = {
                navController.navigateUp()
            })
    }) { padding ->
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.padding(padding)) {
            items(transactionDataByDate, key = { it.quick.id }) {
                CustomLongPressTooltip(item = it, onItemClick = { addQuickEntity ->
//                    navController.navigate("${RoutePath.WatchAndEditQuick.route}?quickId=${addQuickEntity.quick.id}")
                }, onDelete = { addQuickEntity ->
//                    dashboardViewModel.deleteQuickDatabase(addQuickEntity.quick)
                })
            }
        }
    }
}

@Composable @Preview(showBackground = true) fun DashBoarRankPagePreview() {
    DashBoarRankPage(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Gray
            ), navController = rememberNavController(), viewModel(factory = AppViewModelProvider.factory)
    )
}