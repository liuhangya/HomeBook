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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.util.TableInfo
import com.fanda.homebook.R
import com.fanda.homebook.common.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.ui.ClosetHomeGridWidget
import com.fanda.homebook.closet.viewmodel.HomeClosetViewModel
import com.fanda.homebook.components.CustomDropdownMenu
import com.fanda.homebook.components.MenuItem
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.toJson

/**
 * 衣橱首页
 *
 * 衣橱功能的入口页面，显示所有分类的网格视图
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosetHomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    closetViewModel: HomeClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
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
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    // 处理归属选择点击（防误触）
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
                                // 当前归属名称
                                Text(
                                    text = curSelectOwner?.name ?: "",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                                // 下拉箭头
                                Image(
                                    modifier = Modifier.padding(start = 6.dp),
                                    painter = painterResource(id = R.mipmap.icon_arrow_down_black),
                                    contentDescription = "下拉选择归属"
                                )
                            }
                            // 归属下拉菜单
                            OwnerDropdownMenu(
                                owner = curSelectOwner,
                                data = closetViewModel.owners,
                                expanded = expandUserMenu,
                                dpOffset = DpOffset(0.dp, 50.dp), // 菜单偏移位置
                                onDismiss = {
                                    lastBackPressed = System.currentTimeMillis()
                                    expandUserMenu = false
                                },
                                onConfirm = {
                                    expandUserMenu = false
                                    closetViewModel.updateSelectedOwner(it)
                                })
                        }

                        // 右侧：添加和管理按钮
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            // 添加按钮（拍照/选择图片）
                            Box(
                                contentAlignment = Alignment.Center, modifier = Modifier
                                    .size(44.dp)
                                    .clickable {
                                        closetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                                    }) {
                                Image(
                                    painter = painterResource(id = R.mipmap.icon_add_grady),
                                    contentDescription = "添加衣橱物品",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(24.dp)
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
                                    painter = painterResource(id = R.mipmap.icon_setting),
                                    contentDescription = "分类管理",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(containerColor = Color.Transparent),
            )
        }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            CategorySelectorWidget(
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            // 衣橱分类网格视图
            ClosetHomeGridWidget(
                data = groupedClosets, trashData = trashData,
            ) { item ->
                // 网格项点击事件
                if (item.moveToTrash) {
                    // 垃圾桶项：直接跳转到垃圾桶详情页面
                    navController.navigate("${RoutePath.ClosetDetailCategory.route}?categoryId=${item.category.id}&subCategoryId=-1&categoryName=${item.category.name}&moveToTrash=true")
                    return@ClosetHomeGridWidget
                }

                LogUtils.d("点击了： ${item.category}")
                // 检查该分类是否有子分类
                closetViewModel.hasClosetsWithSubcategory(item.category.id) { hasSubcategory ->
                    LogUtils.d("是否存在子分类： $hasSubcategory")
//                if (hasSubcategory) {
                    // 有子分类：跳转到子分类分组页面
//                    navController.navigate("${RoutePath.ClosetCategory.route}?categoryEntity=${item.category.toJson()}")
//                } else {
                    // 无子分类：跳转到详细列表页面
                    navController.navigate("${RoutePath.ClosetDetailCategory.route}?categoryId=${item.category.id}&subCategoryId=-1&categoryName=${item.category.name}&moveToTrash=false")
//                }
                }
            }
        }
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
fun CategorySelectorWidget(modifier: Modifier = Modifier) {

    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(
            modifier
                .weight(1f)
                .padding(vertical = 12.dp).background(Color.Blue),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "排序筛选",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = colorResource(R.color.color_84878C)
            )
            Icon(
                painter = painterResource(id = R.mipmap.icon_arrow_down_black),
                contentDescription = "下拉选择归属",
                tint = colorResource(R.color.color_84878C),
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Row(
            modifier
                .weight(1f)
                .padding(vertical = 12.dp).background(Color.Blue),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "分类筛选",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = colorResource(R.color.color_84878C)
            )
            Icon(
                painter = painterResource(id = R.mipmap.icon_arrow_down_black),
                contentDescription = "下拉选择归属",
                tint = colorResource(R.color.color_84878C),
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Row(
            modifier
                .weight(1f)
                .padding(vertical = 12.dp).background(Color.Blue),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "信息筛选",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = colorResource(R.color.color_84878C)
            )
            Icon(
                painter = painterResource(id = R.mipmap.icon_arrow_down_black),
                contentDescription = "下拉选择归属",
                tint = colorResource(R.color.color_84878C),
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}


/**
 * 归属选择下拉菜单组件
 *
 * @param owner 当前选中的归属
 * @param data 归属列表
 * @param modifier 修饰符
 * @param dpOffset 菜单偏移量
 * @param expanded 是否展开
 * @param onDismiss 关闭回调
 * @param onConfirm 选择确认回调
 */
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
            data.forEach { item ->
                MenuItem(
                    text = item.name, selected = item.id == owner?.id // 标记当前选中的归属
                ) {
                    onConfirm(item)
                }
            }
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览衣橱首页
 */
@Composable
@Preview(showBackground = true)
fun ClosetHomePagePreview() {
    ClosetHomePage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}

@Composable
@Preview(showBackground = true)
fun CategorySelectorWidgetPreview() {
    CategorySelectorWidget()
}