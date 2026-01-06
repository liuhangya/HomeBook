package com.fanda.homebook.book.sheet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearMonthPicker(
    selectedYear: Int,
    selectedMonth: Int,
    onYearMonthSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    startYear: Int = 2000,
    endYear: Int = 2030,
    monthNames: List<String> = listOf(
        "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"
    )
) {
    val coroutineScope = rememberCoroutineScope()
    val years = remember { (startYear..endYear).toList() }
    val months = remember { (1..12).toList() }

    // 使用 LazyListState
    val yearListState = rememberLazyListState(
        initialFirstVisibleItemIndex = years.indexOf(selectedYear).coerceIn(0, years.lastIndex)
    )
    val monthListState = rememberLazyListState(
        initialFirstVisibleItemIndex = months.indexOf(selectedMonth).coerceIn(0, months.lastIndex)
    )
    val density = LocalDensity.current

    // 将 dp 转换为 px
    val sizePx = with(density) { ITEM_HEIGHT.dp.toPx() }.toInt()

    // 实时计算中心选中项
    val (centerSelectedYear, centerSelectedMonth) = remember(yearListState, monthListState) {
        derivedStateOf {
            val year = calculateCenterSelectedItem(
                sizePx =sizePx,
                listState = yearListState,
                items = years,
                defaultSelected = selectedYear
            )
            val month = calculateCenterSelectedItem(
                sizePx =sizePx,
                listState = monthListState,
                items = months,
                defaultSelected = selectedMonth
            )
            Pair(year, month)
        }
    }.value

    // 监听滚动状态
    val yearScrollInProgress by remember {
        derivedStateOf { yearListState.isScrollInProgress }
    }

    val monthScrollInProgress by remember {
        derivedStateOf { monthListState.isScrollInProgress }
    }

    // 当滚动停止时，检查是否需要吸附
    LaunchedEffect(yearScrollInProgress) {
        if (!yearScrollInProgress) {
            val visibleItems = yearListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                // 检查当前是否已经居中
                val isAlreadyCentered = checkIfItemCentered(
                    listState = yearListState,
                    targetItemIndex = years.indexOf(centerSelectedYear),
                    visibleItems = visibleItems
                )

                // 如果不居中，才进行吸附滚动
                if (!isAlreadyCentered) {
                    val targetIndex = years.indexOf(centerSelectedYear)
                    if (targetIndex != -1) {
                        yearListState.animateScrollToItem(targetIndex)
                    }
                }
            }
        }
    }

    LaunchedEffect(monthScrollInProgress) {
        if (!monthScrollInProgress) {
            val visibleItems = monthListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                // 检查当前是否已经居中
                val isAlreadyCentered = checkIfItemCentered(
                    listState = monthListState,
                    targetItemIndex = months.indexOf(centerSelectedMonth),
                    visibleItems = visibleItems
                )

                // 如果不居中，才进行吸附滚动
                if (!isAlreadyCentered) {
                    val targetIndex = months.indexOf(centerSelectedMonth)
                    if (targetIndex != -1) {
                        monthListState.animateScrollToItem(targetIndex)
                    }
                }
            }
        }
    }

    // 当中心项变化时更新外部状态
    LaunchedEffect(centerSelectedYear, centerSelectedMonth) {
        if (centerSelectedYear != selectedYear || centerSelectedMonth != selectedMonth) {
            onYearMonthSelected(centerSelectedYear, centerSelectedMonth)
        }
    }

    Box(
        modifier = modifier
            .height(PICKER_HEIGHT.dp)
    ) {
        // 选中指示器（中间高亮区域）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((ITEM_HEIGHT * SELECTED_SCALE).dp)
                .align(Alignment.Center)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 年份选择列
            YearLazyColumn(
                modifier = Modifier.weight(1f),
                years = years,
                centerSelectedYear = centerSelectedYear,
                listState = yearListState,
                onYearClick = { year ->
                    coroutineScope.launch {
                        val index = years.indexOf(year)
                        yearListState.animateScrollToItem(index)
                    }
                }
            )

            // 月份选择列
            MonthLazyColumn(
                modifier = Modifier.weight(1f),
                months = months,
                monthNames = monthNames,
                centerSelectedMonth = centerSelectedMonth,
                listState = monthListState,
                onMonthClick = { month ->
                    coroutineScope.launch {
                        val index = months.indexOf(month)
                        monthListState.animateScrollToItem(index)
                    }
                }
            )
        }
    }
}

@Composable
private fun YearLazyColumn(
    modifier: Modifier = Modifier,
    years: List<Int>,
    centerSelectedYear: Int,
    listState: LazyListState,
    onYearClick: (Int) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = (PICKER_HEIGHT / 2 - ITEM_HEIGHT / 2).dp)
    ) {
        itemsIndexed(years) { index, year ->
            YearItem(
                year = year,
                isSelected = year == centerSelectedYear,
                listState = listState,
                itemIndex = index,
                onClick = { onYearClick(year) }
            )
        }
    }
}

@Composable
private fun MonthLazyColumn(
    modifier: Modifier = Modifier,
    months: List<Int>,
    monthNames: List<String>,
    centerSelectedMonth: Int,
    listState: LazyListState,
    onMonthClick: (Int) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = (PICKER_HEIGHT / 2 - ITEM_HEIGHT / 2).dp)
    ) {
        itemsIndexed(months) { index, month ->
            MonthItem(
                month = month,
                monthName = monthNames.getOrElse(month - 1) { "${month}月" },
                isSelected = month == centerSelectedMonth,
                listState = listState,
                itemIndex = index,
                onClick = { onMonthClick(month) }
            )
        }
    }
}

