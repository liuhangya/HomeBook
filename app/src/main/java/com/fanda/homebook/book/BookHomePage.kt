package com.fanda.homebook.book

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.book.sheet.TransactionTypeBottomSheet
import com.fanda.homebook.book.sheet.YearMonthPicker
import com.fanda.homebook.book.ui.DailyItemWidget
import com.fanda.homebook.components.ConfirmDialog
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.AmountItemEntity
import com.fanda.homebook.entity.TransactionType
import com.fanda.homebook.quick.sheet.SheetTitleWidget
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookHomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    onShowDrawer: (@Composable () -> Unit) -> Unit,
    onCloseDrawer: () -> Unit
) {
    var showSelectCategoryBottomSheet by remember { mutableStateOf(false) }
    var showMonthPlanDialog by remember { mutableStateOf(false) }
    var showDeleteBookDialog by remember { mutableStateOf(false) }
    var showEditBookDialog by remember { mutableStateOf(false) }
    var showAddBookDialog by remember { mutableStateOf(false) }
    var isEditBook by remember { mutableStateOf(false) }
    var curCategory by remember { mutableStateOf("全部类型") }
    var curBookName by remember { mutableStateOf("居家生活") }
    var curEditBookName by remember { mutableStateOf("") }
    var planAmount by remember { mutableFloatStateOf(0f) }
    var date by remember {
        mutableStateOf(
            convertMillisToDate(
                System.currentTimeMillis(),
                "yyyy年MM月"
            )
        )
    }

    val scope = rememberCoroutineScope()

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
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                showSelectCategoryBottomSheet = true
                            }
                            .padding(start = 0.dp, end = 20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                text = curCategory,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            Image(
                                modifier = Modifier.padding(start = 6.dp),
                                painter = painterResource(id = R.mipmap.icon_arrow_down_black),
                                contentDescription = null
                            )
                        }

                    }

                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(end = 10.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onShowDrawer {
                                    BookDrawerWidget(isEditBook = isEditBook, onEditClick = {
                                        curEditBookName = it
                                        showEditBookDialog = true
                                    }, onDeleteClick = {
                                        showDeleteBookDialog = true
                                    }, onToggleEdit = {
                                        isEditBook = !isEditBook
                                    }, onAddClick = {
                                        showAddBookDialog = true
                                    }, onItemClick = {
                                        curBookName = it
                                        scope.launch {
                                            delay(200)
                                            onCloseDrawer()
                                        }
                                    })
                                }
                            },
                        text = curBookName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }

            },
            colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
        )
    }) { padding ->
        LazyColumn(
            modifier = modifier.padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            item {
                GradientRoundedBoxWithStroke {
                    Column {
                        Row(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    LogUtils.d("点击了日期")
                                }
                                .padding(start = 20.dp, top = 14.dp, bottom = 12.dp, end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = date,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Image(
                                painter = painterResource(id = R.mipmap.icon_down),
                                contentDescription = null,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LocalDataSource.amountItemList.forEach { item ->
                                TopAmountItemWidget(item = item, modifier = Modifier.weight(1f)) {
                                    when (item.type) {
                                        TransactionType.INCOME -> {

                                        }

                                        TransactionType.EXPENSE -> {

                                        }

                                        TransactionType.PLAN -> {
                                            showMonthPlanDialog = true
                                        }

                                        TransactionType.EXCLUDED -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
            items(LocalDataSource.dailyListData, key = { it.id }) {
                DailyItemWidget(item = it)
            }
        }
    }

    if (showMonthPlanDialog) {
        EditDialog(
            title = "设置本月预算",
            value = planAmount.toString(),
            showSuffix = false,
            onDismissRequest = {
                showMonthPlanDialog = false
            },
            onConfirm = {
                showMonthPlanDialog = false
                LogUtils.d("设置预算：$it")
                planAmount = it.toFloat()
            })
    }
    if (showEditBookDialog) {
        EditDialog(
            title = "账本名称",
            value = curEditBookName,
            showSuffix = false,
            onDismissRequest = {
                showEditBookDialog = false
            },
            onConfirm = {
                showEditBookDialog = false
                LogUtils.d("编辑名称：$it")
            })
    }
    if (showAddBookDialog) {
        EditDialog(title = "账本名称", value = "", showSuffix = false, onDismissRequest = {
            showAddBookDialog = false
        }, onConfirm = {
            showAddBookDialog = false
            LogUtils.d("添加名称：$it")
        })
    }
    if (showDeleteBookDialog) {
        ConfirmDialog(title = "删除该账本", onDismissRequest = {
            showDeleteBookDialog = false
        }, onConfirm = {
            showDeleteBookDialog = false
            LogUtils.d("删除账本")
        })
    }
    TransactionTypeBottomSheet(
        initial = curCategory,
        title = "选择类型",
        visible = showSelectCategoryBottomSheet,
        onDismiss = {
            showSelectCategoryBottomSheet = false
        },
        onConfirm = {
            showSelectCategoryBottomSheet = false
        },
        onSettingClick = {
            showSelectCategoryBottomSheet = false
        })

    var selectedYear by remember { mutableStateOf(2024) }
    var selectedMonth by remember { mutableStateOf(5) }

    CustomBottomSheet(visible = true, onDismiss =   {}) {
        Column() {
            SheetTitleWidget(title = "选择时间") {
            }
            YearMonthPicker(
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                onYearMonthSelected = { year, month ->
                    selectedYear = year
                    selectedMonth = month
                    LogUtils.d("选择：$year-$month")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

    }

}



@Composable
fun BookDrawerWidget(
    isEditBook: Boolean,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onToggleEdit: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.7f)
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.color_E3EBF5),
                        Color.White
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(start = 20.dp),
                text = "我的账本",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onToggleEdit()
                    },
                text = if (isEditBook) "保存" else "编辑",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = colorResource(id = R.color.color_333333)
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
        ) {
            items(LocalDataSource.bookList, key = { book -> book }) { book ->
                BookItem(
                    isEditBook,
                    name = book,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    onItemClick = onItemClick
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

@Composable
fun BookItem(
    isEditBook: Boolean,
    modifier: Modifier = Modifier,
    name: String,
    onItemClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    GradientRoundedBoxWithStroke(
        colors = listOf(
            Color.White.copy(alpha = 0.4f),
            Color.White.copy(alpha = 0.2f)
        ), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onItemClick(name) }
                .height(54.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = isEditBook, enter = fadeIn(), exit = fadeOut()) {
                Row {
                    Image(
                        painter = painterResource(id = R.mipmap.icon_edit),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(7.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onEditClick(name)
                            })

                    Image(
                        painter = painterResource(id = R.mipmap.icon_delete_red),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 7.dp, top = 7.dp, end = 20.dp, bottom = 7.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onDeleteClick(name)
                            })

                }
            }

        }
    }
}


@Composable
fun TopAmountItemWidget(
    modifier: Modifier = Modifier,
    item: AmountItemEntity,
    onItemClick: (AmountItemEntity) -> Unit
) {
    Column(
        modifier = modifier.clickable {
            onItemClick(item)
        }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = item.amount.toString(),
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            color = Color.Black
        )
        Text(
            modifier = Modifier.padding(top = 3.dp, bottom = 16.dp),
            text = item.name,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = colorResource(id = R.color.color_83878C)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun BookDrawerWidgetPreview() {
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
        onItemClick = {})
}


@Composable
@Preview(showBackground = true)
fun BookHomePagePreview() {
    BookHomePage(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .statusBarsPadding(),
        navController = rememberNavController(),
        onShowDrawer = {},
        onCloseDrawer = {})
}