package com.fanda.homebook.quick

import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomTopAppBar
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.SimpleBottomSheet
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.quick.ui.EditAmountField
import com.fanda.homebook.quick.ui.EditClosetScreen
import com.fanda.homebook.quick.ui.EditStockScreen
import com.fanda.homebook.quick.ui.SelectCategoryGrid
import com.fanda.homebook.quick.ui.TopTypeSelector
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/*
* 记一笔页面
* */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickHomePage(modifier: Modifier = Modifier, navController: NavController) {

    var date by remember { mutableStateOf(convertMillisToDate(System.currentTimeMillis())) }
    var showDateSelect by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSyncCloset by remember { mutableStateOf(true) }
    var showSyncStock by remember { mutableStateOf(false) }
    var bottomClosetComment by remember { mutableStateOf("") }
    var bottomStockComment by remember { mutableStateOf("") }
    var inputText by remember { mutableStateOf("") }
    var payWay by remember { mutableStateOf("微信") }

    Scaffold(modifier = modifier, topBar = {
        CustomTopAppBar(title = "记一笔", onBackClick = {
            navController.navigateUp()
        }, rightText = "保存", onRightActionClick = {

        }, backIconPainter = painterResource(R.mipmap.icon_back))
    }) { padding ->
        // 获取焦点管理器
        val focusManager = LocalFocusManager.current
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
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "付款方式",
                            rightText = payWay,
                            showText = true,
                            modifier = Modifier
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
                        onCheckedChange = {
                            showSyncCloset = it
                            showSyncStock = !it
                        },
                        onBottomCommentChange = {
                            bottomClosetComment = it
                        })
                    Spacer(modifier = Modifier.height(12.dp))
                    EditStockScreen(
                        showSyncStock = showSyncStock,
                        bottomComment = bottomStockComment,
                        onCheckedChange = {
                            showSyncStock = it
                            showSyncCloset = !it
                        },
                        onBottomCommentChange = {
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

        ShowBottomSheet(payWay = payWay, showBottomSheet = showBottomSheet, onDismiss = {
            showBottomSheet = false
        }, onConfirmClick = {
            showBottomSheet = false
            payWay = it
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowBottomSheet(
    payWay: String,
    showBottomSheet: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirmClick: (String) -> Unit,
) {
    var selectText by remember { mutableStateOf(payWay) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    val scope = rememberCoroutineScope()

    // 👇 提供一个带动画的关闭函数
    val dismissWithAnimation: () -> Unit = {
        scope.launch {
            sheetState.hide() // 触发动画
            onConfirmClick(selectText) // 执行确认逻辑
        }
    }

    // 👇 关键：创建一个 nested scroll connection
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 如果是用户手指滚动（非 fling），且垂直方向有滚动
                return if (available.y != 0f && source == NestedScrollSource.Drag) {
                    // 消费掉所有垂直滚动，不让 Bottom Sheet 收到
                    available
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 同样消费剩余滚动
                return if (available.y != 0f) available else Offset.Zero
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = modifier.heightIn(max = 480.dp),
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            scrimColor = Color.Black.copy(alpha = 0.4f),
            containerColor = Color.White,
            dragHandle = null,
            windowInsets = WindowInsets(0, 0, 0, 0),   // 这个参数用于控制显示的区域
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            // 自定义渐变背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(nestedScrollConnection) // 👈 应用在这里！
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.color_E3EBF5), // 顶部颜色（浅蓝）
                                Color.White  // 底部颜色（白）
                            )
                        ),
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            style = TextStyle.Default,
                            text = "标题",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        TextButton(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            onClick = dismissWithAnimation
                        ) {
                            Text(
                                style = TextStyle.Default,
                                text = "确定",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 9.dp)
                    ) {
                        items(LocalDataSource.payWayData, key = { it }) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier     // 要注意顺序，先点击事件，后加padding
                                    .clickable(onClick = {
                                        selectText = it
                                        Log.d("QuickHomePage", "选择支付方式：${it}")
                                    })
                                    .padding(vertical = 15.dp, horizontal = 24.dp)
                            ) {
                                Text(
                                    text = it
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (selectText == it) {
                                    Image(
                                        painter = painterResource(R.mipmap.icon_selected),
                                        contentDescription = null
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }

}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM月dd日", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Composable
@Preview(showBackground = true)
fun QuickHomePagePreview() {
    HomeBookTheme {
        QuickHomePage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}