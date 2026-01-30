package com.fanda.homebook.quick.sheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.LocalDataSource

@Composable inline fun <reified T : Any> GridBottomSheet(
    initial: T?,
    title: String,
    dataSource: List<T>,
    dpSize: DpSize,
    column: GridCells,
    visible: () -> Boolean,
    crossinline displayText: (T) -> String,
    noinline onDismiss: () -> Unit,
    noinline onSettingClick: (() -> Unit)? = null,
    crossinline onConfirm: (T?) -> Unit
) {
    CustomBottomSheet(visible = visible(), onDismiss = onDismiss) {
        var selected by remember { mutableStateOf(initial) }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SheetTitleWidget(title = title, onSettingClick = onSettingClick) {
                onConfirm(selected)
                onDismiss()
            }
            LazyVerticalGrid(
                columns = column, horizontalArrangement = Arrangement.spacedBy(13.dp), verticalArrangement = Arrangement.spacedBy(13.dp),
                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 24.dp, top = 10.dp, end = 24.dp, bottom = 28.dp),
            ) {
                items(dataSource, key = { it.hashCode() }) {
                    SelectableRoundedButton(
                        cornerSize = 8.dp,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        modifier = Modifier.size(dpSize),
                        text = displayText(it),
                        selected = selected == it,
                        onClick = { selected = it })
                }
            }
        }
    }
}

@Composable
inline fun <reified T : Any> GridBottomSheet(
    initial: List<T>? = null,  // 改为 List<T>
    title: String,
    dataSource: List<T>,
    dpSize: DpSize,
    column: GridCells,
    visible: () -> Boolean,
    crossinline displayText: (T) -> String,
    noinline onDismiss: () -> Unit,
    noinline onSettingClick: (() -> Unit)? = null,
    crossinline onConfirm: (List<T>) -> Unit  // 返回 List<T>
) {
    CustomBottomSheet(visible = visible(), onDismiss = onDismiss) {
        var selected by remember(initial) {
            mutableStateOf(initial?.toSet() ?: emptySet())
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 保持原有标题栏样式，只修改回调
            SheetTitleWidget(title = title, onSettingClick = onSettingClick) {
                onConfirm(selected.toList())  // 转换为 List
                onDismiss()
            }

            LazyVerticalGrid(
                columns = column,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 24.dp, top = 10.dp, end = 24.dp, bottom = 28.dp),
            ) {
                items(dataSource, key = { it.hashCode() }) { item ->
                    val isSelected = selected.contains(item)

                    SelectableRoundedButton(
                        cornerSize = 8.dp,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        modifier = Modifier.size(dpSize),
                        text = displayText(item),
                        selected = isSelected,
                        onClick = {
                            selected = if (isSelected) {
                                // 如果已选中，则移除
                                selected - item
                            } else {
                                // 如果未选中，则添加
                                selected + item
                            }
                        }
                    )
                }
            }
        }
    }
}