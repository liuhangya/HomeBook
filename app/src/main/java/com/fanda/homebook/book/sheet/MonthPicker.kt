package com.fanda.homebook.book.sheet

import androidx.collection.IntList
import androidx.collection.mutableIntListOf
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs
import com.fanda.homebook.R

@Composable
fun MonthPicker(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    monthNames: List<String> = listOf(
        "1个月",
        "2个月",
        "3个月",
        "4个月",
        "6个月",
        "9个月",
        "12个月",
        "15个月",
        "18个月",
        "21个月",
        "24个月",
        "27个月",
        "30个月",
        "36个月",
        "48个月",
        "60个月",
    )
) {
    val coroutineScope = rememberCoroutineScope()
        val months = remember { listOf(1, 2, 3, 4, 6, 9, 12, 15, 18, 21, 24, 27, 30, 36, 48, 60) }

    val monthListState = rememberLazyListState(
        initialFirstVisibleItemIndex = months.indexOf(selectedMonth).coerceIn(0, months.lastIndex)
    )
    val density = LocalDensity.current

    // 将 dp 转换为 px
    val sizePx = with(density) { ITEM_HEIGHT.dp.toPx() }.toInt()

    // 实时计算中心选中项
    val centerSelectedMonth = remember(monthListState) {
        derivedStateOf {

            val month = calculateCenterSelectedItem(
                sizePx = sizePx,
                listState = monthListState,
                items = months,
                defaultSelected = selectedMonth
            )
            month
        }
    }.value

    val monthScrollInProgress by remember {
        derivedStateOf { monthListState.isScrollInProgress }
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
    LaunchedEffect(centerSelectedMonth) {
        if (centerSelectedMonth != selectedMonth) {
            onMonthSelected(centerSelectedMonth)
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
                .padding(horizontal = 24.dp)
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
                monthName = monthNames[index],
                isSelected = month == centerSelectedMonth,
                listState = listState,
                itemIndex = index,
                onClick = { onMonthClick(month) }
            )
        }
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
                color = if (isSelected) Color.Black
                else colorResource(id = R.color.color_83878C)
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
    visibleItems: List<LazyListItemInfo>
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
    visibleItems: List<LazyListItemInfo>,
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
private const val NORMAL_FONT_SIZE = 17
private const val SELECTED_FONT_SIZE = 19

@Composable
@Preview(showBackground = true)
fun MonthPickerDemo() {
    val currentDate = LocalDate.now()
    var selectedMonth by remember { mutableIntStateOf(currentDate.monthValue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "当前选择: ${selectedMonth}月",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        MonthPicker(
            selectedMonth = selectedMonth,
            onMonthSelected = { month ->
                selectedMonth = month
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}