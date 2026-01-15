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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.book.ui.DailyAmountItemWidget
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.LocalDataSource

@Composable fun DashBoarRankPage(modifier: Modifier = Modifier, navController: NavController) {

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "支出排行",
            onBackClick = {
                navController.navigateUp()
            }
        )
    }) { padding ->
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.padding(padding)) {
            items( LocalDataSource.rankList, key = { it.category }){
                DailyAmountItemWidget(item = it)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun DashBoarRankPagePreview() {
    DashBoarRankPage(modifier = Modifier.fillMaxSize().background(
        Color.Gray
    ), navController = rememberNavController())
}