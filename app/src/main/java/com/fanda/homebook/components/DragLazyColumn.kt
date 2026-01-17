package com.fanda.homebook.components

import android.util.Log
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
import com.fanda.homebook.entity.ClosetGridEntity
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 可拖拽排序的 LazyColumn
 * - 仅当长按右侧拖拽图标时才触发拖动
 * - Item 其他区域点击事件不受影响
 */
@Composable fun <T> DragLazyColumn(
    items: MutableList<T>, modifier: Modifier = Modifier, onMove: (from: Int, to: Int, items: MutableList<T>) -> Unit = { _, _, _ -> }, itemContent: @Composable (item: T, isDragging: Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var dragStartIndex by remember { mutableStateOf<Int?>(null) }

    val coroutineScope = rememberCoroutineScope()
    // 拖拽手柄宽度（必须与 UI 中图标区域一致）
    val DRAG_HANDLE_WIDTH_DP = 56.dp

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp), state = listState, modifier = modifier.pointerInput(Unit) {

        val handleWidthPx = with(this.density) { DRAG_HANDLE_WIDTH_DP.toPx() }.toInt()

        detectDragGesturesAfterLongPress(onDragStart = { position ->


            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val info = visibleItems.find {
                position.y.toInt() in it.offset..(it.offset + it.size)
            }
            Log.d("DragLazyColumn", "position: $position, info: ${info?.index} , handleWidthPx: $handleWidthPx")
            if (info != null) {
                // 计算手柄区域：右侧 [viewportEnd - handleWidth, viewportEnd]
                // viewportEndOffset 是 LazyColumn 内容的总高度，不能用这个参数
//                val viewportEnd = listState.layoutInfo.viewportEndOffset
                val viewportEnd = listState.layoutInfo.viewportSize.width
                val handleLeft = viewportEnd - handleWidthPx
                Log.d("DragLazyColumn", "viewportEnd: $viewportEnd,handleLeft: $handleLeft")
                // 只有在手柄区域内才开始拖拽
                if (position.x.toInt() >= handleLeft) {
                    draggedIndex = info.index
                    dragStartIndex = info.index
                    dragOffsetY = 0f
                }
            }
        }, onDragEnd = {
            if (dragStartIndex != null && draggedIndex != null) {
                val from = dragStartIndex!!
                val to = draggedIndex!!
                if (from != to) {
                    onMove(from, to, items)
                }
            }
            draggedIndex = null
            dragStartIndex = null
            dragOffsetY = 0f
        }, onDrag = { change, dragAmount ->
            if (draggedIndex != null) {
                dragOffsetY += dragAmount.y

                // 自动滚动
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                if (visibleItems.isNotEmpty()) {
                    val firstVisible = visibleItems.first().index
                    val lastVisible = visibleItems.last().index
                    val current = draggedIndex!!

                    if (current == firstVisible && dragAmount.y < 0) {
                        coroutineScope.launch {
                            listState.scrollBy(dragAmount.y * 0.4f)
                        }
                    }
                    if (current == lastVisible && dragAmount.y > 0) {
                        coroutineScope.launch {
                            listState.scrollBy(dragAmount.y * 0.4f)
                        }
                    }
                }

                // 实时交换位置
                val itemHeight = visibleItems.firstOrNull()?.size ?: 120
                if (abs(dragOffsetY) > itemHeight * 0.6f) {
                    val oldIndex = draggedIndex!!
                    val direction = if (dragOffsetY > 0) 1 else -1
                    val newIndex = (oldIndex + direction).coerceIn(0, items.size - 1)

                    if (oldIndex != newIndex) {
                        val item = items.removeAt(oldIndex)
                        items.add(newIndex, item)
                        draggedIndex = newIndex
                        dragOffsetY -= itemHeight * direction
                    }
                }

                change.consume()
            }
        })
    }) {
        itemsIndexed(items = items, key = { _, item -> item.hashCode() } // 必须用唯一 ID
        ) { index, item ->
            val isDragging = draggedIndex == index
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDragging) {
                        Modifier.offset { IntOffset(0, dragOffsetY.roundToInt()) }
                    } else Modifier)) {
                itemContent(item, isDragging)
            }
        }
    }
}