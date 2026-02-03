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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.UserCache
import com.fanda.homebook.tools.roundToString

@OptIn(ExperimentalMaterial3Api::class) @Composable fun DashBoarDetailPage(
    modifier: Modifier = Modifier, navController: NavController, dashboardDetailViewModel: DashboardDetailViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    val uiState by dashboardDetailViewModel.uiState.collectAsState()

    BackHandler {
        LogUtils.d("移除分类列表数据")
        UserCache.categoryQuickList = emptyList()
        navController.navigateUp()
    }

    Scaffold(topBar = {
        // 只能用 TopAppBar 才能将背景色嵌入到状态栏
        TopAppBar(title = {}, colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(R.color.color_CDD6E4)), navigationIcon = {
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
        })
    }) { padding ->
        LazyColumn(modifier = modifier.padding(padding)) {
            item {
                // 顶部金额布局
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(colorResource(R.color.color_CDD6E4))
                        .padding(bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.title, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                    )
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
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                ) {

                    SelectableRoundedButton(
                        fontSize = 14.sp, text = "按金额", selected = uiState.queryWay == QueryWay.AMOUNT, onClick = {
                            dashboardDetailViewModel.updateQueryWay(QueryWay.AMOUNT)
                        })
                    SelectableRoundedButton(
                        modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp, text = "按时间", selected = uiState.queryWay == QueryWay.TIME, onClick = {
                            dashboardDetailViewModel.updateQueryWay(QueryWay.TIME)
                        })
                }
            }
            val filterData = when (uiState.queryWay) {
                QueryWay.AMOUNT -> uiState.data.sortedByDescending { it.quick.price.toDouble() }
                QueryWay.TIME -> uiState.data.sortedByDescending { it.quick.date }
            }
            items(filterData, key = { it.quick.id }) {
                DailyAmountItemWidget(
                    item = it, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable @Preview(showBackground = true) fun DashBoarDetailPagePreview() {
    DashBoarDetailPage(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Gray
            ), navController = rememberNavController()
    )
}