package com.fanda.homebook.closet

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.closet.ui.ClosetGridWidget
import com.fanda.homebook.closet.ui.UserDropdownMenu
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.route.RoutePath

/*
*
* 衣橱页面
* */
@OptIn(ExperimentalMaterial3Api::class) @Composable fun ClosetHomePage(modifier: Modifier = Modifier, navController: NavController) {
    var expandUserMenu by remember { mutableStateOf(false) }
    var curUser by remember { mutableStateOf(LocalDataSource.userList.first()) }
    //  记录上次的返回时间
    var lastBackPressed by remember { mutableLongStateOf(0L) }

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopAppBar(title = {
            Box(modifier = Modifier
                .clickable(
                    // 去掉默认的点击效果
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPressed > 200 && !expandUserMenu) {
                        expandUserMenu = true
                    }
                    Log.d("ClosetHomePage", "点击了用户名")
                }
                .padding(start = 8.dp, end = 30.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .height(56.dp)

                        .zIndex(1f)
                ) {
                    Text(text = curUser.name, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black)
                    Image(modifier = Modifier.padding(start = 6.dp), painter = painterResource(id = R.mipmap.icon_arrow_down_black), contentDescription = null)
                }
                UserDropdownMenu(curUser = curUser, data = LocalDataSource.userList, expanded = expandUserMenu, dpOffset = DpOffset(0.dp, 56.dp), onDismiss = {
                    lastBackPressed = System.currentTimeMillis()
                    expandUserMenu = false
                    Log.d("ClosetHomePage", "点击了用户菜单")
                }, onConfirm = {
                    expandUserMenu = false
                    curUser = it
                })
            }
        }, colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent), actions = {
            Image(
                modifier = Modifier.clickable(
                    // 去掉默认的点击效果
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    Log.d("ClosetHomePage", "点击了添加按钮")
                }, painter = painterResource(id = R.mipmap.icon_add_grady), contentDescription = null
            )
            Spacer(modifier = Modifier.width(20.dp))
            Image(
                modifier = Modifier.clickable(
                    // 去掉默认的点击效果
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    navController.navigate(RoutePath.ClosetEditCategory.route)
                }, painter = painterResource(id = R.mipmap.icon_setting), contentDescription = null
            )
        }, modifier = Modifier.padding(end = 20.dp))
    }) { padding ->
        ClosetGridWidget(modifier = Modifier.padding(padding))
    }

}

@Composable @Preview(showBackground = true) fun ClosetHomePagePreview() {
    ClosetHomePage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}