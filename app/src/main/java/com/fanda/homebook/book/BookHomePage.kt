package com.fanda.homebook.book

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.fanda.homebook.book.sheet.TransactionTypeBottomSheet
import com.fanda.homebook.book.sheet.YearMonthBottomSheet
import com.fanda.homebook.book.state.BookUiState
import com.fanda.homebook.book.ui.DailyItemWidget
import com.fanda.homebook.book.ui.MonthItemWidget
import com.fanda.homebook.book.viewmodel.BookViewModel
import com.fanda.homebook.components.ConfirmDialog
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.NumberEditDialog
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.book.entity.AmountItemEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.entity.TransactionAmountType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.EventManager
import com.fanda.homebook.tools.EventType
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.UserCache
import com.fanda.homebook.tools.formatYearMonth
import com.fanda.homebook.tools.roundToString
import com.fanda.homebook.tools.toJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) @Composable fun BookHomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    isDrawerOpen: Boolean,
    onShowDrawer: (@Composable () -> Unit) -> Unit,
    onCloseDrawer: () -> Unit,
    bookViewModel: BookViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 收集状态流
    val uiState by bookViewModel.uiState.collectAsState()
    val curSelectedBook by bookViewModel.curSelectedBook.collectAsState()
    val planEntity by bookViewModel.planEntity.collectAsState()
    val books by bookViewModel.books.collectAsState()
    val categories by bookViewModel.categories.collectAsState()
    val subCategory by bookViewModel.subCategory.collectAsState()
    val transactionData by bookViewModel.transactionData.collectAsState()
    val monthSummaryData by bookViewModel.monthSummaryData.collectAsState()
    val yearSummaryData by bookViewModel.yearSummaryData.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    LogUtils.d("uiState: $uiState")
    LogUtils.d("transactionData: $transactionData")
    LogUtils.d("monthSummaryData: $monthSummaryData")
    LogUtils.d("yearSummaryData: ${yearSummaryData?.toJson()}")

    val scope = rememberCoroutineScope()

    // 监听生命周期变化，当页面恢复时刷新数据，太频繁了，不用这种方式
