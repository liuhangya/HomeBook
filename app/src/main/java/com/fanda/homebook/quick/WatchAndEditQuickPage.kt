package com.fanda.homebook.quick

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.ListBottomSheet
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.quick.ui.EditAmountField
import com.fanda.homebook.quick.ui.SelectCategoryGrid
import com.fanda.homebook.quick.ui.TopTypeSelector
import com.fanda.homebook.quick.viewmodel.WatchAndEditQuickViewModel
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.DATE_FORMAT_MD
import com.fanda.homebook.tools.EventManager
import com.fanda.homebook.tools.EventType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster


/*
* 记一笔页面
* */
@Composable fun WatchAndEditQuickPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    quickViewModel: WatchAndEditQuickViewModel = viewModel(factory = AppViewModelProvider.factory),
) {
    val uiState by quickViewModel.uiState.collectAsState()
    val categories by quickViewModel.categories.collectAsState()
    val subCategories by quickViewModel.subCategories.collectAsState()
    val category by quickViewModel.category.collectAsState()
    val subCategory by quickViewModel.subCategory.collectAsState()
    val payWay by quickViewModel.payWay.collectAsState()
    val payWays by quickViewModel.payWays.collectAsState()

    LogUtils.d("uiState: $uiState")
    LogUtils.d("category: $category")
    LogUtils.d("subCategory: $subCategory")

    // 获取焦点管理器
    val focusManager = LocalFocusManager.current

    val scrollState = rememberScrollState()


    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "修改",
            onBackClick = {
                navController.navigateUp()
            },
            rightText = "保存",
            onRightActionClick = {
                focusManager.clearFocus()
                quickViewModel.updateQuickEntityDatabase {
                    EventManager.sendRefreshEventDelay(uiState.quickEntity.id)
                    Toaster.show("保存成功")
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

                    if (quickViewModel.isHasAnySync()) GradientRoundedBoxWithStroke(modifier = modifier) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(63.dp)
                                .padding(horizontal = 20.dp),
                        ) {
                            Text(
                                style = TextStyle.Default, text = quickViewModel.getSyncTitle(), color = Color.Black.copy(alpha = 0.4f), fontWeight = FontWeight.Medium, fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                modifier = Modifier.scale(0.8f), enabled = false, checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color.Black.copy(alpha = 0.4f),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedBorderColor = Color.Transparent,
                                    uncheckedTrackColor = Color.Black.copy(alpha = 0.4f),
                                )
                            )
                        }
                    }
                }
            }
        }

        if (quickViewModel.showBottomSheet(ShowBottomSheetType.DATE)) {
            // 日期选择器
            CustomDatePickerModal(initialDate = uiState.quickEntity.date, onDateSelected = {
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

}


@Composable @Preview(showBackground = true) fun WatchAndEditQuickHomePagePreview() {
    HomeBookTheme {
        WatchAndEditQuickPage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}