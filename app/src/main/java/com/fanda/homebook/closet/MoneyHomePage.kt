package com.fanda.homebook.closet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fanda.homebook.R
import com.fanda.homebook.closet.viewmodel.HomeClosetViewModel
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils

/**
 * 衣橱首页
 *
 * 衣橱功能的入口页面，显示所有分类的网格视图
 */
@OptIn(ExperimentalMaterial3Api::class) @Composable fun MoneyHomePage(
    modifier: Modifier = Modifier, navController: NavController, closetViewModel: HomeClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 下拉菜单展开状态
    var expandUserMenu by remember { mutableStateOf(false) }
    // 记录上次返回按键时间（用于双击检测）
    var lastBackPressed by remember { mutableLongStateOf(0L) }

    // 从ViewModel收集状态
    val curSelectOwner by closetViewModel.curSelectOwner.collectAsState()
    val closetUiState by closetViewModel.addClosetUiState.collectAsState()
    val groupedClosets by closetViewModel.groupedClosets.collectAsState()
    val trashData by closetViewModel.trashData.collectAsState()

    LogUtils.i("closets", groupedClosets)
    LogUtils.i("groupedTrashClosets", trashData)

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = modifier
                            .height(64.dp)
                            .padding(start = 8.dp, end = 12.dp)
                            .fillMaxWidth()
                            .background(color = Color.Transparent)
                    ) {
                        // 左侧：归属选择区域
                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .height(64.dp) // 固定高度，确保弹出菜单位置正确
                            .align(Alignment.CenterStart)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() }, indication = null
                                ) {
                                    // 处理归属选择点击（防误触）
                                    val now = System.currentTimeMillis()
                                    if (now - lastBackPressed > 200 && !expandUserMenu) {
                                        expandUserMenu = true
                                    }
                                }
                                .padding(start = 0.dp, end = 30.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()
                            ) {
                                // 当前归属名称
                                Text(
                                    text = curSelectOwner?.name ?: "", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                                )
                                // 下拉箭头
                                Image(
                                    modifier = Modifier.padding(start = 6.dp), painter = painterResource(id = R.mipmap.icon_arrow_down_black), contentDescription = "下拉选择归属"
                                )
                            }

                        }

                        // 右侧：添加和管理按钮
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            // 添加按钮（拍照/选择图片）
                            Box(
                                contentAlignment = Alignment.Center, modifier = Modifier
                                    .size(44.dp)
                                    .clickable {
                                        closetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                                    }) {
                                Image(
                                    painter = painterResource(id = R.mipmap.icon_add_grady), contentDescription = "添加衣橱物品", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                                )
                            }

                            // 设置按钮（进入分类管理）
                            Box(
                                contentAlignment = Alignment.Center, modifier = Modifier
                                    .size(44.dp)
                                    .clickable {
                                        navController.navigate(RoutePath.EditCategory.route)
                                    }) {
                                Image(
                                    painter = painterResource(id = R.mipmap.icon_setting), contentDescription = "分类管理", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
            )
        }) { padding ->
//        MyScreen(modifier = Modifier.padding(padding))
        PartialBottomSheet(modifier = Modifier.padding(padding))

    }

    // 图片选择弹窗
    SelectPhotoBottomSheet(
        visible = closetUiState.sheetType == ShowBottomSheetType.SELECT_IMAGE, onDismiss = {
            closetViewModel.dismissBottomSheet()
        }) { selectedUri ->
        closetViewModel.dismissBottomSheet()
        // 跳转到添加衣橱页面，传递选择的图片路径
        navController.navigate("${RoutePath.AddCloset.route}?imagePath=${selectedUri}&categoryId=-1")
    }
}



@Composable
fun MyScreen(modifier: Modifier = Modifier) {
    var showSheet by remember { mutableStateOf(false) }

    Button(modifier = modifier,onClick = { showSheet = true }) {
        Text("打开筛选弹窗")
    }

    // 使用基本版本
    CustomBottomSheet(
        visible = showSheet,
        onDismiss = { showSheet = false },
        maxHeight = 500.dp, // 自定义最大高度
    ) {
        // 你的内容
        LazyColumn (
            modifier = Modifier.fillMaxWidth()
        ) {
            items(20) { index ->
                Text(
                    text = "项目 $index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class) @Composable
fun PartialBottomSheet(modifier: Modifier = Modifier) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = { showBottomSheet = true }
        ) {
            Text("Display partial bottom sheet")
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                modifier = Modifier.fillMaxHeight(),
                sheetState = sheetState,
                onDismissRequest = { showBottomSheet = false }
            ) {
                Text(
                    "Swipe up to open sheet. Swipe down to dismiss.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