//    LaunchedEffect(Unit) {
//        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
//            bookViewModel.refresh()
//            LogUtils.d("收到刷新数据事件")
//        }
//    }

    // 监听事件总线，接收刷新数据事件
    LaunchedEffect(Unit) {
        val event = EventManager.getStickyEvent(EventType.REFRESH_STICKY_EVENT)
        event?.let {
            // 处理粘性事件
            bookViewModel.refresh()
            LogUtils.d("收到刷新数据事件")
        }
    }

    // 监听侧边栏状态，关闭时取消编辑模式
    LaunchedEffect(isDrawerOpen) {
        if (!isDrawerOpen) {
            bookViewModel.updateEditBookStatus(false)
        }
    }

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = modifier
                            .height(64.dp)
                            .padding(start = 8.dp, end = 12.dp)
                            .fillMaxWidth()
                            .background(color = Color.Transparent)
                    ) {
                        // 右侧：分类选择器
                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .height(64.dp)      // 固定高度，避免pop显示位置异常
                            .align(Alignment.CenterEnd)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() }, indication = null
                                ) {
                                    bookViewModel.updateSheetType(ShowBottomSheetType.CATEGORY)
                                }
                                .padding(start = 0.dp, end = 20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()
                            ) {
                                Text(
                                    text = subCategory?.name ?: "全部类型", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                                )
                                Image(
                                    modifier = Modifier.padding(start = 6.dp), painter = painterResource(id = R.mipmap.icon_arrow_down_black), contentDescription = null
                                )
                            }
                        }

                        // 左侧：账本选择器
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(end = 10.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() }, indication = null
                                ) {
                                    onShowDrawer {
                                        BookDrawerWidget(data = books, isEditBook = uiState.isEditBook, onEditClick = {
                                            bookViewModel.updateEditBookEntity(it)
                                            bookViewModel.updateSheetType(ShowBottomSheetType.EDIT)
                                        }, onDeleteClick = {
                                            bookViewModel.updateSheetType(ShowBottomSheetType.DELETE)
                                        }, onToggleEdit = {
                                            bookViewModel.updateEditBookStatus(!uiState.isEditBook)
                                        }, onAddClick = {
                                            bookViewModel.updateSheetType(ShowBottomSheetType.ADD)
                                        }, onItemClick = {
                                            bookViewModel.updateSelectBook(it.id)
                                            scope.launch {
                                                delay(200)
                                                onCloseDrawer()
                                            }
                                        })
                                    }
                                }, text = curSelectedBook?.name ?: "未选择账本", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
            )
        }) { padding ->
        // 主内容区域
        LazyColumn(
            modifier = modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            // 头部：年月金额统计
            item {
                var totalIncome: Double
                var totalExpense: Double
                if (uiState.month <= 0) {
                    // 全年的数据
                    totalIncome = yearSummaryData?.totalIncome ?: 0.0
                    totalExpense = yearSummaryData?.totalExpense ?: 0.0
                } else {
                    // 月度数据
                    totalIncome = monthSummaryData?.monthTotalIncome ?: 0.0
                    totalExpense = monthSummaryData?.monthTotalExpense ?: 0.0
                }

                YearMonthAmountWidget(
                    totalIncome = totalIncome.toFloat(),
                    isFullYear = uiState.month <= 0,
                    totalExpense = totalExpense.toFloat(),
                    totalPlan = planEntity?.amount ?: 0.0f,
                    onYearMonthClick = {
                        bookViewModel.updateSheetType(ShowBottomSheetType.YEAR_MONTH)
                    },
                    uiState = uiState,
                    onItemClick = { type ->
                        when (type) {
                            TransactionAmountType.INCOME -> {
                                navController.navigate("${RoutePath.DashBoard.route}?year=${uiState.year}&month=${uiState.month}&type=${TransactionAmountType.INCOME.ordinal}")
                            }

                            TransactionAmountType.EXPENSE -> {
                                navController.navigate("${RoutePath.DashBoard.route}?year=${uiState.year}&month=${uiState.month}&type=${TransactionAmountType.EXPENSE.ordinal}")
                            }

                            TransactionAmountType.PLAN -> {
                                bookViewModel.updateSheetType(ShowBottomSheetType.MONTH_PLAN)
                            }

                            TransactionAmountType.EXCLUDED -> {}
                        }
                    })
            }

            // 月度明细列表（如果是月度视图）
            monthSummaryData?.groups?.let {
                items(monthSummaryData?.groups!!, key = { it.hashCode() }) { dateGroup ->
                    DailyItemWidget(item = dateGroup, onItemClick = { addQuickEntity ->
                        navController.navigate("${RoutePath.WatchAndEditQuick.route}?quickId=${addQuickEntity.quick.id}")
                    }, onDelete = { addQuickEntity ->
                        bookViewModel.deleteQuickDatabase(addQuickEntity.quick)
                    })
                }
            }

            // 年度明细列表（如果是年度视图）
            yearSummaryData?.monthList?.let {
                items(yearSummaryData?.monthList!!, key = { it.hashCode() }) { monthData ->
                    MonthItemWidget(item = monthData, onItemClick = { category ->
                        // 分类组点击：进入分类详情页
                        UserCache.categoryQuickList = category.transactions
                        navController.navigate(
                            "${RoutePath.DashBoardDetail.route}?title=${
                                bookViewModel.getCategoryDetailTitle(
                                    category.categoryType, category.monthDisplay, category.subCategory?.name ?: ""
                                )
                            }"
                        )
                    }, onMonthClick = { monthData ->
                        // 月份点击：可以扩展功能
                    })
                }
            }
        }

        // 弹窗和底部弹窗
        if (uiState.sheetType == ShowBottomSheetType.MONTH_PLAN) {
            NumberEditDialog(title = "设置本月预算", value = planEntity?.amount?.roundToString() ?: "", showSuffix = false, onDismissRequest = {
                bookViewModel.dismissBottomSheet()
            }, onConfirm = {
                bookViewModel.dismissBottomSheet()
                LogUtils.d("设置预算：$it")
                bookViewModel.updatePlanAmount(it.toFloat())
            })
        }

        if (uiState.sheetType == ShowBottomSheetType.EDIT) {
            EditDialog(title = "编辑账本", value = uiState.curEditBookEntity?.name ?: "", showSuffix = false, onDismissRequest = {
                bookViewModel.dismissBottomSheet()
            }, onConfirm = {
                bookViewModel.updateBookEntityDatabase(uiState.curEditBookEntity?.copy(name = it))
                bookViewModel.dismissBottomSheet()
                LogUtils.d("编辑账本：$it")
            })
        }

        if (uiState.sheetType == ShowBottomSheetType.ADD) {
            EditDialog(title = "添加账本", value = "", showSuffix = false, onDismissRequest = {
                bookViewModel.dismissBottomSheet()
            }, onConfirm = {
                bookViewModel.insertBookEntityDatabase(it)
                bookViewModel.dismissBottomSheet()
                LogUtils.d("添加账本：$it")
            })
        }

        if (uiState.sheetType == ShowBottomSheetType.DELETE) {
            ConfirmDialog(title = "删除该账本", onDismissRequest = {
                bookViewModel.dismissBottomSheet()
            }, onConfirm = {
                bookViewModel.deleteBookEntityDatabase(uiState.curEditBookEntity)
                bookViewModel.dismissBottomSheet()
            })
        }

        // 交易类型选择底部弹窗
        TransactionTypeBottomSheet(initial = subCategory, title = "选择类型", data = categories, visible = bookViewModel.showBottomSheet(ShowBottomSheetType.CATEGORY), onDismiss = {
            bookViewModel.dismissBottomSheet()
        }, onConfirm = {
            bookViewModel.dismissBottomSheet()
            bookViewModel.updateQueryCategory(it)
        }, onSettingClick = {
            bookViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditTransactionCategory.route)
        })

        // 年月选择底部弹窗
        YearMonthBottomSheet(
            year = uiState.year, month = uiState.month, visible = bookViewModel.showBottomSheet(ShowBottomSheetType.YEAR_MONTH), onDismiss = {
                bookViewModel.dismissBottomSheet()
            }) { year, month ->
            bookViewModel.dismissBottomSheet()
            bookViewModel.updateQueryDate(year, month)
            LogUtils.d("选中的年月${year}-${month}")
        }
    }
}

