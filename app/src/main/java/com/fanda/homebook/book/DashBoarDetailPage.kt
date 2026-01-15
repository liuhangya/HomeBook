package com.fanda.homebook.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.fanda.homebook.book.ui.DailyAmountItemWidget
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.TransactionType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) @Composable fun DashBoarDetailPage(modifier: Modifier = Modifier, navController: NavController) {

    var selectType by remember { mutableStateOf("按金额") }

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
                        text = "10月转账支出", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                    )
                    Text(
                        text = "12000", fontWeight = FontWeight.Medium, fontSize = 32.sp, color = Color.Black, modifier = modifier.padding(top = 8.dp), textAlign = TextAlign.Center
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                ) {

                    SelectableRoundedButton(
                        fontSize = 14.sp, text = "按金额", selected = selectType == "按金额", onClick = {
                            selectType = "按金额"
                        })
                    SelectableRoundedButton(
                        modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp, text = "按时间", selected = selectType == "按时间", onClick = { selectType = "按时间" })
                }
            }
            items(LocalDataSource.rankList, key = { it.category }) {
                DailyAmountItemWidget(item = it, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 8.dp))
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