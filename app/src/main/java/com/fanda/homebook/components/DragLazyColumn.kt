package com.fanda.homebook.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.fanda.homebook.tools.LogUtils
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 可拖拽排序的 LazyColumn 组件
 *
 * 特点：
 * - 仅当长按右侧拖拽图标时才触发拖动操作
 * - Item 其他区域点击事件不受影响，保持正常的交互
 * - 支持自动滚动边缘检测
 * - 实时位置交换和动画效果
 *
 * @param items 可变的项目列表，拖拽后会直接修改此列表
 * @param modifier 修饰符，用于自定义布局
 * @param onMove 项目位置交换时的回调函数，参数：原位置(from)、新位置(to)、当前列表(items)
 * @param key 为每个项目生成唯一标识的函数，用于列表优化
 * @param itemContent 每个项目的UI内容，接收两个参数：项目数据(isDragging)和是否正在拖拽状态
 */
@Composable fun <T> DragLazyColumn(
    items: MutableList<T>,
    modifier: Modifier = Modifier,
    onMove: (from: Int, to: Int, items: MutableList<T>) -> Unit = { _, _, _ -> },
    key: (T) -> Any,
    itemContent: @Composable (item: T, isDragging: Boolean) -> Unit
) {
    // 列表滚动状态
    val listState = rememberLazyListState()

    // 拖拽状态管理
    var draggedIndex by remember { mutableStateOf<Int?>(null) }       // 当前被拖拽的项目索引
    var dragOffsetY by remember { mutableFloatStateOf(0f) }           // 拖拽的垂直偏移量
    var dragStartIndex by remember { mutableStateOf<Int?>(null) }     // 拖拽开始时的原始索引

    val coroutineScope = rememberCoroutineScope()

    // 拖拽手柄宽度（必须与UI中的拖拽图标区域保持一致）
    val DRAG_HANDLE_WIDTH_DP = 56.dp

    // 使用快照避免在lambda中直接引用可变列表
    val itemsSnapshot = items

    // 主列表布局：包含拖拽手势检测
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),  // 项目间距
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp), // 内边距
        state = listState, modifier = modifier.pointerInput(itemsSnapshot) { // 使用pointerInput处理手势

            // 将拖拽手柄宽度转换为像素
            val handleWidthPx = with(this.density) { DRAG_HANDLE_WIDTH_DP.toPx() }.toInt()

            // 检测长按后的拖拽手势
            detectDragGesturesAfterLongPress(onDragStart = { position ->
                // 根据点击位置确定被拖拽的项目
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                val info = visibleItems.find {
                    position.y.toInt() in it.offset..(it.offset + it.size)
                }

                LogUtils.d("position: $position, info: ${info?.index} , handleWidthPx: $handleWidthPx")

                if (info != null) {
                    // 计算手柄区域：右侧 [viewportEnd - handleWidth, viewportEnd]
                    val viewportEnd = listState.layoutInfo.viewportSize.width
                    val handleLeft = viewportEnd - handleWidthPx

                    LogUtils.d("viewportEnd: $viewportEnd, handleLeft: $handleLeft")

                    // 只有在手柄区域内才开始拖拽
                    if (position.x.toInt() >= handleLeft) {
                        draggedIndex = info.index
                        dragStartIndex = info.index
                        dragOffsetY = 0f
                        LogUtils.d("draggedIndex: $draggedIndex, dragStartIndex: $dragStartIndex")
                    }
                }
            }, onDragEnd = {
                // 拖拽结束时，如果位置发生变化，调用回调函数
                if (dragStartIndex != null && draggedIndex != null) {
                    val from = dragStartIndex!!
                    val to = draggedIndex!!
                    if (from != to) {
                        onMove(from, to, items)
                    }
                }

                LogUtils.d("draggedIndex2: $draggedIndex, dragStartIndex: $dragStartIndex")

                // 重置拖拽状态
                draggedIndex = null
                dragStartIndex = null
                dragOffsetY = 0f
            }, onDrag = { change, dragAmount ->
                if (draggedIndex != null) {
                    // 累加拖拽偏移量
                    dragOffsetY += dragAmount.y

                    // 边缘自动滚动逻辑
                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                    if (visibleItems.isNotEmpty()) {
                        val firstVisible = visibleItems.first().index
                        val lastVisible = visibleItems.last().index
                        val current = draggedIndex!!

                        // 向上滚动：拖拽到顶部边缘时
                        if (current == firstVisible && dragAmount.y < 0) {
                            coroutineScope.launch {
                                listState.scrollBy(dragAmount.y * 0.4f) // 减速滚动
                            }
                        }

                        // 向下滚动：拖拽到底部边缘时
                        if (current == lastVisible && dragAmount.y > 0) {
                            coroutineScope.launch {
                                listState.scrollBy(dragAmount.y * 0.4f)
                            }
                        }
                    }

                    // 实时位置交换：当拖拽超过项目高度的60%时交换位置
                    val itemHeight = visibleItems.firstOrNull()?.size ?: 120 // 默认项目高度
                    if (abs(dragOffsetY) > itemHeight * 0.6f) {
                        val oldIndex = draggedIndex!!
                        val direction = if (dragOffsetY > 0) 1 else -1 // 拖拽方向
                        val newIndex = (oldIndex + direction).coerceIn(0, items.size - 1) // 计算新位置

                        LogUtils.d("oldIndex: $oldIndex, newIndex: $newIndex")

                        if (oldIndex != newIndex) {
                            // 交换列表中的项目位置
                            val item = items.removeAt(oldIndex)
                            items.add(newIndex, item)

                            // 更新拖拽索引
                            draggedIndex = newIndex

                            // 重置偏移量（相对新位置）
                            dragOffsetY -= itemHeight * direction
                        }
                    }

                    change.consume() // 消费手势事件
                }
            })
        }) {
        // 渲染列表项目
        itemsIndexed(items = items, key = { _, item -> key(item) } // 必须提供唯一键以优化列表性能
        ) { index, item ->
            val isDragging = draggedIndex == index // 判断当前项目是否正在被拖拽

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        // 如果是正在拖拽的项目，应用偏移量
                        if (isDragging) {
                            Modifier.offset {
                                IntOffset(0, dragOffsetY.roundToInt())
                            }
                        } else Modifier)) {
                // 渲染项目内容
                itemContent(item, isDragging)
            }
        }
    }
}