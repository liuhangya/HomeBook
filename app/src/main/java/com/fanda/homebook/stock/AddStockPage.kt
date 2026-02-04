package com.fanda.homebook.stock

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
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
import com.fanda.homebook.book.sheet.MonthBottomSheet
import com.fanda.homebook.book.sheet.YearMonthBottomSheet
import com.fanda.homebook.closet.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.period.PeriodEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.GridBottomSheet
import com.fanda.homebook.quick.sheet.ListBottomSheet
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.stock.ui.StockInfoScreen
import com.fanda.homebook.stock.viewmodel.AddStockViewModel
import com.fanda.homebook.tools.DATE_FORMAT_YMD
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster


/*
* 添加囤货页面
* */
@Composable fun AddStockPage(modifier: Modifier = Modifier, navController: NavController, stockViewModel: AddStockViewModel = viewModel(factory = AppViewModelProvider.factory)) {
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

    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "单品信息",
            onBackClick = {
                navController.navigateUp()
            },
            rightText = "保存",
            onRightActionClick = {
                focusManager.clearFocus()

                // 先校验囤货的参数
                if (stockViewModel.checkParams()) {
                    // 再根据是否选中同步到账单
                    if (uiState.stockEntity.syncBook) {
                        if (stockViewModel.checkBookParams()) {
                            // 先插入账单数据
                            stockViewModel.saveQuickEntityDatabase()
                        } else {
                            return@TopIconAppBar
                        }
                    }
                    // 插入囤货数据
                    stockViewModel.saveStockEntityDatabase(context) {
                        Toaster.show("保存成功")
                        navController.navigateUp()
                    }

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
                        model = uiState.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .clickable {
                                stockViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                            }
                    )

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
                        usagePeriod = period?.name ?: "",
                        shelfMonth = uiState.stockEntity.shelfMonth,
                        date = convertMillisToDate(uiState.stockEntity.buyDate, DATE_FORMAT_YMD),
                        openDate =  convertMillisToDate(uiState.stockEntity.openDate, DATE_FORMAT_YMD),
                        expireDate = convertMillisToDate(uiState.stockEntity.expireDate, DATE_FORMAT_YMD),
                        syncBook = uiState.stockEntity.syncBook,
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
        CustomDatePickerModal(onDateSelected = {
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
        CustomDatePickerModal(onDateSelected = {
            stockViewModel.updateOpenDate(it ?: System.currentTimeMillis())
        }, onDismiss = {
            stockViewModel.dismissBottomSheet()
        })
    }

    if (stockViewModel.showBottomSheet(ShowBottomSheetType.EXPIRE_DATE)) {
        // 日期选择器
        CustomDatePickerModal(onDateSelected = {
            stockViewModel.updateExpireDate(it ?: System.currentTimeMillis())
        }, onDismiss = {
            stockViewModel.dismissBottomSheet()
        })
    }

    MonthBottomSheet( month = uiState.stockEntity.shelfMonth, visible =  stockViewModel.showBottomSheet(ShowBottomSheetType.SHELF_MONTH), onDismiss = {
        stockViewModel.dismissBottomSheet()
    }) {  month ->
        stockViewModel.dismissBottomSheet()
        stockViewModel.updateShelfMonth(month)
        LogUtils.d("选中的月: $month")
    }

    SelectPhotoBottomSheet(
        visible = stockViewModel.showBottomSheet(ShowBottomSheetType.SELECT_IMAGE),
        onDismiss = {
            stockViewModel.dismissBottomSheet()
        }) {
        stockViewModel.dismissBottomSheet()
        stockViewModel.updateImageUrl(it)
    }

}


@Composable @Preview(showBackground = true) fun AddStockPagePreview() {
    HomeBookTheme {
        AddStockPage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}