/**
 * 年月金额统计组件
 *
 * @param modifier 修饰符
 * @param isFullYear 是否为全年视图
 * @param totalIncome 总收入
 * @param totalExpense 总支出
 * @param totalPlan 总预算
 * @param uiState 页面UI状态
 * @param onYearMonthClick 年月选择点击回调
 * @param onItemClick 金额项点击回调
 */
@Composable fun YearMonthAmountWidget(
    modifier: Modifier = Modifier,
    isFullYear: Boolean = false,
    totalIncome: Float,
    totalExpense: Float,
    totalPlan: Float,
    uiState: BookUiState,
    onYearMonthClick: () -> Unit,
    onItemClick: (type: TransactionAmountType) -> Unit
) {
    // 渐变圆角边框容器
    GradientRoundedBoxWithStroke {
        Column {
            // 年月选择行
            Row(modifier = modifier
                .clip(RoundedCornerShape(25.dp))
                .clickable { onYearMonthClick() }
                .padding(start = 20.dp, top = 14.dp, bottom = 12.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatYearMonth(uiState.year, uiState.month), fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black
                )
                Image(
                    painter = painterResource(id = R.mipmap.icon_down), contentDescription = null, modifier = Modifier.padding(start = 4.dp)
                )
            }

            // 金额统计行
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 支出项
                val expenseEntity = AmountItemEntity(
                    if (isFullYear) "本年支出" else "本月支出", totalExpense, TransactionAmountType.EXPENSE
                )
                val incomeEntity = AmountItemEntity(
                    if (isFullYear) "本年收入" else "本月收入", totalIncome, TransactionAmountType.INCOME
                )

                TopAmountItemWidget(
                    item = expenseEntity, modifier = Modifier.weight(1f)
                ) {
                    onItemClick(it.type)
                }

                // 收入项
                TopAmountItemWidget(
                    item = incomeEntity, modifier = Modifier.weight(1f)
                ) {
                    onItemClick(it.type)
                }

                // 预算项（仅在月度视图中显示）
                AnimatedVisibility(
                    visible = !isFullYear, modifier = Modifier.weight(1f)
                ) {
                    val planEntity = AmountItemEntity("添加预算", totalPlan, TransactionAmountType.PLAN)
                    TopAmountItemWidget(item = planEntity) {
                        onItemClick(it.type)
                    }
                }
            }
        }
    }
}

/**
 * 账本侧边栏组件
 *
 * @param data 账本列表数据
 * @param isEditBook 是否为编辑模式
 * @param modifier 修饰符
 * @param onItemClick 账本项点击回调
 * @param onEditClick 编辑点击回调
 * @param onDeleteClick 删除点击回调
 * @param onToggleEdit 切换编辑模式回调
 * @param onAddClick 添加账本回调
 */
