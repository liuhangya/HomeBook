package com.fanda.homebook.quick

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.closet.viewmodel.AddClosetViewModel
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.period.PeriodEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.CategoryBottomSheet
import com.fanda.homebook.quick.sheet.ColorTypeBottomSheet
import com.fanda.homebook.quick.sheet.GridBottomSheet
import com.fanda.homebook.quick.sheet.ListBottomSheet
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.quick.ui.EditAmountField
import com.fanda.homebook.quick.ui.EditQuickClosetScreen
import com.fanda.homebook.quick.ui.EditQuickStockScreen
import com.fanda.homebook.quick.ui.SelectCategoryGrid
import com.fanda.homebook.quick.ui.TopTypeSelector
import com.fanda.homebook.quick.viewmodel.AddQuickViewModel
import com.fanda.homebook.quick.viewmodel.WatchAndEditQuickViewModel
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.stock.viewmodel.AddStockViewModel
import com.fanda.homebook.tools.DATE_FORMAT_MD
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/*
* 记一笔页面
* */
@Composable fun WatchAndEditQuickPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    quickViewModel: WatchAndEditQuickViewModel = viewModel(factory = AppViewModelProvider.factory),
    addClosetViewModel: AddClosetViewModel = viewModel(factory = AppViewModelProvider.factory),
    stockViewModel: AddStockViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    val uiState by quickViewModel.uiState.collectAsState()
    val categories by quickViewModel.categories.collectAsState()
    val subCategories by quickViewModel.subCategories.collectAsState()
    val category by quickViewModel.category.collectAsState()
    val subCategory by quickViewModel.subCategory.collectAsState()
    val payWay by quickViewModel.payWay.collectAsState()
    val payWays by quickViewModel.payWays.collectAsState()

    // 衣橱数据
    val addClosetUiState by addClosetViewModel.addClosetUiState.collectAsState()
    val colorTypes by addClosetViewModel.colorTypes.collectAsState()
    val products by addClosetViewModel.products.collectAsState()
    val sizes by addClosetViewModel.sizes.collectAsState()
    val closetCategories by addClosetViewModel.categories.collectAsState()

    val colorType by addClosetViewModel.colorType.collectAsState()
    val selectSeasons by addClosetViewModel.selectSeasons.collectAsState()
    val product by addClosetViewModel.product.collectAsState()
    val size by addClosetViewModel.size.collectAsState()
    val owner by addClosetViewModel.owner.collectAsState()
    val closetCategory by addClosetViewModel.category.collectAsState()
    val closetSubCategory by addClosetViewModel.subCategory.collectAsState()

    // 囤货数据
    val addStockUiState by stockViewModel.uiState.collectAsState()
    val rackEntity by stockViewModel.rackEntity.collectAsState()
    val stockProduct by stockViewModel.product.collectAsState()
    val stockSubCategory by stockViewModel.subCategory.collectAsState()
    val rackSubCategoryList by stockViewModel.rackSubCategoryList.collectAsState()
    val period by stockViewModel.period.collectAsState()

    LogUtils.d("uiState: $uiState")
    LogUtils.d("addClosetUiState: $addClosetUiState")
    LogUtils.d("addStockUiState: $addStockUiState")


    // 获取焦点管理器
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()

    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "记一笔",
            onBackClick = {
                navController.navigateUp()
            },
            rightText = "保存",
            onRightActionClick = {
                focusManager.clearFocus()
                // 先校验记一笔的参数
                if (quickViewModel.checkParams()) {
                    // 再根据是否选中同步衣橱或囤货，再单独判断参数
                    // 是否同步到衣橱
                    if (quickViewModel.uiState.value.syncCloset) {
                        addClosetViewModel.updateClosetPrice(uiState.quickEntity.price)
                        if (addClosetViewModel.checkParams()) {
                            addClosetViewModel.saveClosetEntityDatabase(context) {}
                        } else {
                            return@TopIconAppBar
                        }
                    }

                    if (quickViewModel.uiState.value.syncStock) {
                        stockViewModel.updatePrice(uiState.quickEntity.price)
                        if (stockViewModel.checkParams()) {
                            stockViewModel.saveStockEntityDatabase(context) {}
                        } else {
                            return@TopIconAppBar
                        }
                    }

                    quickViewModel.saveQuickEntityDatabase {
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
                    .verticalScroll(scrollState)  // 让内容能滑动，内容的 padding 不能加在这里，不然 padding 部分不能滑过去
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    TopTypeSelector(data = categories, transactionType = category, date = convertMillisToDate(uiState.quickEntity.date, DATE_FORMAT_MD), onDateClick = {
                        quickViewModel.updateSheetType(ShowBottomSheetType.DATE)
                    }, onTypeChange = {
                        quickViewModel.updateCategory(it)
                    })
                    Spacer(modifier = Modifier.height(20.dp))
                    EditAmountField(price = uiState.quickEntity.price) {
                        quickViewModel.updatePrice(it)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SelectCategoryGrid(initial = subCategory, items = subCategories) {
                        quickViewModel.updateSubCategory(it)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "备注",
                            showRightArrow = false,
                            showTextField = true,
                            removeIndication = true,
                            modifier = Modifier
                                .height(64.dp)
                                .padding(horizontal = 20.dp),
                            inputText = uiState.quickEntity.quickComment,
                            onValueChange = {
                                quickViewModel.updateQuickComment(it)
                            })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "付款方式", rightText = payWay?.name ?: "", showText = true, modifier = Modifier
                                .height(64.dp)
                                .padding(start = 20.dp, end = 20.dp)
                        ) {
                            focusManager.clearFocus()
                            quickViewModel.updateSheetType(ShowBottomSheetType.PAY_WAY)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    EditQuickClosetScreen(
                        showSyncCloset = uiState.syncCloset,
                        bottomComment = addClosetUiState.closetEntity.comment,
                        closetCategory = closetCategory?.name ?: "",
                        closetSubCategory = closetSubCategory?.name ?: "",
                        product = product?.name ?: "",
                        color = colorType?.color ?: -1,
                        season = addClosetViewModel.getSeasonDes(selectSeasons),
                        owner = owner?.name ?: "",
                        size = size?.name ?: "",
                        onCheckedChange = {
                            quickViewModel.updateSyncCloset(it)
                            coroutineScope.launch {
                                // 延迟 50ms，等待滚动完成
                                delay(50)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        },
                        onBottomCommentChange = {
                            addClosetViewModel.updateClosetComment(it)
                        },
                        onClick = {
                            addClosetViewModel.updateSheetType(it)
                        })
                    Spacer(modifier = Modifier.height(12.dp))
                    EditQuickStockScreen(
                        sync = uiState.syncStock,
                        name = addStockUiState.stockEntity.name,
                        goodsRack = rackEntity?.name ?: "",
                        stockCategory = stockSubCategory?.name ?: "",
                        period = period?.name ?: "",
                        stockProduct = stockProduct?.name ?: "",
                        bottomStockComment = addStockUiState.stockEntity.comment,
                        onCheckedChange = {
                            quickViewModel.updateSyncStock(it)
                            coroutineScope.launch {
                                delay(50)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        },
                        onBottomCommentChange = {
                            stockViewModel.updateClosetComment(it)
                        },
                        onNameChange = {
                            stockViewModel.updateStockName(it)
                        },
                        onClick = {
                            stockViewModel.updateSheetType(it)
                        })
                }
            }
        }

        if (quickViewModel.showBottomSheet(ShowBottomSheetType.DATE)) {
            // 日期选择器
            CustomDatePickerModal(onDateSelected = {
                quickViewModel.updateDate(it)
            }, onDismiss = {
                quickViewModel.dismissBottomSheet()
            })
        }

    }

    ListBottomSheet(initial = payWay, title = "付款方式", dataSource = payWays, visible = { uiState.sheetType == ShowBottomSheetType.PAY_WAY }, displayText = { it.name }, onSettingClick = {
        quickViewModel.dismissBottomSheet()
        navController.navigate(RoutePath.EditPayWay.route)
    }, onDismiss = { quickViewModel.dismissBottomSheet() }) {
        quickViewModel.updatePayWay(it)
    }

    ColorTypeBottomSheet(color = colorType, colorList = colorTypes, visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.COLOR) }, onDismiss = {
        addClosetViewModel.dismissBottomSheet()
    }, onConfirm = {
        addClosetViewModel.updateClosetColor(it)
    }, onSettingClick = {
        addClosetViewModel.dismissBottomSheet()
        navController.navigate(RoutePath.EditColor.route)
    })

    ListBottomSheet<ProductEntity>(
        initial = product,
        title = "品牌",
        dataSource = products,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.PRODUCT) },
        displayText = { it.name },
        onDismiss = { addClosetViewModel.dismissBottomSheet() },
        onSettingClick = {
            addClosetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditProduct.route)
        }) {
        addClosetViewModel.updateClosetProduct(it)
    }

    ListBottomSheet<OwnerEntity>(
        initial = owner,
        title = "归属",
        dataSource = addClosetViewModel.owners,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.OWNER) },
        displayText = { it.name },
        onDismiss = { addClosetViewModel.dismissBottomSheet() }) {
        addClosetViewModel.updateClosetOwner(it)
    }

    GridBottomSheet<SizeEntity>(
        initial = size,
        title = "尺码",
        dataSource = sizes,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.SIZE) },
        displayText = { it.name },
        dpSize = DpSize(52.dp, 36.dp),
        column = GridCells.Fixed(5),
        onDismiss = { addClosetViewModel.dismissBottomSheet() },
        onSettingClick = {
            addClosetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditSize.route)
        }) {
        addClosetViewModel.updateClosetSize(it)
    }

    GridBottomSheet<SeasonEntity>(
        initial = selectSeasons,
        title = "季节",
        dataSource = addClosetViewModel.seasons,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.SEASON) },
        displayText = { it.name },
        dpSize = DpSize(66.dp, 36.dp),
        column = GridCells.Fixed(4),
        onDismiss = { addClosetViewModel.dismissBottomSheet() }) {
        addClosetViewModel.updateClosetSeason(it)
    }

    CategoryBottomSheet(
        categories = closetCategories,
        categoryEntity = closetCategory,
        subCategoryEntity = closetSubCategory,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.CATEGORY) },
        onDismiss = {
            addClosetViewModel.dismissBottomSheet()
        },
        onSettingClick = {
            addClosetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditCategory.route)
        },
        onConfirm = { category, subCategory ->
            LogUtils.i("选中的分类： $category, $subCategory")
            addClosetViewModel.updateSelectedCategory(category, subCategory)
        })

    // 囤货相关布局
    ListBottomSheet<RackEntity>(
        initial = rackEntity,
        title = "货架",
        dataSource = stockViewModel.racks,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.RACK) },
        displayText = { it.name },
        onDismiss = { stockViewModel.dismissBottomSheet() }) {
        stockViewModel.updateRack(it)
    }

    ListBottomSheet<ProductEntity>(
        initial = stockProduct,
        title = "品牌",
        dataSource = products,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.STOCK_PRODUCT) },
        displayText = { it.name },
        onDismiss = { stockViewModel.dismissBottomSheet() },
        onSettingClick = {
            stockViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditProduct.route)
        }) {
        stockViewModel.updateProduct(it)
    }

    ListBottomSheet<RackSubCategoryEntity>(
        initial = stockSubCategory,
        title = "类别",
        dataSource = rackSubCategoryList,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.STOCK_CATEGORY) },
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


}


@Composable @Preview(showBackground = true) fun WatchAndEditQuickHomePagePreview() {
    HomeBookTheme {
        WatchAndEditQuickPage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}