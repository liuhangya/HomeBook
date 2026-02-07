package com.fanda.homebook.stock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.common.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.components.CustomDropdownMenu
import com.fanda.homebook.components.MenuItem
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.stock.StockStatusEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.stock.ui.StockGridWidget
import com.fanda.homebook.stock.viewmodel.StockHomeViewModel
import com.fanda.homebook.tools.LogUtils

/**
 * 囤货首页页面
 * 显示库存物品列表，支持按货架、分类、使用状态筛选
 *
 * @param modifier Compose修饰符，用于调整布局样式
 * @param navController 导航控制器，用于页面跳转
 * @param stockHomeViewModel 库存首页的ViewModel，提供业务逻辑和数据绑定
 */
@OptIn(ExperimentalMaterial3Api::class) @Composable fun StockHomePage(
    modifier: Modifier = Modifier, navController: NavController, stockHomeViewModel: StockHomeViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 控制货架下拉菜单的展开状态
    var expandUserMenu by remember { mutableStateOf(false) }
    // 记录上次的返回按钮点击时间（用于防双击）
    var lastBackPressed by remember { mutableLongStateOf(0L) }

    // 通过ViewModel获取各种状态
    val uiState by stockHomeViewModel.uiState.collectAsState()
    val rackUiState by stockHomeViewModel.rackUiState.collectAsState()
    val curSelectRack by stockHomeViewModel.curSelectRack.collectAsState()
    val rackSubCategoryList by stockHomeViewModel.rackSubCategoryList.collectAsState()
    val stocks by stockHomeViewModel.stocks.collectAsState()
    val stockStatusCounts by stockHomeViewModel.stockStatusCounts.collectAsState()

    LogUtils.i("stockStatusCounts: $stockStatusCounts")
    LogUtils.i("uiState: $uiState")

    // 使用Scaffold作为页面骨架，包含顶部栏和内容区域
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
                        // 左侧：货架选择区域（点击展开下拉菜单）
                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .height(64.dp)  // 固定高度，避免pop显示位置异常
                            .align(Alignment.CenterStart)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() }, indication = null  // 去掉默认的点击效果
                                ) {
                                    val now = System.currentTimeMillis()
                                    // 防双击，200ms内只响应一次点击
                                    if (now - lastBackPressed > 200 && !expandUserMenu) {
                                        expandUserMenu = true
                                    }
                                }
                                .padding(start = 0.dp, end = 30.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()
                            ) {
                                Text(
                                    text = curSelectRack?.name ?: "", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                                )
                                Image(
                                    modifier = Modifier.padding(start = 6.dp), painter = painterResource(id = R.mipmap.icon_arrow_down_black), contentDescription = null
                                )
                            }
                            // 货架下拉菜单
                            RackDropdownMenu(curMenu = curSelectRack, data = stockHomeViewModel.racks, expanded = expandUserMenu, dpOffset = DpOffset(0.dp, 50.dp), onDismiss = {
                                lastBackPressed = System.currentTimeMillis()
                                expandUserMenu = false
                            }, onConfirm = {
                                expandUserMenu = false
                                stockHomeViewModel.updateSelectedRack(it)
                            })
                        }

                        // 右侧：添加按钮区域
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center, modifier = Modifier
                                    .size(44.dp)
                                    .clickable {
                                        // 点击添加按钮打开图片选择弹窗
                                        stockHomeViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                                    }) {
                                Image(
                                    painter = painterResource(id = R.mipmap.icon_add_grady), contentDescription = "添加", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
            )
        }) { padding ->
        // 内容区域
        Column(modifier = Modifier.padding(padding)) {
            // 状态筛选菜单（全部/使用中/未开封/已用完）
            StateMenu(
                data = rackUiState.statusList, curMenuEntity = uiState.curSelectUseStatus
            ) {
                stockHomeViewModel.updateSelectedUseStatus(it)
            }

            // 分类标签菜单（子分类筛选）
            LabelMenu(
                list = rackSubCategoryList, selectedRackCategory = uiState.curSelectRackSubCategory
            ) {
                stockHomeViewModel.updateSelectedSubRackCategory(it)
                LogUtils.i("点击了标签 $it")
            }

            // 库存物品网格列表
            StockGridWidget(data = stocks) {
                LogUtils.i("点击了囤货： $it")
                // 跳转到查看/编辑页面，传递物品ID参数
                navController.navigate("${RoutePath.WatchAndEditStock.route}?stockId=${it.stock.id}")
            }
        }
    }

    // 图片选择底部弹窗（用于添加新物品时选择图片）
    SelectPhotoBottomSheet(
        visible = uiState.sheetType == ShowBottomSheetType.SELECT_IMAGE, onDismiss = {
            stockHomeViewModel.dismissBottomSheet()
        }) {
        stockHomeViewModel.dismissBottomSheet()
        // 选择图片后跳转到添加页面，传递图片路径参数
        navController.navigate("${RoutePath.AddStock.route}?imagePath=${it}")
    }
}

