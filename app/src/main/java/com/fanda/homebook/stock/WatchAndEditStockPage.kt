package com.fanda.homebook.stock

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fanda.homebook.R
import com.fanda.homebook.common.sheet.MonthBottomSheet
import com.fanda.homebook.common.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.components.CustomDropdownMenu
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.MenuCenterItem
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.LocalDataSource.stockMenuList
import com.fanda.homebook.data.period.PeriodEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.stock.StockEntity
import com.fanda.homebook.data.stock.StockMenuEntity
import com.fanda.homebook.data.stock.StockUseStatus
import com.fanda.homebook.data.stock.getStockDes
import com.fanda.homebook.data.stock.visibleExpireTime
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.sheet.ListBottomSheet
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.stock.sheet.StockCommentBottomSheet
import com.fanda.homebook.stock.state.WatchAndEditStockUiState
import com.fanda.homebook.stock.ui.StockInfoScreen
import com.fanda.homebook.stock.viewmodel.WatchAndEditStockViewModel
import com.fanda.homebook.tools.DATE_FORMAT_YMD
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster

@OptIn(ExperimentalMaterial3Api::class) @Composable fun WatchAndEditStockPage(
    modifier: Modifier = Modifier, navController: NavController, stockViewModel: WatchAndEditStockViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    var expandMenu by remember { mutableStateOf(false) }
    //  记录上次的返回时间
    var lastBackPressed by remember { mutableLongStateOf(0L) }

    // 通过 ViewModel 状态管理进行数据绑定
    val uiState by stockViewModel.uiState.collectAsState()
    val rackEntity by stockViewModel.rackEntity.collectAsState()
    val product by stockViewModel.product.collectAsState()
    val subCategory by stockViewModel.subCategory.collectAsState()
    val products by stockViewModel.products.collectAsState()
    val rackSubCategoryList by stockViewModel.rackSubCategoryList.collectAsState()
    val period by stockViewModel.period.collectAsState()


    LogUtils.d("uiState: $uiState")
    LogUtils.d("period: $period")

    // 获取焦点管理器
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

    BackHandler {
        if (uiState.isEditState) {
            stockViewModel.updateEditState(false)
            Toaster.show("已退出编辑状态")
        } else {
            navController.navigateUp()
        }
    }

    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding(), floatingActionButton = {
        AnimatedVisibility(
            visible = uiState.stockEntity.useStatus != StockUseStatus.USED.code, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()
        ) {

            FloatingActionButton(
                shape = FloatingActionButtonDefaults.largeShape, containerColor = Color.Black, contentColor = Color.White, onClick = {
                    if (uiState.stockEntity.useStatus == StockUseStatus.NO_USE.code) {
                        stockViewModel.updateSheetType(ShowBottomSheetType.OPEN_DATE, forceShow = true)
                    } else if (uiState.stockEntity.useStatus == StockUseStatus.USING.code) {
                        stockViewModel.updateSheetType(ShowBottomSheetType.USED_UP, forceShow = true)
                    }

                }, modifier = Modifier
                    .padding(5.dp)
                    .size(60.dp)
            ) {
                Text(
                    text = when (uiState.stockEntity.useStatus) {
                        StockUseStatus.NO_USE.code -> {
                            "开始\n使用"
                        }

                        StockUseStatus.USING.code -> {
                            "不用了"
                        }

                        else -> {
                            ""
                        }
                    }
                )
            }
        }
    }, topBar = {
        TopAppBar(
            navigationIcon = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController.popBackStack()
                    }) {
                    Image(
                        painter = painterResource(id = R.mipmap.icon_back), contentDescription = "Back", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Box(
                    modifier = modifier
                        .height(64.dp)
                        .fillMaxWidth()
                        .background(color = Color.Transparent)
                ) {
                    Text(
                        text = "单品信息", style = TextStyle.Default.copy(
                            fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Medium
                        ), color = Color.Black, maxLines = 1, modifier = Modifier
                            .align(Alignment.Center)
                            .padding(end = 48.dp)
                    )
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(64.dp)      // 这里要固定高度，不然 pop 显示位置异常
                        .align(Alignment.CenterEnd)
                            .clickable(
                                // 去掉默认的点击效果
                                interactionSource = remember { MutableInteractionSource() }, indication = null
                            ) {
                                if (uiState.isEditState) {
                                    stockViewModel.updateStockEntityDatabase(context) {
                                        focusManager.clearFocus()
                                        navController.navigateUp()
                                        Toaster.show("编辑成功")
                                    }
                                } else {
                                    val now = System.currentTimeMillis()
                                    if (now - lastBackPressed > 200 && !expandMenu) {
                                        expandMenu = true
                                    }
                                }
                            }
                            .padding(end = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                text = if (uiState.isEditState) "保存" else "管理", fontWeight = FontWeight.Normal, fontSize = 16.sp, color = colorResource(R.color.color_333333)
                            )
                        }
                        StockDropdownMenu(data = stockMenuList, expanded = expandMenu, dpOffset = DpOffset((-65).dp, 50.dp), onDismiss = {
                            lastBackPressed = System.currentTimeMillis()
                            expandMenu = false
                        }, onConfirm = {
                            expandMenu = false
                            when (it.type) {
                                ShowBottomSheetType.EDIT -> {
                                    stockViewModel.updateEditState(true)
                                    Toaster.show("已开启编辑状态")
                                }

                                ShowBottomSheetType.DELETE -> {
                                    stockViewModel.deleteEntityDatabase {
                                        Toaster.show("删除成功")
                                        navController.navigateUp()
                                    }
                                }

                                ShowBottomSheetType.COPY -> {
                                    stockViewModel.copyEntityDatabase {
                                        Toaster.show("复制成功")
                                    }
                                }

                                else -> {}
                            }
                        })
                    }

                }

            },
            colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
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
                    TopImageWidget(uiState) {
                        stockViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "名称",
                            showRightArrow = false,
                            showTextField = true,
                            removeIndication = true,
                            modifier = Modifier
                                .height(64.dp)
                                .padding(horizontal = 20.dp),
                            inputText = uiState.stockEntity.name,
                            isEditState = uiState.isEditState,
                            onValueChange = {
                                stockViewModel.updateStockName(it)
                            })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "货架", rightText = rackEntity?.name ?: "", showText = true, modifier = Modifier
                                .height(64.dp)
                                .padding(start = 20.dp, end = 20.dp)
                        ) {
                            focusManager.clearFocus()
                            stockViewModel.updateSheetType(ShowBottomSheetType.RACK)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    StockInfoScreen(
                        bottomComment = uiState.stockEntity.comment,
                        subCategory = subCategory?.name ?: "",
                        product = product?.name ?: "",
                        isEditState = uiState.isEditState,
                        usagePeriod = period?.name ?: "",
                        shelfMonth = uiState.stockEntity.shelfMonth,
                        date = convertMillisToDate(uiState.stockEntity.buyDate, DATE_FORMAT_YMD),
                        openDate = convertMillisToDate(uiState.stockEntity.openDate, DATE_FORMAT_YMD),
                        expireDate = convertMillisToDate(uiState.stockEntity.expireDate, DATE_FORMAT_YMD),
                        syncBook = uiState.stockEntity.syncBook,
                        showSyncBook = false,
                        price = uiState.stockEntity.price,
                        onCheckedChange = {
                            stockViewModel.updateSyncBook(it)
                            LogUtils.d("同步至当日账单： $it")
                        },
                        onBottomCommentChange = {
                            stockViewModel.updateClosetComment(it)
                        },
                        onPriceChange = {
                            stockViewModel.updatePrice(it)
                        },
                        onClick = {
                            stockViewModel.updateSheetType(it)
                        })
                }
            }
        }
    }

    ListBottomSheet<RackEntity>(
        initial = rackEntity,
        title = "货架",
        dataSource = stockViewModel.racks,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.RACK) },
        displayText = { it.name },
        onDismiss = { stockViewModel.dismissBottomSheet() }) {
        stockViewModel.updateRack(it)
    }

    if (stockViewModel.showBottomSheet(ShowBottomSheetType.BUY_DATE)) {
        // 日期选择器
        CustomDatePickerModal(initialDate = uiState.stockEntity.buyDate, onDateSelected = {
            stockViewModel.updateBuyDate(it ?: System.currentTimeMillis())
        }, onDismiss = {
            stockViewModel.dismissBottomSheet()
        })
    }

    ListBottomSheet<ProductEntity>(
        initial = product,
        title = "品牌",
        dataSource = products,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.PRODUCT) },
        displayText = { it.name },
        onDismiss = { stockViewModel.dismissBottomSheet() },
        onSettingClick = {
            stockViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditProduct.route)
        }) {
        stockViewModel.updateProduct(it)
    }

    ListBottomSheet<RackSubCategoryEntity>(
        initial = subCategory,
        title = "类别",
        dataSource = rackSubCategoryList,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.CATEGORY) },
        displayText = { it.name },
        onDismiss = { stockViewModel.dismissBottomSheet() }) {
        stockViewModel.updateCategory(it)
    }

    ListBottomSheet<PeriodEntity>(
        initial = period,
        title = "使用时段",
        dataSource = stockViewModel.periods,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.USAGE_PERIOD) },
        displayText = { it.name },
        onDismiss = { stockViewModel.dismissBottomSheet() }) {
        stockViewModel.updatePeriod(it)
    }

    if (stockViewModel.showBottomSheet(ShowBottomSheetType.OPEN_DATE)) {
        // 日期选择器
        CustomDatePickerModal(initialDate = uiState.stockEntity.openDate, onDateSelected = {
            stockViewModel.updateOpenDate(it ?: System.currentTimeMillis())
            stockViewModel.updateUseStatus(context, StockUseStatus.USING)
        }, onDismiss = {
            stockViewModel.dismissBottomSheet()
        })
    }

    if (uiState.showUsedUpDateSelectDialog) {
        // 日期选择器
        CustomDatePickerModal(initialDate = uiState.stockEntity.usedDate, onDateSelected = {
            stockViewModel.updateUsedUpDate(it ?: System.currentTimeMillis())
        }, onDismiss = {
            stockViewModel.updateUsedUpDateSelectDialog(false)
        })
    }

    if (stockViewModel.showBottomSheet(ShowBottomSheetType.EXPIRE_DATE)) {
        // 日期选择器
        CustomDatePickerModal(initialDate = uiState.stockEntity.expireDate, onDateSelected = {
            stockViewModel.updateExpireDate(it ?: System.currentTimeMillis())
        }, onDismiss = {
            stockViewModel.dismissBottomSheet()
        })
    }

    MonthBottomSheet(month = uiState.stockEntity.shelfMonth, visible = stockViewModel.showBottomSheet(ShowBottomSheetType.SHELF_MONTH), onDismiss = {
        stockViewModel.dismissBottomSheet()
    }) { month ->
        stockViewModel.dismissBottomSheet()
        stockViewModel.updateShelfMonth(month)
        LogUtils.d("选中的月: $month")
    }

    SelectPhotoBottomSheet(
        visible = stockViewModel.showBottomSheet(ShowBottomSheetType.SELECT_IMAGE), onDismiss = {
            stockViewModel.dismissBottomSheet()
        }) {
        stockViewModel.dismissBottomSheet()
        stockViewModel.updateImageUrl(it)
    }

    StockCommentBottomSheet(visible = stockViewModel.showBottomSheet(ShowBottomSheetType.USED_UP), onDismiss = {
        stockViewModel.dismissBottomSheet()
    }, remain = uiState.stockEntity.remain, date = convertMillisToDate(uiState.stockEntity.usedDate, DATE_FORMAT_YMD), feel = uiState.stockEntity.feel, onConfirm = {
        if (uiState.stockEntity.usedDate <= 0) {
            Toaster.show("请选择用完日期")
        } else if (uiState.stockEntity.remain.isEmpty()) {
            Toaster.show("请填写用完后剩余量")
        } else if (uiState.stockEntity.feel.isEmpty()) {
            Toaster.show("请填写使用感受")
        } else {
            stockViewModel.updateUseStatus(context, StockUseStatus.USED)
            stockViewModel.dismissBottomSheet()
        }
    }, onDateClick = {
        stockViewModel.updateUsedUpDateSelectDialog(true)
    }, onRemainClick = {
        stockViewModel.updateRemain(it)

    }, onFeelClick = {
        stockViewModel.updateFeel(it)
    })


}

