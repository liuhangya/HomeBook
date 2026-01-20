package com.fanda.homebook.stock

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.fanda.homebook.R
import com.fanda.homebook.closet.ui.ClosetInfoScreen
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.ClosetTypeBottomSheet
import com.fanda.homebook.quick.sheet.ColorType
import com.fanda.homebook.quick.sheet.ColorTypeBottomSheet
import com.fanda.homebook.quick.sheet.GridBottomSheet
import com.fanda.homebook.quick.sheet.ListBottomSheet
import com.fanda.homebook.quick.sheet.SelectedCategory
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.stock.sheet.StockCommentBottomSheet
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import kotlinx.coroutines.launch


/*
* 添加囤货页面
* */
@Composable fun AddStockPage(modifier: Modifier = Modifier, navController: NavController) {
    var date by remember { mutableStateOf(convertMillisToDate(System.currentTimeMillis())) }
    var comment by remember { mutableStateOf("") }
    var showCommentBottomSheet by remember { mutableStateOf(false) }
    var showUsedUpDatePicker by remember { mutableStateOf(false) }
    var syncBook by remember { mutableStateOf(true) }
    var wearCount by remember { mutableIntStateOf(1) }
    var product by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(ColorType("", -1L)) }
    var season by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(0f) }
    var remain by remember { mutableStateOf("") }
    var usedUpDate by remember { mutableStateOf(convertMillisToDate(System.currentTimeMillis(), "yyyy-MM-dd")) }
    var feel by remember { mutableStateOf("") }

    var currentShowBottomSheetType by remember { mutableStateOf(ShowBottomSheetType.NONE) }

    var currentClosetCategory by remember { mutableStateOf<SelectedCategory?>(null) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    // 获取焦点管理器
    val focusManager = LocalFocusManager.current

    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding(), snackbarHost = {
        SnackbarHost(hostState = snackBarHostState)
    }, floatingActionButton = {
        FloatingActionButton(containerColor = Color.Black, contentColor = Color.White, onClick = {
            showCommentBottomSheet = true
        }) {
            Text(text = "不用了", modifier = Modifier.padding(0.dp))
        }
    }, topBar = {
        TopIconAppBar(
            title = "单品信息",
            onBackClick = {
                navController.navigateUp()
            },
            rightText = "保存",
            onRightActionClick = {
                focusManager.clearFocus()
                scope.launch {
                    snackBarHostState.showSnackbar("保存成功")
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
                        model = R.mipmap.bg_closet_dufault,
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
                            title = "归属", rightText = owner, showText = true, modifier = Modifier
                                .height(64.dp)
                                .padding(start = 20.dp, end = 20.dp)
                        ) {
                            focusManager.clearFocus()
                            currentShowBottomSheetType = ShowBottomSheetType.OWNER
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    WearCountAndCost(price.toString(), wearCount) {
                        focusManager.clearFocus()
                        wearCount++
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ClosetInfoScreen(bottomComment = comment,
                        closetCategory = currentClosetCategory?.categoryName ?: "",
                        closetSubCategory = currentClosetCategory?.subCategoryName ?: "",
                        product = product,
                        color = color.color,
                        season = season,
                        date = date,
                        syncBook = syncBook,
                        size = size,
                        price = price.toString(),
                        onCheckedChange = {
                            syncBook = it
                            LogUtils.d("同步至当日账单： $it")
                        },
                        onBottomCommentChange = {
                            comment = it
                        },
                        onPriceChange = {
                            price = it.toFloat()
                        },
                        onClick = {
                            currentShowBottomSheetType = it
                        })
                }
            }
        }
    }

    if (currentShowBottomSheetType == ShowBottomSheetType.BUY_DATE || showUsedUpDatePicker) {
        // 日期选择器
        CustomDatePickerModal(onDateSelected = {
            if (currentShowBottomSheetType == ShowBottomSheetType.BUY_DATE) {
                date = convertMillisToDate(it ?: System.currentTimeMillis(), "yyyy-MM-dd")
            } else {
                usedUpDate = convertMillisToDate(it ?: System.currentTimeMillis(), "yyyy-MM-dd")
            }
        }, onDismiss = {
            currentShowBottomSheetType = ShowBottomSheetType.NONE
            showUsedUpDatePicker = false
        })
    }

    ListBottomSheet(initial = product,
        title = "品牌",
        dataSource = LocalDataSource.productData,
        visible = { currentShowBottomSheetType == ShowBottomSheetType.PRODUCT },
        displayText = { it },
        onDismiss = { currentShowBottomSheetType = ShowBottomSheetType.NONE }) {
        product = it!!
    }

    ListBottomSheet(initial = owner,
        title = "归属",
        dataSource = LocalDataSource.ownerData,
        visible = { currentShowBottomSheetType == ShowBottomSheetType.OWNER },
        displayText = { it },
        onDismiss = { currentShowBottomSheetType = ShowBottomSheetType.NONE }) {
        owner = it!!
    }

    GridBottomSheet(initial = owner,
        title = "尺码",
        dataSource = LocalDataSource.sizeData,
        visible = { currentShowBottomSheetType == ShowBottomSheetType.SIZE },
        displayText = { it },
        dpSize = DpSize(52.dp, 36.dp),
        column = GridCells.Fixed(5),
        onDismiss = { currentShowBottomSheetType = ShowBottomSheetType.NONE }) {
        size = it!!
    }

    GridBottomSheet(initial = season,
        title = "季节",
        dataSource = LocalDataSource.seasonData,
        visible = { currentShowBottomSheetType == ShowBottomSheetType.SEASON },
        displayText = { it },
        dpSize = DpSize(66.dp, 36.dp),
        column = GridCells.Fixed(4),
        onDismiss = { currentShowBottomSheetType = ShowBottomSheetType.NONE }) {
        season = it!!
    }


//    ColorTypeBottomSheet(color = color, visible = { currentShowBottomSheetType == ShowBottomSheetType.COLOR }, onDismiss = {
//        currentShowBottomSheetType = ShowBottomSheetType.NONE
//    }, onConfirm = {
//        color = it
//    }, onSettingClick = {
//        currentShowBottomSheetType = ShowBottomSheetType.NONE
//        navController.navigate(RoutePath.ClosetEditColor.route)
//    })

    ClosetTypeBottomSheet(categories = LocalDataSource.closetCategoryData,
        currentCategory = currentClosetCategory,
        visible = { currentShowBottomSheetType == ShowBottomSheetType.CATEGORY },
        onDismiss = {
            currentShowBottomSheetType = ShowBottomSheetType.NONE
        },
        onConfirm = {
            currentClosetCategory = it
        })

    StockCommentBottomSheet(remain = remain, date = usedUpDate, feel = feel, visible = showCommentBottomSheet, onDismiss = {
        showCommentBottomSheet = false
    }, onDateClick = {
        showUsedUpDatePicker = true
    }, onRemainClick = {
        remain = it
    }, onFeelClick = {
        feel = it
    }, onConfirm = {
        showCommentBottomSheet = false
    })

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
                title = "穿着次数：${wearCount}次", showText = true, showRightArrow = false, rightText = "", showPlus = true, showDivider = true, modifier = itemPadding, onPlusClick = onPlusClick
            )

            ItemOptionMenu(
                title = "穿着成本", showText = true, rightText = showPrice, showDivider = false, showRightArrow = false, modifier = itemPadding
            )
        }
    }
}


@Composable @Preview(showBackground = true) fun AddStockPagePreview() {
    HomeBookTheme {
        AddStockPage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}