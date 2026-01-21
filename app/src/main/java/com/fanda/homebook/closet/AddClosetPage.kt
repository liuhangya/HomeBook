package com.fanda.homebook.closet

import androidx.compose.foundation.background
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.fanda.homebook.closet.viewmodel.AddClosetViewModel
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.CategoryBottomSheet
import com.fanda.homebook.quick.sheet.ClosetTypeBottomSheet
import com.fanda.homebook.quick.sheet.ColorTypeBottomSheet
import com.fanda.homebook.quick.sheet.GridBottomSheet
import com.fanda.homebook.quick.sheet.ListBottomSheet
import com.fanda.homebook.quick.sheet.SelectedCategory
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import kotlinx.coroutines.launch


/*
* 添加衣橱页面
* */
@Composable fun AddClosetPage(
    modifier: Modifier = Modifier, navController: NavController, addClosetViewModel: AddClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 通过 ViewModel 状态管理进行数据绑定
    val addClosetUiState by addClosetViewModel.addClosetUiState.collectAsState()
    val colorTypes by addClosetViewModel.colorTypes.collectAsState()
    val products by addClosetViewModel.products.collectAsState()
    val sizes by addClosetViewModel.sizes.collectAsState()
    val categories by addClosetViewModel.categories.collectAsState()

    val colorType by addClosetViewModel.colorType.collectAsState()
    val season by addClosetViewModel.season.collectAsState()
    val product by addClosetViewModel.product.collectAsState()
    val size by addClosetViewModel.size.collectAsState()
    val owner by addClosetViewModel.owner.collectAsState()
    val category by addClosetViewModel.category.collectAsState()
    val subCategory by addClosetViewModel.subCategory.collectAsState()

    LogUtils.d("category: $category")
    LogUtils.d("subCategory: $subCategory")
    LogUtils.d("AddClosetPage: addClosetUiState: $addClosetUiState")

    // 获取焦点管理器
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding()
//        ,
//        floatingActionButton = {
//        FloatingActionButton(containerColor = Color.Black, contentColor = Color.White, onClick = {
//            scope.launch {
//                snackBarHostState.showSnackbar("不穿了")
//            }
//        }, modifier = Modifier.padding(5.dp)) {
//            Text(text = "不穿了")
//        }
//    }

        , topBar = {
            TopIconAppBar(
                title = "单品信息",
                onBackClick = {
                    navController.navigateUp()
                },
                rightText = "保存",
                onRightActionClick = {
                    focusManager.clearFocus()
                    addClosetViewModel.saveClosetEntityDatabase(context){
                        navController.navigateUp()
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
                        model = addClosetUiState.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "归属", rightText = owner?.name ?: "", showText = true, modifier = Modifier
                                .height(64.dp)
                                .padding(start = 20.dp, end = 20.dp)
                        ) {
                            focusManager.clearFocus()
                            addClosetViewModel.updateSheetType(ShowBottomSheetType.OWNER)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
//                    WearCountAndCost(addClosetUiState.closetEntity.price, addClosetUiState.closetEntity.wearCount) {
//                        focusManager.clearFocus()
//                        addClosetViewModel.plusClosetWearCount()
//                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ClosetInfoScreen(
                        bottomComment = addClosetUiState.closetEntity.comment,
                        closetCategory = category?.name ?: "",
                        closetSubCategory = subCategory?.name ?: "",
                        product = product?.name ?: "",
                        color = colorType?.color ?: -1,
                        season = season?.name ?: "",
                        date = convertMillisToDate(addClosetUiState.closetEntity.date, "yyyy-MM-dd"),
                        syncBook = addClosetUiState.closetEntity.syncBook,
                        size = size?.name ?: "",
                        price = addClosetUiState.closetEntity.price,
                        onCheckedChange = {
                            addClosetViewModel.updateClosetSyncBook(it)
                            LogUtils.d("同步至当日账单： $it")
                        },
                        onBottomCommentChange = {
                            addClosetViewModel.updateClosetComment(it)
                        },
                        onPriceChange = {
                            addClosetViewModel.updateClosetPrice(it)
                        },
                        onClick = {
                            addClosetViewModel.updateSheetType(it)
                        })
                }
            }
        }
    }

    if (addClosetViewModel.showBottomSheet(ShowBottomSheetType.BUY_DATE)) {
        // 日期选择器
        CustomDatePickerModal(onDateSelected = {
            addClosetViewModel.updateClosetDate(it ?: System.currentTimeMillis())
        }, onDismiss = {
            addClosetViewModel.dismissBottomSheet()
        })
    }

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
        initial = season,
        title = "季节",
        dataSource = addClosetViewModel.seasons,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.SEASON) },
        displayText = { it.name },
        dpSize = DpSize(66.dp, 36.dp),
        column = GridCells.Fixed(4),
        onDismiss = { addClosetViewModel.dismissBottomSheet() }) {
        addClosetViewModel.updateClosetSeason(it)
    }


    ColorTypeBottomSheet(color = colorType, colorList = colorTypes, visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.COLOR) }, onDismiss = {
        addClosetViewModel.dismissBottomSheet()
    }, onConfirm = {
        addClosetViewModel.updateClosetColor(it)
    }, onSettingClick = {
        addClosetViewModel.dismissBottomSheet()
        navController.navigate(RoutePath.EditColor.route)
    })

    CategoryBottomSheet(
        categories = categories,
        categoryEntity = category,
        subCategoryEntity = subCategory,
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


}

@Composable fun WearCountAndCost(
    price: Float, wearCount: Int, modifier: Modifier = Modifier, onPlusClick: (() -> Unit)
) {
    val itemPadding = Modifier.padding(
        20.dp, 20.dp, 20.dp, 20.dp
    )
    val showPrice = if (price <= 0f) {
        ""
    } else {
        "${String.format("%.1f", price / wearCount)}元/次"
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


@Composable @Preview(showBackground = true) fun AddClosetPagePreview() {
    HomeBookTheme {
        AddClosetPage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}