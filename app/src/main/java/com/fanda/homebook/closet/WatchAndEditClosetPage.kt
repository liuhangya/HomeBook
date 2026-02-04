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
import com.fanda.homebook.closet.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.ui.ClosetInfoScreen
import com.fanda.homebook.closet.viewmodel.WatchAndEditClosetViewModel
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.CategoryBottomSheet
import com.fanda.homebook.quick.sheet.ColorTypeBottomSheet
import com.fanda.homebook.quick.sheet.GridBottomSheet
import com.fanda.homebook.quick.sheet.ListBottomSheet
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.DATE_FORMAT_YMD
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster

@Composable fun WatchAndEditClosetPage(
    modifier: Modifier = Modifier, navController: NavController, closetViewModel: WatchAndEditClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 通过 ViewModel 状态管理进行数据绑定
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

    // 获取焦点管理器
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current


    BackHandler {
        if (addClosetUiState.isEditState) {
            closetViewModel.updateEditState(false)
            Toaster.show("已退出编辑状态")
        } else {
            navController.navigateUp()
        }
    }


    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding(), floatingActionButton = {
        AnimatedVisibility(
            visible = !addClosetUiState.isEditState, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                containerColor = Color.Black, contentColor = Color.White, onClick = {
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
                ""
            } else if (addClosetUiState.isEditState) {
                "保存"
            } else {
                "编辑"
            },
            onRightActionClick = {
                if (addClosetUiState.isEditState) {
                    closetViewModel.updateClosetEntityDatabase(context) {
                        focusManager.clearFocus()
                        navController.navigateUp()
                        Toaster.show("编辑成功")
                    }
                } else {
                    closetViewModel.updateEditState(true)
                    Toaster.show("已开启编辑状态")
                }
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )
    }) { padding ->

        // 创建一个覆盖整个屏幕的可点击区域（放在最外层）
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {// 给最外层添加事件，用于取消输入框的焦点，从而关闭输入法
                detectTapGestures(onTap = { focusManager.clearFocus() }, onDoubleTap = { focusManager.clearFocus() }, onLongPress = { focusManager.clearFocus() })
            }
            .background(Color.Transparent) // 必须有背景或 clickable 才能响应事件
        ) {
            // 为了让 padding 内容能滑动，所以用 Column 包起来
            Column(
                modifier = Modifier
                    .padding(padding)
                    .imePadding()   // 让输入法能顶起内容，不遮挡内容
                    .verticalScroll(rememberScrollState())  // 让内容能滑动，内容的 padding 不能加在这里，不然 padding 部分不能滑过去
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    AsyncImage(
                        contentScale = ContentScale.Crop,
                        model = addClosetUiState.imageUri ?: addClosetUiState.closetEntity.imageLocalPath,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .clickable {
                                closetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                            })

                    Spacer(modifier = Modifier.height(20.dp))
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
                    WearCountAndCost(addClosetUiState.closetEntity.price, addClosetUiState.closetEntity.wearCount) {
                        focusManager.clearFocus()
                        closetViewModel.plusClosetWearCount(context)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ClosetInfoScreen(
                        bottomComment = addClosetUiState.closetEntity.comment,
                        closetCategory = category?.name ?: "",
                        closetSubCategory = subCategory?.name ?: "",
                        product = product?.name ?: "",
                        color = colorType?.color ?: -1,
                        season = closetViewModel.getSeasonDes(selectSeasons),
                        date = convertMillisToDate(addClosetUiState.closetEntity.date, DATE_FORMAT_YMD),
                        syncBook = addClosetUiState.closetEntity.syncBook,
                        showSyncBook = false,
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

    if (closetViewModel.showBottomSheet(ShowBottomSheetType.BUY_DATE)) {
        // 日期选择器
        CustomDatePickerModal(onDateSelected = {
            closetViewModel.updateClosetDate(it ?: System.currentTimeMillis())
        }, onDismiss = {
            closetViewModel.dismissBottomSheet()
        })
    }

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

    ListBottomSheet<OwnerEntity>(
        initial = owner,
        title = "归属",
        dataSource = closetViewModel.owners,
        visible = { closetViewModel.showBottomSheet(ShowBottomSheetType.OWNER) },
        displayText = { it.name },
        onDismiss = { closetViewModel.dismissBottomSheet() }) {
        closetViewModel.updateClosetOwner(it)
    }

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


    ColorTypeBottomSheet(color = colorType, colorList = colorTypes, visible = { closetViewModel.showBottomSheet(ShowBottomSheetType.COLOR) }, onDismiss = {
        closetViewModel.dismissBottomSheet()
    }, onConfirm = {
        closetViewModel.updateClosetColor(it)
    }, onSettingClick = {
        closetViewModel.dismissBottomSheet()
        navController.navigate(RoutePath.EditColor.route)
    })

    CategoryBottomSheet(categories = categories, categoryEntity = category, subCategoryEntity = subCategory, visible = { closetViewModel.showBottomSheet(ShowBottomSheetType.CATEGORY) }, onDismiss = {
        closetViewModel.dismissBottomSheet()
    }, onSettingClick = {
        closetViewModel.dismissBottomSheet()
        navController.navigate(RoutePath.EditCategory.route)
    }, onConfirm = { category, subCategory ->
        LogUtils.i("选中的分类： $category, $subCategory")
        closetViewModel.updateSelectedCategory(category, subCategory)
    })

    SelectPhotoBottomSheet(
        visible = closetViewModel.showBottomSheet(ShowBottomSheetType.SELECT_IMAGE), onDismiss = {
            closetViewModel.dismissBottomSheet()
        }) {
        closetViewModel.dismissBottomSheet()
        closetViewModel.updateImageUrl(it)
    }


}

@Composable fun WearCountAndCost(
    price: String, wearCount: Int, modifier: Modifier = Modifier, onPlusClick: (() -> Unit)
) {
    val itemPadding = Modifier.padding(
        20.dp, 20.dp, 20.dp, 20.dp
    )
    val showPrice = if (price.isEmpty()) {
        ""
    } else {
        "${String.format("%.1f", price.toFloat() / wearCount)}元/次"
    }
    GradientRoundedBoxWithStroke(modifier = modifier) {
        Column {
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

            ItemOptionMenu(
                title = "穿着成本", showText = true, rightText = showPrice, showDivider = false, showRightArrow = false, modifier = itemPadding, removeIndication = true
            )
        }
    }
}


@Composable @Preview(showBackground = true) fun WatchAndEditClosetPagePreview() {
    HomeBookTheme {
        WatchAndEditClosetPage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}