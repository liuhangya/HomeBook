package com.fanda.homebook.quick

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fanda.homebook.components.CustomTopAppBar
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.R
import com.fanda.homebook.components.GradientRoundedBoxWithStroke

/*
* 记一笔页面
* */
@Composable
fun QuickHomePage(modifier: Modifier = Modifier, navController: NavController) {
    Scaffold(modifier = modifier, topBar = {
        CustomTopAppBar(title = RoutePath.QUICK_ADD.title, onBackClick = {
            navController.navigateUp()
        }, rightText = "保存", onRightActionClick = {

        }, backIconPainter = painterResource(R.mipmap.icon_back))
    }) { padding ->
        GradientRoundedBoxWithStroke(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(text = "记一笔页面")
        }
    }
}