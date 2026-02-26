package com.fanda.homebook.quick

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.common.sheet.ListBottomSheet
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.quick.ui.EditAmountField
import com.fanda.homebook.quick.ui.SelectCategoryGrid
import com.fanda.homebook.quick.ui.TopTypeSelector
import com.fanda.homebook.quick.viewmodel.WatchAndEditQuickViewModel
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.DATE_FORMAT_MD
import com.fanda.homebook.tools.EventManager
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster


/*
* 记一笔页面 - 用于查看和编辑单条快速记账记录
* 主要功能：修改已有的记账记录，包括金额、分类、日期、备注等信息
*/
@Composable fun WatchAndEditQuickPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    quickViewModel: WatchAndEditQuickViewModel = viewModel(factory = AppViewModelProvider.factory),
) {
    // 从ViewModel中收集状态数据
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

    // 获取焦点管理器，用于控制输入法显示/隐藏
    val focusManager = LocalFocusManager.current

    // 创建滚动状态，支持页面内容滚动
    val scrollState = rememberScrollState()


    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            // 顶部应用栏
            TopIconAppBar(
                title = "修改", // 页面标题
                onBackClick = {
                    // 返回按钮点击事件
                    navController.navigateUp()
                },
                rightText = "保存", // 右上角保存按钮
                onRightActionClick = {
                    // 保存按钮点击事件
                    focusManager.clearFocus() // 清除焦点，关闭输入法
                    quickViewModel.updateQuickEntityDatabase {
                        // 保存成功后发送刷新事件并提示
                        EventManager.sendStickyRefreshEventDelay(uiState.quickEntity.id)
                        Toaster.show("保存成功")
                        navController.navigateUp() // 返回上一页
                    }
                },
                backIconPainter = painterResource(R.mipmap.icon_back), // 返回图标
            )
        }) { padding ->

        // 创建一个覆盖整个屏幕的可点击区域（放在最外层）
        // 用于点击空白处取消输入框焦点，关闭输入法
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // 给最外层添加事件，用于取消输入框的焦点，从而关闭输入法
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
                    // 顶部类型选择器（收支类型和日期选择）
                    TopTypeSelector(data = categories, transactionType = category, date = convertMillisToDate(uiState.quickEntity.date, DATE_FORMAT_MD), onDateClick = {
                        // 日期点击事件，显示日期选择器
                        quickViewModel.updateSheetType(ShowBottomSheetType.DATE)
                    }, onTypeChange = {
                        // 收支类型变更事件
                        quickViewModel.updateCategory(it)
                    })

                    Spacer(modifier = Modifier.height(20.dp))

                    // 金额编辑输入框
                    EditAmountField(
                        price = uiState.quickEntity.price, onValueChange = {
                            quickViewModel.updatePrice(it)
                        })

                    Spacer(modifier = Modifier.height(12.dp))

                    // 子分类选择网格
                    SelectCategoryGrid(
                        initial = subCategory, items = subCategories, onItemClick = {
                            quickViewModel.updateSubCategory(it)
                        })

                    Spacer(modifier = Modifier.height(12.dp))

                    // 备注编辑区域
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
                                // 备注内容变更事件
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
                            // 付款方式点击事件，显示付款方式选择底部弹窗
                            focusManager.clearFocus()
                            quickViewModel.updateSheetType(ShowBottomSheetType.PAY_WAY)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 同步状态显示（如果有同步功能的话）
                    if (quickViewModel.isHasAnySync()) {
                        GradientRoundedBoxWithStroke(modifier = modifier) {
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
        }

        // 日期选择器弹窗
        if (quickViewModel.showBottomSheet(ShowBottomSheetType.DATE)) {
            CustomDatePickerModal(initialDate = uiState.quickEntity.date, onDateSelected = {
                // 日期选择完成事件
                quickViewModel.updateDate(it)
            }, onDismiss = {
                // 弹窗关闭事件
                quickViewModel.dismissBottomSheet()
            })
        }

    }

    // 付款方式选择底部弹窗
    ListBottomSheet(initial = payWay, title = "付款方式", dataSource = payWays, visible = { uiState.sheetType == ShowBottomSheetType.PAY_WAY }, displayText = { it.name }, onSettingClick = {
        // 设置按钮点击事件，跳转到付款方式编辑页面
        quickViewModel.dismissBottomSheet()
        navController.navigate(RoutePath.EditPayWay.route)
    }, onDismiss = {
        // 弹窗关闭事件
        quickViewModel.dismissBottomSheet()
    }) {
        // 付款方式选择事件
        quickViewModel.updatePayWay(it)
    }

}


/**
 * 预览函数 - 用于Android Studio的Compose预览功能
 */
@Composable @Preview(showBackground = true) fun WatchAndEditQuickHomePagePreview() {
    HomeBookTheme {
        WatchAndEditQuickPage(
            modifier = Modifier.fillMaxWidth(), navController = rememberNavController()
        )
    }
}