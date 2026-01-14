package com.fanda.homebook.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class) @Composable fun DashBoarPage(modifier: Modifier = Modifier, navController: NavController) {
    var showSelectCategoryBottomSheet by remember { mutableStateOf(false) }
    var showMonthPlanDialog by remember { mutableStateOf(false) }
    var showSelectYearMonthBottomSheet by remember { mutableStateOf(false) }
    var curCategory by remember { mutableStateOf("全部类型") }

    val currentDate = LocalDate.now()
    var selectedYear by remember { mutableIntStateOf(currentDate.year) }
    var selectedMonth by remember { mutableIntStateOf(currentDate.monthValue) }

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopAppBar(
            title = {
                Box(
                    modifier = modifier
                        .height(64.dp)
                        .padding(start = 8.dp, end = 12.dp)
                        .fillMaxWidth()
                        .background(color = Color.Transparent)
                ) {

                    Box(modifier = Modifier
                        .wrapContentWidth()
                        .height(64.dp)      // 这里要固定高度，不然 pop 显示位置异常
                        .align(Alignment.CenterEnd)
                        .clickable(
                            // 去掉默认的点击效果
                            interactionSource = remember { MutableInteractionSource() }, indication = null
                        ) {
                            showSelectCategoryBottomSheet = true
                        }
                        .padding(start = 0.dp, end = 20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                text = curCategory, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                            )
                            Image(
                                modifier = Modifier.padding(start = 6.dp), painter = painterResource(id = R.mipmap.icon_arrow_down_black), contentDescription = null
                            )
                        }

                    }


                }

            },
            colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
        )
    }) { padding ->
        Column(modifier = modifier.padding(padding)) {

        }
    }

}


@Composable @Preview(showBackground = true) fun DashBoarPagePreview() {
    DashBoarPage(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Gray)
        .statusBarsPadding(), navController = rememberNavController())
}