@Composable fun BookDrawerWidget(
    data: List<BookEntity>,
    isEditBook: Boolean,
    modifier: Modifier = Modifier,
    onItemClick: (BookEntity) -> Unit,
    onEditClick: (BookEntity) -> Unit,
    onDeleteClick: (BookEntity) -> Unit,
    onToggleEdit: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.7f)  // 占屏幕70%宽度
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.color_E3EBF5), Color.White
                    )
                )
            )
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp), horizontalArrangement = Arrangement.Absolute.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(start = 20.dp), text = "我的账本", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) {
                        onToggleEdit()
                    }, text = if (isEditBook) "保存" else "编辑", fontWeight = FontWeight.Normal, fontSize = 16.sp, color = colorResource(id = R.color.color_333333)
            )
        }

        // 账本列表
        LazyColumn(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
        ) {
            items(data, key = { book -> book.id }) { book ->
                BookItem(
                    isEditBook, book = book, onEditClick = onEditClick, onDeleteClick = onDeleteClick, onItemClick = onItemClick
                )
            }
        }

        // 添加账本按钮
        Box(modifier = Modifier.padding(20.dp)) {
            SelectableRoundedButton(
                modifier = Modifier.width(200.dp),
                text = "添加",
                selected = false,
                onClick = onAddClick,
                cornerSize = 27.dp,
                interaction = true,
                contentPadding = PaddingValues(horizontal = 47.dp, vertical = 15.dp),
                fontSize = 16.sp
            )
        }
    }
}

/**
 * 单个账本项组件
 *
 * @param isEditBook 是否为编辑模式
 * @param modifier 修饰符
 * @param book 账本实体
 * @param onItemClick 项点击回调
 * @param onEditClick 编辑点击回调
 * @param onDeleteClick 删除点击回调
 */
@Composable fun BookItem(
    isEditBook: Boolean, modifier: Modifier = Modifier, book: BookEntity, onItemClick: (BookEntity) -> Unit, onEditClick: (BookEntity) -> Unit, onDeleteClick: (BookEntity) -> Unit
) {
    GradientRoundedBoxWithStroke(
        colors = listOf(
            Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.2f)
        ), modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(book) }
            .height(54.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = book.name, fontSize = 16.sp, modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            // 编辑模式下显示编辑和删除按钮
            AnimatedVisibility(
                visible = isEditBook, enter = fadeIn(), exit = fadeOut()
            ) {
                Row {
                    // 编辑按钮
                    Image(
                        painter = painterResource(id = R.mipmap.icon_edit), contentDescription = null, modifier = Modifier
                            .padding(7.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() }, indication = null
                            ) {
                                onEditClick(book)
                            })

                    // 删除按钮
                    Image(
                        painter = painterResource(id = R.mipmap.icon_delete_red),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 7.dp, top = 7.dp, end = 20.dp, bottom = 7.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() }, indication = null
                            ) {
                                onDeleteClick(book)
                            })
                }
            }
        }
    }
}

/**
 * 顶部金额项组件
 *
 * @param modifier 修饰符
 * @param item 金额项实体
 * @param onItemClick 点击回调
 */
@Composable fun TopAmountItemWidget(
    modifier: Modifier = Modifier, item: AmountItemEntity, onItemClick: (AmountItemEntity) -> Unit
) {
    Column(
        modifier = modifier.clickable { onItemClick(item) }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 12.dp), text = item.amount.roundToString(), fontWeight = FontWeight.Medium, fontSize = 22.sp, color = Color.Black
        )
        Text(
            modifier = Modifier.padding(top = 3.dp, bottom = 16.dp), text = item.name, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colorResource(id = R.color.color_83878C)
        )
    }
}

@Composable @Preview(showBackground = true) fun BookDrawerWidgetPreview() {
    BookDrawerWidget(
        isEditBook = true,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .statusBarsPadding(),
        onEditClick = {},
        onDeleteClick = {},
        onToggleEdit = {},
        onAddClick = {},
        onItemClick = {},
        data = emptyList()
    )
}

@Composable @Preview(showBackground = true) fun BookHomePagePreview() {
    BookHomePage(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .statusBarsPadding(), navController = rememberNavController(), onShowDrawer = {}, onCloseDrawer = {}, isDrawerOpen = true
    )
}