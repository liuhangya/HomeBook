package com.fanda.homebook.quick

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomTopAppBar
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.quick.ui.EditAmountField
import com.fanda.homebook.quick.ui.EditClosetScreen
import com.fanda.homebook.quick.ui.SelectCategoryGrid
import com.fanda.homebook.quick.ui.TopTypeSelector
import com.fanda.homebook.ui.theme.HomeBookTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/*
* 记一笔页面
* */
@Composable
fun QuickHomePage(modifier: Modifier = Modifier, navController: NavController) {

    var date by remember { mutableStateOf(convertMillisToDate(System.currentTimeMillis())) }
    var showDateSelect by remember { mutableStateOf(false) }
    var showSyncCloset by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }

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
                    detectTapGestures(onTap = { focusManager.clearFocus() }, onDoubleTap = { focusManager.clearFocus() }, onLongPress = { focusManager.clearFocus() })
                }
                .background(Color.Transparent) // 必须有背景或 clickable 才能响应事件
        ) {
            // 为了让 padding 内容能滑动，所以用 Column 包起来
            Column(
                modifier = Modifier
                    .padding(padding)
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
                            rightText = "微信",
                            modifier = Modifier
                                .height(64.dp)
                                .padding(start = 20.dp, end = 10.dp)
                        ) {
                            Log.d("QuickHomePage", "点击了付款方式")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    EditClosetScreen(showSyncCloset = showSyncCloset, onCheckedChange = {
                        showSyncCloset = it
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