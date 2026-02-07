package com.fanda.homebook.book.sheet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.fanda.homebook.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs

/**
 * 月份选择器组件
 * 实现一个滚轮式的月份选择器，支持滚动吸附和视觉缩放效果
 *
 * @param selectedMonth 当前选中的月份值
 * @param onMonthSelected 月份选中回调
 * @param modifier 修饰符
 * @param monthNames 月份显示名称列表，默认显示为"N个月"格式
 */
@Composable fun MonthPicker(
    selectedMonth: Int, onMonthSelected: (Int) -> Unit, modifier: Modifier = Modifier, monthNames: List<String> = listOf(
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
    // 协程作用域，用于执行滚动动画
    val coroutineScope = rememberCoroutineScope()
    // 实际的月份数值列表
    val months = remember { listOf(1, 2, 3, 4, 6, 9, 12, 15, 18, 21, 24, 27, 30, 36, 48, 60) }

    // 初始化LazyListState，设置初始可见项为当前选中的月份
    val monthListState = rememberLazyListState(
        initialFirstVisibleItemIndex = months.indexOf(selectedMonth).coerceIn(0, months.lastIndex)
    )
    val density = LocalDensity.current

    // 将ITEM_HEIGHT从dp转换为px，用于精确计算位置
    val sizePx = with(density) { ITEM_HEIGHT.dp.toPx() }.toInt()

    // 实时计算当前位于中心位置的选中项
    val centerSelectedMonth = remember(monthListState) {
        derivedStateOf {
            val month = calculateCenterSelectedItem(
                sizePx = sizePx, listState = monthListState, items = months, defaultSelected = selectedMonth
            )
            month
        }
    }.value

    // 监听滚动是否在进行中
    val monthScrollInProgress by remember {
        derivedStateOf { monthListState.isScrollInProgress }
    }

    // 滚动停止后的吸附效果：确保选中项精确居中
    LaunchedEffect(monthScrollInProgress) {
        if (!monthScrollInProgress) {
            val visibleItems = monthListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                // 检查当前选中项是否已经居中
                val isAlreadyCentered = checkIfItemCentered(
                    listState = monthListState, targetItemIndex = months.indexOf(centerSelectedMonth), visibleItems = visibleItems
                )

                // 如果不居中，执行滚动动画使其居中
                if (!isAlreadyCentered) {
                    val targetIndex = months.indexOf(centerSelectedMonth)
                    if (targetIndex != -1) {
                        monthListState.animateScrollToItem(targetIndex)
                    }
                }
            }
        }
    }

    // 当中心选中项变化时，通知外部状态更新
    LaunchedEffect(centerSelectedMonth) {
        if (centerSelectedMonth != selectedMonth) {
            onMonthSelected(centerSelectedMonth)
        }
    }

    // 主容器
    Box(
        modifier = modifier.height(PICKER_HEIGHT.dp)
    ) {
        // 选中指示器：中间高亮背景区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height((ITEM_HEIGHT * SELECTED_SCALE).dp)
                .align(Alignment.Center)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = MaterialTheme.shapes.medium
                )
        )

        // 水平布局容器
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 月份选择列
            MonthLazyColumn(
                modifier = Modifier.weight(1f), months = months, monthNames = monthNames, centerSelectedMonth = centerSelectedMonth, listState = monthListState, onMonthClick = { month ->
                    // 点击月份项时滚动到该位置
                    coroutineScope.launch {
                        val index = months.indexOf(month)
                        monthListState.animateScrollToItem(index)
                    }
                })
        }
    }
}

/**
 * 月份列表列
 *
 * @param modifier 修饰符
 * @param months 月份数值列表
 * @param monthNames 月份显示名称列表
 * @param centerSelectedMonth 当前中心选中的月份
 * @param listState LazyList状态
 * @param onMonthClick 月份项点击回调
 */
@Composable private fun MonthLazyColumn(
    modifier: Modifier = Modifier, months: List<Int>, monthNames: List<String>, centerSelectedMonth: Int, listState: LazyListState, onMonthClick: (Int) -> Unit
) {
    LazyColumn(
        state = listState, modifier = modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally,
        // 设置内容内边距，确保第一个和最后一个项可以滚动到中心位置
        contentPadding = PaddingValues(vertical = (PICKER_HEIGHT / 2 - ITEM_HEIGHT / 2).dp)
    ) {
        // 遍历月份列表创建项
        itemsIndexed(months) { index, month ->
            MonthItem(
                month = month, monthName = monthNames[index], isSelected = month == centerSelectedMonth, listState = listState, itemIndex = index, onClick = { onMonthClick(month) })
        }
    }
}

/**
 * 月份项
 *
 * @param month 月份数值
 * @param monthName 月份显示名称
 * @param isSelected 是否被选中
 * @param listState LazyList状态
 * @param itemIndex 项在列表中的索引
 * @param onClick 点击回调
 */
