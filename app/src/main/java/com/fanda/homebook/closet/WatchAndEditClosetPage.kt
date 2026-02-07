package com.fanda.homebook.closet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.fanda.homebook.R
import com.fanda.homebook.closet.ui.ClosetInfoScreen
import com.fanda.homebook.closet.viewmodel.WatchAndEditClosetViewModel
import com.fanda.homebook.common.sheet.CategoryExpandBottomSheet
import com.fanda.homebook.common.sheet.ColorTypeBottomSheet
import com.fanda.homebook.common.sheet.GridBottomSheet
import com.fanda.homebook.common.sheet.ListBottomSheet
import com.fanda.homebook.common.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.DATE_FORMAT_YMD
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster

/**
 * 查看与编辑衣橱页面
 *
 * 用于查看和编辑单个衣橱物品的详细信息
 */
@Composable fun WatchAndEditClosetPage(
    modifier: Modifier = Modifier, navController: NavController, closetViewModel: WatchAndEditClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 从ViewModel收集状态
    val addClosetUiState by closetViewModel.addClosetUiState.collectAsState()
    val colorTypes by closetViewModel.colorTypes.collectAsState()
    val products by closetViewModel.products.collectAsState()
    val sizes by closetViewModel.sizes.collectAsState()
    val categories by closetViewModel.categories.collectAsState()

    val colorType by closetViewModel.colorType.collectAsState()
    val selectSeasons by closetViewModel.selectSeasons.collectAsState()
    val product by closetViewModel.product.collectAsState()
    val size by closetViewModel.size.collectAsState()
    val owner by closetViewModel.owner.collectAsState()
    val category by closetViewModel.category.collectAsState()
    val subCategory by closetViewModel.subCategory.collectAsState()

    LogUtils.d("category: $category")
    LogUtils.d("subCategory: $subCategory")
    LogUtils.d("AddClosetPage: addClosetUiState: $addClosetUiState")

    // 焦点管理器（用于关闭软键盘）
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // 处理返回键：编辑模式下退出编辑，非编辑模式下返回上一页
    BackHandler {
        if (addClosetUiState.isEditState) {
            closetViewModel.updateEditState(false)
            Toaster.show("已退出编辑状态")
        } else {
            navController.navigateUp()
        }
    }

    // 使用Scaffold布局
    Scaffold(
        modifier = modifier.statusBarsPadding(), // 状态栏内边距
        floatingActionButton = {
            // 浮动操作按钮（仅在非编辑模式显示）
            AnimatedVisibility(
                visible = !addClosetUiState.isEditState, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    containerColor = Color.Black, contentColor = Color.White, onClick = {
                        // 根据当前状态切换垃圾桶状态
                        if (addClosetUiState.closetEntity.moveToTrash) {
                            closetViewModel.toggleMoveToTrash(context, false)
                            Toaster.show("从垃圾桶恢复啦")
                        } else {
                            closetViewModel.toggleMoveToTrash(context, true)
                            Toaster.show("移动到垃圾桶啦")
                        }
                    }, modifier = Modifier
                        .padding(5.dp)
                        .size(60.dp)
                ) {
                    Text(text = if (addClosetUiState.closetEntity.moveToTrash) "恢复" else "不穿了")
                }
            }
        }, topBar = {
            TopIconAppBar(
                title = "单品信息",
                onBackClick = {
                    navController.navigateUp()
                },
                rightText = if (addClosetUiState.closetEntity.moveToTrash) {
                    "" // 垃圾桶模式下不显示编辑/保存按钮
                } else if (addClosetUiState.isEditState) {
                    "保存" // 编辑模式下显示保存
                } else {
                    "编辑" // 查看模式下显示编辑
                },
                onRightActionClick = {
                    if (addClosetUiState.isEditState) {
                        // 保存编辑并返回
                        closetViewModel.updateClosetEntityDatabase(context) {
                            focusManager.clearFocus()
                            navController.navigateUp()
                            Toaster.show("编辑成功")
                        }
                    } else {
                        // 进入编辑模式
                        closetViewModel.updateEditState(true)
                        Toaster.show("已开启编辑状态")
                    }
                },
                backIconPainter = painterResource(R.mipmap.icon_back),
            )
        }) { padding ->
        // 全屏点击区域（用于关闭软键盘）
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() }, onDoubleTap = { focusManager.clearFocus() }, onLongPress = { focusManager.clearFocus() })
            }
            .background(Color.Transparent)) {
            // 主要内容区域（可滚动）
            Column(
                modifier = Modifier
                    .padding(padding)
                    .imePadding() // 处理输入法遮挡
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    // 衣橱图片
                    AsyncImage(
                        contentScale = ContentScale.Crop,
                        model = addClosetUiState.imageUri ?: addClosetUiState.closetEntity.imageLocalPath,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f) // 1:1比例
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .clickable {
                                // 编辑模式下可点击更换图片
                                closetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                            })

                    Spacer(modifier = Modifier.height(20.dp))

                    // 归属信息
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "归属", rightText = owner?.name ?: "", showText = true, modifier = Modifier
                                .height(64.dp)
                                .padding(start = 20.dp, end = 20.dp)
                        ) {
                            focusManager.clearFocus()
                            closetViewModel.updateSheetType(ShowBottomSheetType.OWNER)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 穿着次数和成本
                    WearCountAndCost(
                        price = addClosetUiState.closetEntity.price, wearCount = addClosetUiState.closetEntity.wearCount
                    ) {
                        focusManager.clearFocus()
                        closetViewModel.plusClosetWearCount(context)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 衣橱详细信息表单
                    ClosetInfoScreen(
                        bottomComment = addClosetUiState.closetEntity.comment,
                        closetCategory = category?.name ?: "",
                        closetSubCategory = subCategory?.name ?: "",
                        product = product?.name ?: "",
                        color = colorType?.color ?: -1,
                        season = closetViewModel.getSeasonDes(selectSeasons),
                        date = convertMillisToDate(addClosetUiState.closetEntity.date, DATE_FORMAT_YMD),
                        syncBook = addClosetUiState.closetEntity.syncBook,
                        showSyncBook = false, // 查看/编辑页面不显示同步到账单
                        size = size?.name ?: "",
                        isEditState = addClosetUiState.isEditState,
                        price = addClosetUiState.closetEntity.price,
                        onCheckedChange = {
                            closetViewModel.updateClosetSyncBook(it)
                            LogUtils.d("同步至当日账单： $it")
                        },
                        onBottomCommentChange = {
                            closetViewModel.updateClosetComment(it)
                        },
                        onPriceChange = {
                            closetViewModel.updateClosetPrice(it)
                        },
                        onClick = {
                            closetViewModel.updateSheetType(it)
                        })
                }
            }
        }
    }

    // 各种底部弹窗组件（根据状态显示）

    // 购买日期选择器
    if (closetViewModel.showBottomSheet(ShowBottomSheetType.BUY_DATE)) {
        CustomDatePickerModal(initialDate = addClosetUiState.closetEntity.date, onDateSelected = {
            closetViewModel.updateClosetDate(it ?: System.currentTimeMillis())
        }, onDismiss = {
            closetViewModel.dismissBottomSheet()
        })
    }

    // 品牌选择弹窗
    ListBottomSheet<ProductEntity>(
        initial = product,
        title = "品牌",
        dataSource = products,
        visible = { closetViewModel.showBottomSheet(ShowBottomSheetType.PRODUCT) },
        displayText = { it.name },
        onDismiss = { closetViewModel.dismissBottomSheet() },
        onSettingClick = {
            closetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditProduct.route)
        }) {
        closetViewModel.updateClosetProduct(it)
    }

    // 归属选择弹窗
    ListBottomSheet<OwnerEntity>(
        initial = owner,
        title = "归属",
        dataSource = closetViewModel.owners,
        visible = { closetViewModel.showBottomSheet(ShowBottomSheetType.OWNER) },
        displayText = { it.name },
        onDismiss = { closetViewModel.dismissBottomSheet() }) {
        closetViewModel.updateClosetOwner(it)
    }

    // 尺码选择弹窗（网格布局）
    GridBottomSheet<SizeEntity>(
        initial = size,
        title = "尺码",
        dataSource = sizes,
        visible = { closetViewModel.showBottomSheet(ShowBottomSheetType.SIZE) },
        displayText = { it.name },
        dpSize = DpSize(52.dp, 36.dp),
        column = GridCells.Fixed(5),
        onDismiss = { closetViewModel.dismissBottomSheet() },
        onSettingClick = {
            closetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditSize.route)
        }) {
        closetViewModel.updateClosetSize(it)
    }

    // 季节选择弹窗（网格布局）
    GridBottomSheet<SeasonEntity>(
        initial = selectSeasons,
        title = "季节",
        dataSource = closetViewModel.seasons,
        visible = { closetViewModel.showBottomSheet(ShowBottomSheetType.SEASON) },
        displayText = { it.name },
        dpSize = DpSize(66.dp, 36.dp),
        column = GridCells.Fixed(4),
        onDismiss = { closetViewModel.dismissBottomSheet() }) {
        closetViewModel.updateClosetSeason(it)
    }

    // 颜色选择弹窗
    ColorTypeBottomSheet(color = colorType, colorList = colorTypes, visible = { closetViewModel.showBottomSheet(ShowBottomSheetType.COLOR) }, onDismiss = {
        closetViewModel.dismissBottomSheet()
    }, onConfirm = {
        closetViewModel.updateClosetColor(it)
    }, onSettingClick = {
        closetViewModel.dismissBottomSheet()
        navController.navigate(RoutePath.EditColor.route)
    })

    // 分类选择弹窗（可展开）
    CategoryExpandBottomSheet(
        categories = categories,
        categoryEntity = category,
        subCategoryEntity = subCategory,
        visible = { closetViewModel.showBottomSheet(ShowBottomSheetType.CATEGORY) },
        onDismiss = {
            closetViewModel.dismissBottomSheet()
        },
        onSettingClick = {
            closetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditCategory.route)
        },
        onConfirm = { category, subCategory ->
            LogUtils.i("选中的分类： $category, $subCategory")
            closetViewModel.updateSelectedCategory(category, subCategory)
        })

    // 图片选择弹窗
    SelectPhotoBottomSheet(
        visible = closetViewModel.showBottomSheet(ShowBottomSheetType.SELECT_IMAGE), onDismiss = {
            closetViewModel.dismissBottomSheet()
        }) {
        closetViewModel.dismissBottomSheet()
        closetViewModel.updateImageUrl(it)
    }
}

