package com.fanda.homebook.quick

import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomTopAppBar
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.quick.sheet.ClosetTypeBottomSheet
import com.fanda.homebook.quick.sheet.ColorType
import com.fanda.homebook.quick.sheet.ColorTypeBottomSheet
import com.fanda.homebook.quick.sheet.PayWayBottomSheet
import com.fanda.homebook.quick.sheet.ProductTypeBottomSheet
import com.fanda.homebook.quick.sheet.SelectedCategory
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.quick.ui.EditAmountField
import com.fanda.homebook.quick.ui.EditClosetScreen
import com.fanda.homebook.quick.ui.EditStockScreen
import com.fanda.homebook.quick.ui.SelectCategoryGrid
import com.fanda.homebook.quick.ui.TopTypeSelector
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme


/*
* 记一笔页面
* */
@Composable fun QuickHomePage(modifier: Modifier = Modifier, navController: NavController) {

    var date by remember { mutableStateOf(convertMillisToDate(System.currentTimeMillis())) }
    var showDateSelect by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showProductBottomSheet by remember { mutableStateOf(false) }
    var showColorBottomSheet by remember { mutableStateOf(false) }
    var showClosetCategoryBottomSheet by remember { mutableStateOf(false) }
    var showSyncCloset by remember { mutableStateOf(true) }
    var showSyncStock by remember { mutableStateOf(false) }
    var bottomClosetComment by remember { mutableStateOf("") }
    var bottomStockComment by remember { mutableStateOf("") }
    var inputText by remember { mutableStateOf("") }
    var payWay by remember { mutableStateOf("微信") }
    var product by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(ColorType("",0x00000000)) }

    var currentClosetCategory by remember { mutableStateOf<SelectedCategory?>(null) }

    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        CustomTopAppBar(
            title = "记一笔",
            onBackClick = {
                navController.navigateUp()
            },
            rightText = "保存",
            onRightActionClick = {

            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )
    }) { padding ->
        // 获取焦点管理器
        val focusManager = LocalFocusManager.current
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
                        .padding(20.dp)
                ) {
                    TopTypeSelector(onDateClick = {
                        showDateSelect = true
                    }, date = date)
                    Spacer(modifier = Modifier.height(20.dp))
                    EditAmountField()
                    Spacer(modifier = Modifier.height(12.dp))
                    SelectCategoryGrid()
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "备注",
                            showRightArrow = false,
                            showTextField = true,
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
                            title = "付款方式", rightText = payWay, showText = true, modifier = Modifier
                                .height(64.dp)
                                .padding(start = 20.dp, end = 10.dp)
                        ) {
                            showBottomSheet = !showBottomSheet
                            Log.d("QuickHomePage", "点击了付款方式")
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
                        onCheckedChange = {
                            showSyncCloset = it
                            showSyncStock = !it
                        },
                        onBottomCommentChange = {
                            bottomClosetComment = it
                        },
                        onClosetCategoryClick = {
                            showClosetCategoryBottomSheet = true
                        }, onProductClick = {
                            showProductBottomSheet = true
                        }, onColorClick = {
                            showColorBottomSheet = true
                        })
                    Spacer(modifier = Modifier.height(12.dp))
                    EditStockScreen(showSyncStock = showSyncStock, bottomComment = bottomStockComment, onCheckedChange = {
                        showSyncStock = it
                        showSyncCloset = !it
                    }, onBottomCommentChange = {
                        bottomStockComment = it
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

    PayWayBottomSheet(payWay = payWay, showBottomSheet = showBottomSheet, onDismiss = {
        showBottomSheet = false
    }, onConfirm = {
        payWay = it
    })

    ProductTypeBottomSheet(product = product, showBottomSheet = showProductBottomSheet, onDismiss = {
        showProductBottomSheet = false
    }, onConfirm = {
        product = it
    })

    ColorTypeBottomSheet(color = color, showBottomSheet = showColorBottomSheet, onDismiss = {
        showColorBottomSheet = false
    }, onConfirm = {
        color = it
    })

    ClosetTypeBottomSheet(categories = LocalDataSource.closetCategoryData, currentCategory = currentClosetCategory, showBottomSheet = showClosetCategoryBottomSheet, onDismiss = {
        showClosetCategoryBottomSheet = false
    }, onConfirm = {
        currentClosetCategory = it
    })
}


@Composable @Preview(showBackground = true) fun QuickHomePagePreview() {
    HomeBookTheme {
        QuickHomePage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}