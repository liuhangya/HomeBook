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
import com.fanda.homebook.common.sheet.CategoryExpandBottomSheet
import com.fanda.homebook.common.sheet.ColorTypeBottomSheet
import com.fanda.homebook.common.sheet.GridBottomSheet
import com.fanda.homebook.common.sheet.ListBottomSheet
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
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.quick.ui.EditAmountField
import com.fanda.homebook.quick.ui.EditQuickClosetScreen
import com.fanda.homebook.quick.ui.EditQuickStockScreen
import com.fanda.homebook.quick.ui.SelectCategoryGrid
import com.fanda.homebook.quick.ui.TopTypeSelector
import com.fanda.homebook.quick.viewmodel.AddQuickViewModel
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.stock.viewmodel.AddStockViewModel
import com.fanda.homebook.tools.DATE_FORMAT_MD
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 记一笔页面（快速记账页面）
 * 包含金额输入、分类选择、支付方式、衣橱同步、囤货同步等功能
 *
 * @param modifier 修饰符
 * @param navController 导航控制器
 * @param quickViewModel 快速记账ViewModel
 * @param addClosetViewModel 衣橱ViewModel
 * @param stockViewModel 囤货ViewModel
 */
@Composable fun AddQuickHomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    quickViewModel: AddQuickViewModel = viewModel(factory = AppViewModelProvider.factory),
    addClosetViewModel: AddClosetViewModel = viewModel(factory = AppViewModelProvider.factory),
    stockViewModel: AddStockViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 收集快速记账相关状态
    val uiState by quickViewModel.uiState.collectAsState()
    val categories by quickViewModel.categories.collectAsState()
    val subCategories by quickViewModel.subCategories.collectAsState()
    val category by quickViewModel.category.collectAsState()
    val subCategory by quickViewModel.subCategory.collectAsState()
    val payWay by quickViewModel.payWay.collectAsState()
    val payWays by quickViewModel.payWays.collectAsState()

    // 收集衣橱相关状态
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

    // 收集囤货相关状态
    val addStockUiState by stockViewModel.uiState.collectAsState()
    val rackEntity by stockViewModel.rackEntity.collectAsState()
    val stockProduct by stockViewModel.product.collectAsState()
    val stockSubCategory by stockViewModel.subCategory.collectAsState()
    val rackSubCategoryList by stockViewModel.rackSubCategoryList.collectAsState()
    val period by stockViewModel.period.collectAsState()

    LogUtils.d("uiState: $uiState")
    LogUtils.d("addClosetUiState: $addClosetUiState")
    LogUtils.d("addStockUiState: $addStockUiState")

    // 获取焦点管理器（用于关闭输入法）
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopIconAppBar(
                title = "记一笔",
                onBackClick = {
                    navController.navigateUp()  // 返回上一页
                },
                rightText = "保存",
                onRightActionClick = {
                    focusManager.clearFocus()  // 保存前先关闭键盘

                    // 先校验记一笔的基本参数
                    if (quickViewModel.checkParams()) {
                        // 根据是否选中同步衣橱，校验衣橱参数
                        if (quickViewModel.uiState.value.quickEntity.syncCloset) {
                            addClosetViewModel.updateClosetPrice(uiState.quickEntity.price)
                            if (!addClosetViewModel.checkParams()) {
                                return@TopIconAppBar  // 衣橱参数校验失败
                            }
                        }

                        // 根据是否选中同步囤货，校验囤货参数
                        if (quickViewModel.uiState.value.quickEntity.syncStock) {
                            stockViewModel.updatePrice(uiState.quickEntity.price)
                            if (!stockViewModel.checkParams()) {
                                return@TopIconAppBar  // 囤货参数校验失败
                            }
                        }

                        // 所有校验通过，开始保存
                        // 先保存衣橱数据（如果需要）
                        if (quickViewModel.uiState.value.quickEntity.syncCloset) {
                            addClosetViewModel.saveClosetEntityDatabase(context) {}
                        }

                        // 再保存囤货数据（如果需要）
                        if (quickViewModel.uiState.value.quickEntity.syncStock) {
                            stockViewModel.saveStockEntityDatabase(context) {}
                        }

                        // 最后保存记账数据
                        quickViewModel.saveQuickEntityDatabase {
                            Toaster.show("保存成功")
                            navController.navigateUp()  // 保存成功后返回
                        }
                    }
                },
                backIconPainter = painterResource(R.mipmap.icon_back),
            )
        }) { padding ->
        // 创建一个覆盖整个屏幕的可点击区域，用于点击空白处关闭输入法
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() }, onDoubleTap = { focusManager.clearFocus() }, onLongPress = { focusManager.clearFocus() })
            }
            .background(Color.Transparent)  // 透明背景，仅用于接收点击事件
        ) {
            // 主内容区域（可滚动）
            Column(
                modifier = Modifier
                    .padding(padding)
                    .imePadding()  // 让输入法能顶起内容
                    .verticalScroll(scrollState)  // 垂直滚动
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // 顶部类型选择器（分类和日期）
                    TopTypeSelector(data = categories, transactionType = category, date = convertMillisToDate(uiState.quickEntity.date, DATE_FORMAT_MD), onDateClick = {
                        quickViewModel.updateSheetType(ShowBottomSheetType.DATE)
                    }, onTypeChange = {
                        quickViewModel.updateCategory(it)
                    })

                    Spacer(modifier = Modifier.height(20.dp))

                    // 金额输入框
                    EditAmountField(price = uiState.quickEntity.price) {
                        quickViewModel.updatePrice(it)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 分类选择网格
                    SelectCategoryGrid(
                        initial = subCategory, items = subCategories
                    ) {
                        quickViewModel.updateSubCategory(it)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 备注输入框
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

                    // 付款方式选择
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

                    // 衣橱同步设置
                    EditQuickClosetScreen(
                        showSyncCloset = uiState.quickEntity.syncCloset,
                        bottomComment = addClosetUiState.closetEntity.comment,
                        closetCategory = closetCategory?.name ?: "",
                        closetSubCategory = closetSubCategory?.name ?: "",
                        product = product?.name ?: "",
                        color = colorType?.color ?: -1,
                        season = addClosetViewModel.getSeasonDes(selectSeasons),
                        owner = owner?.name ?: "",
                        size = size?.name ?: "",
                        onCheckedChange = { sync ->
                            quickViewModel.updateSyncCloset(sync)
                            // 开关切换后自动滚动到底部
                            coroutineScope.launch {
                                delay(50)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        },
                        onBottomCommentChange = {
                            addClosetViewModel.updateClosetComment(it)
                        },
                        onClick = { sheetType ->
                            addClosetViewModel.updateSheetType(sheetType)
                        })

                    Spacer(modifier = Modifier.height(12.dp))

                    // 囤货同步设置
                    EditQuickStockScreen(
                        sync = uiState.quickEntity.syncStock,
                        name = addStockUiState.stockEntity.name,
                        goodsRack = rackEntity?.name ?: "",
                        stockCategory = stockSubCategory?.name ?: "",
                        period = period?.name ?: "",
                        stockProduct = stockProduct?.name ?: "",
                        bottomStockComment = addStockUiState.stockEntity.comment,
                        onCheckedChange = { sync ->
                            quickViewModel.updateSyncStock(sync)
                            // 开关切换后自动滚动到底部
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
                        onClick = { sheetType ->
                            stockViewModel.updateSheetType(sheetType)
                        })
                }
            }
        }
    }

    // 日期选择器弹窗
    if (quickViewModel.showBottomSheet(ShowBottomSheetType.DATE)) {
        CustomDatePickerModal(initialDate = uiState.quickEntity.date, onDateSelected = { selectedDate ->
            quickViewModel.updateDate(selectedDate)
        }, onDismiss = {
            quickViewModel.dismissBottomSheet()
        })
    }

    // 付款方式选择弹窗
    ListBottomSheet(initial = payWay, title = "付款方式", dataSource = payWays, visible = { uiState.sheetType == ShowBottomSheetType.PAY_WAY }, displayText = { it.name }, onSettingClick = {
        quickViewModel.dismissBottomSheet()
        navController.navigate(RoutePath.EditPayWay.route)  // 跳转到支付方式管理
    }, onDismiss = { quickViewModel.dismissBottomSheet() }) { selectedPayWay ->
        quickViewModel.updatePayWay(selectedPayWay)
    }

    // 衣橱相关弹窗
    ColorTypeBottomSheet(
        color = colorType,
        colorList = colorTypes,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.COLOR) },
        onDismiss = { addClosetViewModel.dismissBottomSheet() },
        onConfirm = { selectedColor ->
            addClosetViewModel.updateClosetColor(selectedColor)
        },
        onSettingClick = {
            addClosetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditColor.route)  // 跳转到颜色管理
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
            navController.navigate(RoutePath.EditProduct.route)  // 跳转到品牌管理
        }) { selectedProduct ->
        addClosetViewModel.updateClosetProduct(selectedProduct)
    }

    ListBottomSheet<OwnerEntity>(
        initial = owner,
        title = "归属",
        dataSource = addClosetViewModel.owners,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.OWNER) },
        displayText = { it.name },
        onDismiss = { addClosetViewModel.dismissBottomSheet() }) { selectedOwner ->
        addClosetViewModel.updateClosetOwner(selectedOwner)
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
            navController.navigate(RoutePath.EditSize.route)  // 跳转到尺码管理
        }) { selectedSize ->
        addClosetViewModel.updateClosetSize(selectedSize)
    }

    GridBottomSheet<SeasonEntity>(
        initial = selectSeasons,
        title = "季节",
        dataSource = addClosetViewModel.seasons,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.SEASON) },
        displayText = { it.name },
        dpSize = DpSize(66.dp, 36.dp),
        column = GridCells.Fixed(4),
        onDismiss = { addClosetViewModel.dismissBottomSheet() }) { selectedSeasons ->
        addClosetViewModel.updateClosetSeason(selectedSeasons)
    }

    CategoryExpandBottomSheet(
        categories = closetCategories,
        categoryEntity = closetCategory,
        subCategoryEntity = closetSubCategory,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.CATEGORY) },
        onDismiss = { addClosetViewModel.dismissBottomSheet() },
        onSettingClick = {
            addClosetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditCategory.route)  // 跳转到分类管理
        },
        onConfirm = { selectedCategory, selectedSubCategory ->
            LogUtils.i("选中的分类： $selectedCategory, $selectedSubCategory")
            addClosetViewModel.updateSelectedCategory(selectedCategory, selectedSubCategory)
        })

    // 囤货相关弹窗
    ListBottomSheet<RackEntity>(
        initial = rackEntity,
        title = "货架",
        dataSource = stockViewModel.racks,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.RACK) },
        displayText = { it.name },
        onDismiss = { stockViewModel.dismissBottomSheet() }) { selectedRack ->
        stockViewModel.updateRack(selectedRack)
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
            navController.navigate(RoutePath.EditProduct.route)  // 跳转到品牌管理
        }) { selectedProduct ->
        stockViewModel.updateProduct(selectedProduct)
    }

    ListBottomSheet<RackSubCategoryEntity>(
        initial = stockSubCategory,
        title = "类别",
        dataSource = rackSubCategoryList,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.STOCK_CATEGORY) },
        displayText = { it.name },
        onDismiss = { stockViewModel.dismissBottomSheet() }) { selectedCategory ->
        stockViewModel.updateCategory(selectedCategory)
    }

    ListBottomSheet<PeriodEntity>(
        initial = period,
        title = "使用时段",
        dataSource = stockViewModel.periods,
        visible = { stockViewModel.showBottomSheet(ShowBottomSheetType.USAGE_PERIOD) },
        displayText = { it.name },
        onDismiss = { stockViewModel.dismissBottomSheet() }) { selectedPeriod ->
        stockViewModel.updatePeriod(selectedPeriod)
    }
}

@Composable @Preview(showBackground = true) fun AddQuickHomePagePreview() {
    HomeBookTheme {
        AddQuickHomePage(
            modifier = Modifier.fillMaxWidth(), navController = rememberNavController()
        )
    }
}