/**
 * 穿着次数和成本显示组件
 *
 * @param price 购买价格
 * @param wearCount 穿着次数
 * @param modifier 修饰符
 * @param onPlusClick 增加穿着次数回调
 */
@Composable fun WearCountAndCost(
    price: String, wearCount: Int, modifier: Modifier = Modifier, onPlusClick: (() -> Unit)
) {
    val itemPadding = Modifier.padding(
        20.dp, 20.dp, 20.dp, 20.dp
    )

    // 计算穿着成本（价格/穿着次数）
    val showPrice = if (price.isEmpty()) {
        ""
    } else {
        "${String.format("%.1f", price.toFloat() / wearCount)}元/次"
    }

    GradientRoundedBoxWithStroke(modifier = modifier) {
        Column {
            // 穿着次数（带+1按钮）
            ItemOptionMenu(
                title = "穿着次数：${wearCount}次",
                showText = true,
                showRightArrow = false,
                rightText = "",
                showPlus = true,
                showDivider = true,
                modifier = itemPadding,
                onPlusClick = onPlusClick,
                removeIndication = true
            )

            // 穿着成本（只读显示）
            ItemOptionMenu(
                title = "穿着成本", showText = true, rightText = showPrice, showDivider = false, showRightArrow = false, modifier = itemPadding, removeIndication = true
            )
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览查看与编辑衣橱页面
 */
@Composable @Preview(showBackground = true) fun WatchAndEditClosetPagePreview() {
    HomeBookTheme {
        WatchAndEditClosetPage(
            modifier = Modifier.fillMaxWidth(), navController = rememberNavController()
        )
    }
}