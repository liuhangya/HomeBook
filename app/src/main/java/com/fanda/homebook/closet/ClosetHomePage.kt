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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.closet.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.ui.ClosetHomeGridWidget
import com.fanda.homebook.closet.viewmodel.AddClosetViewModel
import com.fanda.homebook.closet.viewmodel.HomeClosetViewModel
import com.fanda.homebook.components.CustomDropdownMenu
import com.fanda.homebook.components.MenuItem
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.UserCache
import com.fanda.homebook.tools.toJson

/*
*
* 衣橱页面
* */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosetHomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    closetViewModel: HomeClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    var expandUserMenu by remember { mutableStateOf(false) }
    //  记录上次的返回时间
    var lastBackPressed by remember { mutableLongStateOf(0L) }

    val curSelectOwner by closetViewModel.curSelectOwner.collectAsState()
    val closetUiState by closetViewModel.addClosetUiState.collectAsState()
    val groupedClosets by closetViewModel.groupedClosets.collectAsState()
    val trashData by closetViewModel.trashData.collectAsState()

    LogUtils.i("closets", groupedClosets)
    LogUtils.i("groupedTrashClosets", trashData)


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
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val now = System.currentTimeMillis()
                                if (now - lastBackPressed > 200 && !expandUserMenu) {
                                    expandUserMenu = true
                                }
                            }
                            .padding(start = 0.dp, end = 30.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                text = curSelectOwner?.name ?: "",
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            Image(
                                modifier = Modifier.padding(start = 6.dp),
                                painter = painterResource(id = R.mipmap.icon_arrow_down_black),
                                contentDescription = null
                            )
                        }
                        OwnerDropdownMenu(
                            owner = curSelectOwner,
                            data = closetViewModel.owners,
                            expanded = expandUserMenu,
                            dpOffset = DpOffset(0.dp, 50.dp),
                            onDismiss = {
                                lastBackPressed = System.currentTimeMillis()
                                expandUserMenu = false
                            },
                            onConfirm = {
                                expandUserMenu = false
                                closetViewModel.updateSelectedOwner(it)
                            })
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                                .size(44.dp)
                                .clickable {
                                    closetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                                }) {
                            Image(
                                painter = painterResource(id = R.mipmap.icon_add_grady),
                                contentDescription = "Action",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                                .size(44.dp)
                                .clickable { navController.navigate(RoutePath.EditCategory.route) }) {
                            Image(
                                painter = painterResource(id = R.mipmap.icon_setting),
                                contentDescription = "Action",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

            },
            colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
        )
    }) { padding ->
        ClosetHomeGridWidget(
            data = groupedClosets,
            trashData = trashData,
            modifier = Modifier.padding(padding)
        ) {
            if (it.moveToTrash) {
                // 垃圾桶数据，直接到详细列表页面，然后只查垃圾桶数据
                // 没有子分类，跳转到详细列表页面
                navController.navigate("${RoutePath.ClosetDetailCategory.route}?categoryId=${it.category.id}&subCategoryId=-1&categoryName=${it.category.name}&moveToTrash=true")
                return@ClosetHomeGridWidget
            }
            closetViewModel.hasClosetsWithSubcategory(it.category.id) { has ->
                LogUtils.d("是否存在子分类： $has")
                if (has) {
                    // 有子分类，跳转到子分类分组页面
                    navController.navigate("${RoutePath.ClosetCategory.route}?categoryEntity=${it.category.toJson()}")
                } else {
                    // 没有子分类，跳转到详细列表页面
                    navController.navigate("${RoutePath.ClosetDetailCategory.route}?categoryId=${it.category.id}&subCategoryId=-1&categoryName=${it.category.name}&moveToTrash=false")
                }
            }
        }
    }

    SelectPhotoBottomSheet(
        visible = closetUiState.sheetType == ShowBottomSheetType.SELECT_IMAGE, onDismiss = {
            closetViewModel.dismissBottomSheet()
        }) {
        closetViewModel.dismissBottomSheet()
        navController.navigate("${RoutePath.AddCloset.route}?imagePath=${it}")
    }

}

@Composable
fun OwnerDropdownMenu(
    owner: OwnerEntity?,
    data: List<OwnerEntity>,
    modifier: Modifier = Modifier,
    dpOffset: DpOffset,
    expanded: Boolean,
    onDismiss: (() -> Unit),
    onConfirm: (OwnerEntity) -> Unit
) {
    CustomDropdownMenu(
        modifier = modifier, dpOffset = dpOffset, expanded = expanded, onDismissRequest = onDismiss
    ) {
        Column {
            data.forEach {
                MenuItem(text = it.name, selected = it.id == owner?.id) {
                    onConfirm(it)
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ClosetHomePagePreview() {
    ClosetHomePage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}