/**
 * 状态筛选菜单组件
 * 显示库存物品的使用状态筛选选项（全部/使用中/未开封/已用完）
 *
 * @param modifier Compose修饰符
 * @param data 状态数据列表
 * @param curMenuEntity 当前选中的状态实体
 * @param onMenuChange 状态变化回调函数
 */
@Composable fun StateMenu(
    modifier: Modifier = Modifier, data: List<StockStatusEntity>, curMenuEntity: StockStatusEntity, onMenuChange: (StockStatusEntity) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        data.forEach { statusEntity ->
            Text(
                text = "${statusEntity.name}(${statusEntity.count})",
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) {
                        onMenuChange(statusEntity)
                    },
                textAlign = TextAlign.Center,
                color = if (statusEntity.name == curMenuEntity.name) Color.Black else colorResource(id = R.color.color_83878C),
                fontWeight = if (statusEntity.name == curMenuEntity.name) FontWeight.Medium else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 分类标签菜单组件
 * 显示当前选中货架的子分类标签，用于筛选特定子分类的物品
 *
 * @param modifier Compose修饰符
 * @param list 子分类数据列表
 * @param selectedRackCategory 当前选中的子分类实体
 * @param onLabelChange 标签变化回调函数
 */
@Composable fun LabelMenu(
    modifier: Modifier = Modifier, list: List<RackSubCategoryEntity>, selectedRackCategory: RackSubCategoryEntity?, onLabelChange: (RackSubCategoryEntity) -> Unit
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(list) { subCategory ->
            SelectableRoundedButton(
                text = subCategory.name, selected = subCategory.id == selectedRackCategory?.id, onClick = {
                    onLabelChange(subCategory)
                }, modifier = Modifier.animateItem()  // 添加Item加载动画
            )
        }
    }
}

/**
 * 货架下拉菜单组件
 * 显示所有可选的货架列表，供用户切换当前查看的货架
 *
 * @param curMenu 当前选中的货架实体
 * @param data 货架数据列表
 * @param modifier Compose修饰符
 * @param dpOffset 下拉菜单位移偏移量
 * @param expanded 是否展开下拉菜单
 * @param onDismiss 下拉菜单关闭回调
 * @param onConfirm 货架选择确认回调
 */
@Composable fun RackDropdownMenu(
    curMenu: RackEntity?, data: List<RackEntity>, modifier: Modifier = Modifier, dpOffset: DpOffset, expanded: Boolean, onDismiss: (() -> Unit), onConfirm: (RackEntity) -> Unit
) {
    CustomDropdownMenu(
        modifier = modifier, dpOffset = dpOffset, expanded = expanded, onDismissRequest = onDismiss
    ) {
        Column {
            data.forEach { rack ->
                MenuItem(
                    text = rack.name, selected = rack.id == curMenu?.id
                ) {
                    onConfirm(rack)
                }
            }
        }
    }
}

/**
 * 预览函数 - 用于Android Studio的Compose预览
 *
 * @see StockHomePage 查看完整参数说明
 */
@Composable @Preview(showBackground = true) fun ClosetHomePagePreview() {
    StockHomePage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}