@Composable
private fun YearItem(
    year: Int,
    isSelected: Boolean,
    listState: LazyListState,
    itemIndex: Int,
    onClick: () -> Unit
) {
    val visibleItems = listState.layoutInfo.visibleItemsInfo

    // 计算中心位置
    val viewportHeight = listState.layoutInfo.viewportEndOffset
    val center = viewportHeight / 2

    val distanceFromCenter = calculateDistanceFromCenter(visibleItems, itemIndex, center)
    val scale = calculateScale(distanceFromCenter)
    val alpha = calculateAlpha(distanceFromCenter)

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) SELECTED_SCALE else scale,
        label = "yearScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(ITEM_HEIGHT.dp)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                this.alpha = if (isSelected) 1f else alpha
            }
//            .clickable { onClick() }
    ) {
        Text(
            text = year.toString(),
            style = TextStyle(
                fontSize = if (isSelected) SELECTED_FONT_SIZE.sp else NORMAL_FONT_SIZE.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
        )
    }
}

@Composable
private fun MonthItem(
    month: Int,
    monthName: String,
    isSelected: Boolean,
    listState: LazyListState,
    itemIndex: Int,
    onClick: () -> Unit
) {
    val visibleItems = listState.layoutInfo.visibleItemsInfo

    // 计算中心位置
    val viewportHeight = listState.layoutInfo.viewportEndOffset
    val center = viewportHeight / 2

    val distanceFromCenter = calculateDistanceFromCenter(visibleItems, itemIndex, center)
    val scale = calculateScale(distanceFromCenter)
    val alpha = calculateAlpha(distanceFromCenter)

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) SELECTED_SCALE else scale,
        label = "monthScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(ITEM_HEIGHT.dp)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                this.alpha = if (isSelected) 1f else alpha
            }
//            .clickable { onClick() }
    ) {
        Text(
            text = monthName,
            style = TextStyle(
                fontSize = if (isSelected) SELECTED_FONT_SIZE.sp else NORMAL_FONT_SIZE.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
        )
    }
}

// 辅助函数：计算中间选中项
private fun <T> calculateCenterSelectedItem(
    sizePx: Int,
    listState: LazyListState,
    items: List<T>,
    defaultSelected: T
): T {
    val visibleItems = listState.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return defaultSelected

    // 计算中心位置：视口高度/2
    val viewportHeight = listState.layoutInfo.viewportEndOffset
    val center = viewportHeight / 2 - sizePx

    var selectedIndex = visibleItems.first().index
    var minDistance = Float.MAX_VALUE

    for (item in visibleItems) {
        val itemCenter = item.offset + item.size / 2
        val distance = abs(itemCenter - center).toFloat()
        if (distance < minDistance) {
            minDistance = distance
            selectedIndex = item.index
        }
    }

    return if (selectedIndex in items.indices) {
        items[selectedIndex]
    } else {
        defaultSelected
    }
}

// 辅助函数：检查项是否已经居中
private fun checkIfItemCentered(
    listState: LazyListState,
    targetItemIndex: Int,
    visibleItems: List<androidx.compose.foundation.lazy.LazyListItemInfo>
): Boolean {
    if (targetItemIndex == -1) return false

    // 找到目标项
    val targetItem = visibleItems.find { it.index == targetItemIndex } ?: return false

    // 计算中心位置
    val viewportHeight = listState.layoutInfo.viewportEndOffset
    val center = viewportHeight / 2

    // 计算目标项中心位置
    val itemCenter = targetItem.offset + targetItem.size / 2

    // 判断是否居中（允许一定的误差范围，比如ITEM_HEIGHT的1/4）
    val tolerance = targetItem.size / 4
    return abs(itemCenter - center) <= tolerance
}

// 辅助函数：计算距离中心的位置
private fun calculateDistanceFromCenter(
    visibleItems: List<androidx.compose.foundation.lazy.LazyListItemInfo>,
    itemIndex: Int,
    center: Int
): Float {
    return visibleItems.find { it.index == itemIndex }?.let { item ->
        val itemCenter = item.offset + item.size / 2
        abs(itemCenter - center).toFloat()
    } ?: Float.MAX_VALUE
}

// 辅助函数：计算缩放比例
private fun calculateScale(distanceFromCenter: Float): Float {
    return when {
        distanceFromCenter < ITEM_HEIGHT -> 1.2f - (distanceFromCenter / ITEM_HEIGHT) * 0.4f
        else -> 0.8f
    }.coerceIn(0.8f, 1.2f)
}

// 辅助函数：计算透明度
private fun calculateAlpha(distanceFromCenter: Float): Float {
    return when {
        distanceFromCenter < ITEM_HEIGHT * 2 -> 1f - (distanceFromCenter / (ITEM_HEIGHT * 2)) * 0.5f
        else -> 0.5f
    }.coerceIn(0.5f, 1f)
}

// 常量
private const val ITEM_HEIGHT = 50
private const val PICKER_HEIGHT = 300
private const val SELECTED_SCALE = 1.3f
private const val NORMAL_FONT_SIZE = 16
private const val SELECTED_FONT_SIZE = 20

@Composable
@Preview(showBackground = true)
fun YearMonthPickerDemo() {
    var selectedYear by remember { mutableStateOf(2024) }
    var selectedMonth by remember { mutableStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "当前选择: ${selectedYear}年${selectedMonth}月",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        YearMonthPicker(
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            onYearMonthSelected = { year, month ->
                selectedYear = year
                selectedMonth = month
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}