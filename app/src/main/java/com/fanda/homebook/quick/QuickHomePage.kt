package com.fanda.homebook.quick

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomTopAppBar
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.QuickShowBottomSheetType
import com.fanda.homebook.entity.TransactionType
import com.fanda.homebook.quick.sheet.ClosetTypeBottomSheet
import com.fanda.homebook.quick.sheet.ColorType
import com.fanda.homebook.quick.sheet.ColorTypeBottomSheet
import com.fanda.homebook.quick.sheet.GridBottomSheet
import com.fanda.homebook.quick.sheet.ListBottomSheet
import com.fanda.homebook.quick.sheet.SelectedCategory
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.quick.ui.EditAmountField
import com.fanda.homebook.quick.ui.EditClosetScreen
import com.fanda.homebook.quick.ui.SelectCategoryGrid
import com.fanda.homebook.quick.ui.TopTypeSelector
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme


/*
* 记一笔页面
* */
@Composable
fun QuickHomePage(modifier: Modifier = Modifier, navController: NavController) {

    var date by remember { mutableStateOf(convertMillisToDate(System.currentTimeMillis())) }
    var showDateSelect by remember { mutableStateOf(false) }
    var showSyncCloset by remember { mutableStateOf(true) }
    var bottomClosetComment by remember { mutableStateOf("") }
    var bottomStockComment by remember { mutableStateOf("") }
    var inputText by remember { mutableStateOf("") }
    var payWay by remember { mutableStateOf("微信") }
    var product by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(ColorType("", -1L)) }
    var season by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var goodsRack by remember { mutableStateOf("") }
    var stockCategory by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    var stockProduct by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    var currentShowBottomSheetType by remember { mutableStateOf(QuickShowBottomSheetType.NONE) }

    var currentClosetCategory by remember { mutableStateOf<SelectedCategory?>(null) }

    // 获取焦点管理器
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current

    fun getCategoryData() = when (transactionType) {
        TransactionType.EXPENSE -> {
            LocalDataSource.expenseCategoryData
        }

        TransactionType.INCOME -> {
            LocalDataSource.incomeCategoryData
        }

        TransactionType.EXCLUDED -> {
            LocalDataSource.excludeCategoryData
        }
    }

    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        CustomTopAppBar(
            title = "记一笔",
            onBackClick = {
                navController.navigateUp()
            },
            rightText = "保存",
            onRightActionClick = {
                focusManager.clearFocus()
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )
    }) { padding ->

        // 创建一个覆盖整个屏幕的可点击区域（放在最外层）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {// 给最外层添加事件，用于取消输入框的焦点，从而关闭输入法
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() },
                        onDoubleTap = { focusManager.clearFocus() },
                        onLongPress = { focusManager.clearFocus() })
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
                        .padding(20.dp)
                ) {
                    TopTypeSelector(
                        transactionType = transactionType,
                        date = date,
                        onDateClick = {
                            showDateSelect = true
                        },
                        onTypeChange = {
                            transactionType = it
                        })
                    Spacer(modifier = Modifier.height(20.dp))
                    EditAmountField()
                    Spacer(modifier = Modifier.height(12.dp))
                    SelectCategoryGrid(items = getCategoryData())
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
                            inputText = inputText,
                            onValueChange = {
                                inputText = it
                            })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "付款方式",
                            rightText = payWay,
                            showText = true,
                            modifier = Modifier
                                .height(64.dp)
                                .padding(start = 20.dp, end = 20.dp)
                        ) {
                            focusManager.clearFocus()
                            currentShowBottomSheetType = QuickShowBottomSheetType.PAY_WAY
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    EditClosetScreen(
                        showSyncCloset = showSyncCloset,
                        bottomComment = bottomClosetComment,
                        closetCategory = currentClosetCategory?.categoryName ?: "",
                        closetSubCategory = currentClosetCategory?.subCategoryName ?: "",
                        product = product,
                        color = color.color,
                        season = season,
                        owner = owner,
                        size = size,
                        name = name,
                        goodsRack = goodsRack,
                        stockCategory = stockCategory,
                        period = period,
                        stockProduct = stockProduct,
                        bottomStockComment = bottomStockComment,
                        onCheckedChange = {
                            showSyncCloset = it
                        },
                        onBottomCommentChange = {
                            if (showSyncCloset) {
                                bottomClosetComment = it
                            } else {
                                bottomStockComment = it
                            }
                        },
                        onNameChange = {
                            name = it
                        },
                        onClick = {
                            if (it == QuickShowBottomSheetType.STOCK_CATEGORY && goodsRack.isEmpty()) {
                                Toast.makeText(context, "请先选择货架", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                currentShowBottomSheetType = it
                            }
                        })
                }
            }
        }

        if (showDateSelect) {
            // 日期选择器
            CustomDatePickerModal(onDateSelected = {
                Log.d("QuickHomePage", "选择日期：${it}")
                date = convertMillisToDate(it ?: System.currentTimeMillis())
            }, onDismiss = {
                Log.d("QuickHomePage", "取消选择日期")
                showDateSelect = false
            })
        }

    }

    ListBottomSheet(
        initial = payWay,
        title = "付款方式",
        dataSource = LocalDataSource.payWayData,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.PAY_WAY },
        displayText = { it },
        onDismiss = { currentShowBottomSheetType = QuickShowBottomSheetType.NONE }) {
        payWay = it
    }

    ListBottomSheet(
        initial = product,
        title = "品牌",
        dataSource = LocalDataSource.productData,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.PRODUCT },
        displayText = { it },
        onDismiss = { currentShowBottomSheetType = QuickShowBottomSheetType.NONE }) {
        product = it
    }

    ListBottomSheet(
        initial = owner,
        title = "归属",
        dataSource = LocalDataSource.ownerData,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.OWNER },
        displayText = { it },
        onDismiss = { currentShowBottomSheetType = QuickShowBottomSheetType.NONE }) {
        owner = it
    }

    GridBottomSheet(
        initial = owner,
        title = "尺码",
        dataSource = LocalDataSource.sizeData,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.SIZE },
        displayText = { it },
        dpSize = DpSize(52.dp, 36.dp),
        column = GridCells.Fixed(5),
        onDismiss = { currentShowBottomSheetType = QuickShowBottomSheetType.NONE }) {
        size = it
    }

    GridBottomSheet(
        initial = season,
        title = "季节",
        dataSource = LocalDataSource.seasonData,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.SEASON },
        displayText = { it },
        dpSize = DpSize(66.dp, 36.dp),
        column = GridCells.Fixed(4),
        onDismiss = { currentShowBottomSheetType = QuickShowBottomSheetType.NONE }) {
        season = it
    }


    ColorTypeBottomSheet(
        color = color,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.COLOR },
        onDismiss = {
            currentShowBottomSheetType = QuickShowBottomSheetType.NONE
        },
        onConfirm = {
            color = it
        })

    ClosetTypeBottomSheet(
        categories = LocalDataSource.closetCategoryData,
        currentCategory = currentClosetCategory,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.CATEGORY },
        onDismiss = {
            currentShowBottomSheetType = QuickShowBottomSheetType.NONE
        },
        onConfirm = {
            currentClosetCategory = it
        })

    ListBottomSheet(
        initial = stockProduct,
        title = "品牌",
        dataSource = LocalDataSource.productData,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.STOCK_PRODUCT },
        displayText = { it },
        onDismiss = { currentShowBottomSheetType = QuickShowBottomSheetType.NONE }) {
        stockProduct = it
    }

    ListBottomSheet(
        initial = goodsRack,
        title = "货架",
        dataSource = LocalDataSource.goodsRackData,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.GOODS_RACK },
        displayText = { it },
        onDismiss = { currentShowBottomSheetType = QuickShowBottomSheetType.NONE }) {
        if (goodsRack != it) {
            // 切换货架时，清空商品
            stockCategory = ""
        }
        goodsRack = it
    }

    ListBottomSheet(
        initial = stockCategory,
        title = "类别",
        dataSource = LocalDataSource.stockCategoryData.find { it.name == goodsRack }?.children?.map { it.name }
            ?: emptyList(),
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.STOCK_CATEGORY },
        displayText = { it },
        onDismiss = { currentShowBottomSheetType = QuickShowBottomSheetType.NONE }) {
        stockCategory = it
    }

    ListBottomSheet(
        initial = period,
        title = "使用时段",
        dataSource = LocalDataSource.periodData,
        visible = { currentShowBottomSheetType == QuickShowBottomSheetType.PERIOD },
        displayText = { it },
        onDismiss = { currentShowBottomSheetType = QuickShowBottomSheetType.NONE }) {
        period = it
    }


}


@Composable
@Preview(showBackground = true)
fun QuickHomePagePreview() {
    HomeBookTheme {
        QuickHomePage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}