@Composable fun TopImageWidget(uiState: WatchAndEditStockUiState, onImageClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            contentScale = ContentScale.Crop,
            model = uiState.imageUri ?: uiState.stockEntity.imageLocalPath,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable {
                    onImageClick()
                })
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
        ) {
            Text(
                text = when (uiState.stockEntity.useStatus) {
                    StockUseStatus.NO_USE.code -> {
                        "未开封"
                    }

                    StockUseStatus.USING.code -> {
                        "使用中"
                    }

                    else -> {
                        uiState.stockEntity.feel
                    }
                },
                style = TextStyle.Default.copy(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                modifier = Modifier
                    .background(color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                fontSize = 12.sp,
                color = Color.White
            )
        }
        if (uiState.stockEntity.visibleExpireTime()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            ) {
                Text(
                    text = uiState.stockEntity.getStockDes(),
                    textAlign = TextAlign.Center,
                    style = TextStyle.Default.copy(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.Black.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable fun StockDropdownMenu(
    data: List<StockMenuEntity>, modifier: Modifier = Modifier, dpOffset: DpOffset, expanded: Boolean, onDismiss: (() -> Unit), onConfirm: (StockMenuEntity) -> Unit
) {
    CustomDropdownMenu(
        modifier = modifier, dpOffset = dpOffset, expanded = expanded, onDismissRequest = onDismiss, width = 100.dp
    ) {
        Column {
            data.forEach {
                MenuCenterItem(text = it.name) {
                    onConfirm(it)
                }
            }
        }
    }
}


@Composable @Preview(showBackground = true) fun TopImageWidgetPreview() {
    HomeBookTheme {
        TopImageWidget(WatchAndEditStockUiState(stockEntity = StockEntity(imageLocalPath = "https://picsum.photos/200/300"))) {

        }
    }
}