@Composable private fun MonthItem(
    month: Int, monthName: String, isSelected: Boolean, listState: LazyListState, itemIndex: Int, onClick: () -> Unit
) {
    val visibleItems = listState.layoutInfo.visibleItemsInfo

    // 计算视口中心位置
    val viewportHeight = listState.layoutInfo.viewportEndOffset
    val center = viewportHeight / 2

    // 计算当前项距离中心的距离
    val distanceFromCenter = calculateDistanceFromCenter(visibleItems, itemIndex, center)
    // 根据距离计算缩放比例和透明度
    val scale = calculateScale(distanceFromCenter)
    val alpha = calculateAlpha(distanceFromCenter)

    // 缩放动画：选中项使用固定缩放值，非选中项根据距离动态缩放
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) SELECTED_SCALE else scale, label = "monthScale"
    )

    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .height(ITEM_HEIGHT.dp)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                this.alpha = if (isSelected) 1f else alpha
            }) {
        // 月份文本
        Text(
            text = monthName, style = TextStyle(
                fontSize = if (isSelected) SELECTED_FONT_SIZE.sp else NORMAL_FONT_SIZE.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.Black
                else colorResource(id = R.color.color_83878C)
            )
        )
    }
}

/**
 * 计算当前位于中心位置的选中项
 *
 * @param sizePx 单个项的高度（像素）
 * @param listState LazyList状态
 * @param items 数据项列表
 * @param defaultSelected 默认选中项
 * @return 当前位于中心位置的项
 */
private fun <T> calculateCenterSelectedItem(
    sizePx: Int, listState: LazyListState, items: List<T>, defaultSelected: T
): T {
    val visibleItems = listState.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return defaultSelected

    // 计算中心位置：视口高度的一半减去项高度的一半
    val viewportHeight = listState.layoutInfo.viewportEndOffset
    val center = viewportHeight / 2 - sizePx

    var selectedIndex = visibleItems.first().index
    var minDistance = Float.MAX_VALUE

    // 遍历所有可见项，找到距离中心最近的项
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

/**
 * 检查指定项是否已经居中
 *
 * @param listState LazyList状态
 * @param targetItemIndex 目标项索引
 * @param visibleItems 可见项列表
 * @return 如果目标项已居中返回true，否则返回false
 */
private fun checkIfItemCentered(
    listState: LazyListState, targetItemIndex: Int, visibleItems: List<LazyListItemInfo>
): Boolean {
    if (targetItemIndex == -1) return false

    // 在可见项中查找目标项
    val targetItem = visibleItems.find { it.index == targetItemIndex } ?: return false

    // 计算中心位置
    val viewportHeight = listState.layoutInfo.viewportEndOffset
    val center = viewportHeight / 2

    // 计算目标项的中心位置
    val itemCenter = targetItem.offset + targetItem.size / 2

    // 判断是否居中（允许ITEM_HEIGHT/4的误差范围）
    val tolerance = targetItem.size / 4
    return abs(itemCenter - center) <= tolerance
}

/**
 * 计算指定项距离视口中心的距离
 *
 * @param visibleItems 可见项列表
 * @param itemIndex 项索引
 * @param center 视口中心位置
 * @return 距离中心的像素距离，如果项不可见返回Float.MAX_VALUE
 */
private fun calculateDistanceFromCenter(
    visibleItems: List<LazyListItemInfo>, itemIndex: Int, center: Int
): Float {
    return visibleItems.find { it.index == itemIndex }?.let { item ->
        val itemCenter = item.offset + item.size / 2
        abs(itemCenter - center).toFloat()
    } ?: Float.MAX_VALUE
}

/**
 * 根据距离中心的距离计算缩放比例
 * 距离越近缩放越大，距离越远缩放越小
 *
 * @param distanceFromCenter 距离中心的距离（像素）
 * @return 缩放比例，范围[0.8, 1.2]
 */
private fun calculateScale(distanceFromCenter: Float): Float {
    return when {
        distanceFromCenter < ITEM_HEIGHT -> 1.2f - (distanceFromCenter / ITEM_HEIGHT) * 0.4f
        else -> 0.8f
    }.coerceIn(0.8f, 1.2f)
}

/**
 * 根据距离中心的距离计算透明度
 * 距离越近透明度越高，距离越远透明度越低
 *
 * @param distanceFromCenter 距离中心的距离（像素）
 * @return 透明度值，范围[0.5, 1.0]
 */
private fun calculateAlpha(distanceFromCenter: Float): Float {
    return when {
        distanceFromCenter < ITEM_HEIGHT * 2 -> 1f - (distanceFromCenter / (ITEM_HEIGHT * 2)) * 0.5f
        else -> 0.5f
    }.coerceIn(0.5f, 1f)
}

// 组件常量定义
private const val ITEM_HEIGHT = 50          // 单个项的高度
private const val PICKER_HEIGHT = 300       // 选择器总高度
private const val SELECTED_SCALE = 1.3f     // 选中项的缩放比例
private const val NORMAL_FONT_SIZE = 17     // 普通文本字号
private const val SELECTED_FONT_SIZE = 19   // 选中文本字号

/**
 * 预览函数
 */
@Composable @Preview(showBackground = true) fun MonthPickerDemo() {
    val currentDate = LocalDate.now()
    var selectedMonth by remember { mutableIntStateOf(currentDate.monthValue) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "当前选择: ${selectedMonth}月", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp)
        )

        MonthPicker(
            selectedMonth = selectedMonth, onMonthSelected = { month ->
                selectedMonth = month
            }, modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}