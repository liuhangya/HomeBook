package com.fanda.homebook.closet

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.closet.ui.ClosetGridWidget
import com.fanda.homebook.closet.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.ui.UserDropdownMenu
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.route.RoutePath

/*
*
* 衣橱页面
* */
@OptIn(ExperimentalMaterial3Api::class) @Composable fun ClosetHomePage(modifier: Modifier = Modifier, navController: NavController) {
    var showSelectImage by remember { mutableStateOf(false) }
    var expandUserMenu by remember { mutableStateOf(false) }
    var curUser by remember { mutableStateOf(LocalDataSource.userList.first()) }
    //  记录上次的返回时间
    var lastBackPressed by remember { mutableLongStateOf(0L) }

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

                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(64.dp)      // 这里要固定高度，不然 pop 显示位置异常
                        .align(Alignment.CenterStart)
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
                            .padding(start = 0.dp, end = 30.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(text = curUser.name, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black)
                            Image(modifier = Modifier.padding(start = 6.dp), painter = painterResource(id = R.mipmap.icon_arrow_down_black), contentDescription = null)
                        }
                        UserDropdownMenu(curUser = curUser, data = LocalDataSource.userList, expanded = expandUserMenu, dpOffset = DpOffset(0.dp, 50.dp), onDismiss = {
                            lastBackPressed = System.currentTimeMillis()
                            expandUserMenu = false
                            Log.d("ClosetHomePage", "点击了用户菜单")
                        }, onConfirm = {
                            expandUserMenu = false
                            curUser = it
                        })
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                showSelectImage = true
//                                navController.navigate(RoutePath.ClosetEditColor.route)
                            }) {
                            Image(
                                painter = painterResource(id = R.mipmap.icon_add_grady), contentDescription = "Action", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                            )
                        }
                        Box(contentAlignment = Alignment.Center, modifier = Modifier
                            .size(44.dp)
                            .clickable { navController.navigate(RoutePath.EditCategory.route) }) {
                            Image(
                                painter = painterResource(id = R.mipmap.icon_setting), contentDescription = "Action", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

            },
            colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
        )
    }) { padding ->
        ClosetGridWidget(modifier = Modifier.padding(padding)) {
            navController.navigate(RoutePath.ClosetDetailCategory.route)
        }
    }

    SelectPhotoBottomSheet(visible = showSelectImage, onDismiss = {
        showSelectImage = false
    }) {
        showSelectImage = false
        // 导航时保存状态
//        navController.currentBackStackEntry?.savedStateHandle?.set(
//            "selectedImageUri", it
//        )
        navController.navigate("${RoutePath.AddCloset.route}?imagePath=${it}")
    }

}

@Composable @Preview(showBackground = true) fun ClosetHomePagePreview() {
    ClosetHomePage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}