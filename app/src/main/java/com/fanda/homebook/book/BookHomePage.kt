package com.fanda.homebook.book

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.book.sheet.TransactionTypeBottomSheet
import com.fanda.homebook.book.sheet.YearMonthBottomSheet
import com.fanda.homebook.book.state.BookUiState
import com.fanda.homebook.book.ui.DailyItemWidget
import com.fanda.homebook.book.viewmodel.BookViewModel
import com.fanda.homebook.components.ConfirmDialog
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.NumberEditDialog
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.entity.AmountItemEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.entity.TransactionAmountType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.UserCache
import com.fanda.homebook.tools.formatYearMonth
import com.fanda.homebook.tools.roundToString
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

    val uiState by bookViewModel.uiState.collectAsState()
    val curSelectedBook by bookViewModel.curSelectedBook.collectAsState()
    val books by bookViewModel.books.collectAsState()
    val categories by bookViewModel.categories.collectAsState()
    val subCategory by bookViewModel.subCategory.collectAsState()
    val transactionDayGroupedData by bookViewModel.transactionDayGroupedData.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    LogUtils.i("uiState: $uiState")
    LogUtils.i("transactionDayGroupedData: $transactionDayGroupedData")

    val scope = rememberCoroutineScope()

    // 监听生命周期变化
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // 可见时刷新数据
                    LogUtils.d("可见时刷新数据 BookHomePage: ON_RESUME")
                    bookViewModel.refreshBooks()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isDrawerOpen) {
        if (!isDrawerOpen) {
            // 侧边栏关闭时，取消编辑
            bookViewModel.updateEditBookStatus(false)
        }
    }

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopAppBar(
            title = {
                Box(
                    modifier = modifier
                        .height(64.dp)
                        .padding(start = 8.dp, end = 12.dp)
                        .fillMaxWidth()
                        .background(color = Color.Transparent)
                ) {

                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(64.dp)      // 这里要固定高度，不然 pop 显示位置异常
                        .align(Alignment.CenterEnd)
                            .clickable(
                                // 去掉默认的点击效果
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
        LazyColumn(
            modifier = modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            item {
                TopTotalAmountWidget(
                    totalIncome = transactionDayGroupedData?.monthTotalIncome?.toFloat() ?: 0.0f,
                    totalExpense = transactionDayGroupedData?.monthTotalExpense?.toFloat() ?: 0.0f,
                    totalPlan = UserCache.planAmount,
                    onYearMonthClick = {
                        bookViewModel.updateSheetType(ShowBottomSheetType.YEAR_MONTH)
                    },
                    uiState = uiState,
                    onItemClick = { type ->
                        when (type) {
                            TransactionAmountType.INCOME -> {
                                navController.navigate("${RoutePath.DashBoar.route}?year=${uiState.year}&month=${uiState.month}&type=${TransactionAmountType.INCOME.ordinal}")
                            }

                            TransactionAmountType.EXPENSE -> {
                                navController.navigate("${RoutePath.DashBoar.route}?year=${uiState.year}&month=${uiState.month}&type=${TransactionAmountType.EXPENSE.ordinal}")
                            }

                            TransactionAmountType.PLAN -> {
                                bookViewModel.updateSheetType(ShowBottomSheetType.MONTH_PLAN)
                            }

                            TransactionAmountType.EXCLUDED -> {}
                        }
                    })
            }

            transactionDayGroupedData?.groups?.let {
                items(transactionDayGroupedData?.groups!!, key = { it.hashCode() }) {
                    DailyItemWidget(item = it)
                }
            }

        }

        if (uiState.sheetType == ShowBottomSheetType.MONTH_PLAN) {
            NumberEditDialog(title = "设置本月预算", value = UserCache.planAmount.roundToString(), showSuffix = false, onDismissRequest = {
                bookViewModel.dismissBottomSheet()
            }, onConfirm = {
                bookViewModel.dismissBottomSheet()
                LogUtils.d("设置预算：$it")
                UserCache.planAmount = it.toFloat()
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
        TransactionTypeBottomSheet(initial = subCategory, title = "选择类型", data = categories, visible = bookViewModel.showBottomSheet(ShowBottomSheetType.CATEGORY), onDismiss = {
            bookViewModel.dismissBottomSheet()
        }, onConfirm = {
            bookViewModel.dismissBottomSheet()
            bookViewModel.updateQueryCategory(it)
        }, onSettingClick = {
            bookViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditTransactionCategory.route)
        })

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

@Composable fun TopTotalAmountWidget(
    modifier: Modifier = Modifier, totalIncome: Float, totalExpense: Float, totalPlan: Float, uiState: BookUiState, onYearMonthClick: () -> Unit, onItemClick: (type: TransactionAmountType) -> Unit
) {
    GradientRoundedBoxWithStroke {
        Column {
            Row(modifier = modifier
                .clip(RoundedCornerShape(25.dp))
                .clickable {
                    onYearMonthClick()
                }
                .padding(start = 20.dp, top = 14.dp, bottom = 12.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatYearMonth(uiState.year, uiState.month), fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black
                )
                Image(
                    painter = painterResource(id = R.mipmap.icon_down), contentDescription = null, modifier = Modifier.padding(start = 4.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.month <= 0) {
                    // 全年的
                } else {
                    // 每月的
                    val expenseEntity = AmountItemEntity("本月支出", totalExpense, TransactionAmountType.EXPENSE)
                    val incomeEntity = AmountItemEntity("本月收入", totalIncome, TransactionAmountType.INCOME)
                    val planEntity = AmountItemEntity("添加预算", totalPlan, TransactionAmountType.PLAN)
                    TopAmountItemWidget(item = expenseEntity, modifier = Modifier.weight(1f)) {
                        onItemClick(it.type)
                    }
                    TopAmountItemWidget(item = incomeEntity, modifier = Modifier.weight(1f)) {
                        onItemClick(it.type)
                    }
                    TopAmountItemWidget(item = planEntity, modifier = Modifier.weight(1f)) {
                        onItemClick(it.type)
                    }
                }
            }
        }
    }
}


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
            .fillMaxWidth(0.7f)
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.color_E3EBF5), Color.White
                    )
                )
            )
    ) {
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
        LazyColumn(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
        ) {
            items(data, key = { book -> book.id }) { book ->
                BookItem(
                    isEditBook, book = book, onEditClick = onEditClick, onDeleteClick = onDeleteClick, onItemClick = onItemClick
                )
            }
        }
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
            AnimatedVisibility(visible = isEditBook, enter = fadeIn(), exit = fadeOut()) {
                Row {
                    Image(
                        painter = painterResource(id = R.mipmap.icon_edit), contentDescription = null, modifier = Modifier
                            .padding(7.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() }, indication = null
                            ) {
                                onEditClick(book)
                            })

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


@Composable fun TopAmountItemWidget(
    modifier: Modifier = Modifier, item: AmountItemEntity, onItemClick: (AmountItemEntity) -> Unit
) {
    Column(
        modifier = modifier.clickable {
            onItemClick(item)
        }, horizontalAlignment = Alignment.CenterHorizontally
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
            .statusBarsPadding(), navController = rememberNavController(), onShowDrawer = {}, onCloseDrawer = {}, isDrawerOpen = true)
}