package com.fanda.homebook.common.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.SelectableRoundedButton

/**
 * 通用网格底部弹窗组件（单选版本）
 * 以网格形式显示选项，支持单选操作
 *
 * @param T 数据类型，必须为 Any 的子类
 * @param initial 初始选中的项（可为空）
 * @param title 弹窗标题
 * @param dataSource 数据源列表
 * @param dpSize 每个网格项的大小
 * @param column 网格列数配置
 * @param visible 弹窗是否可见的函数
 * @param displayText 将数据项转换为显示文本的函数
 * @param onDismiss 弹窗关闭回调函数
 * @param onSettingClick 设置按钮点击回调（可选）
 * @param onConfirm 确认选择回调函数，返回选中的项
 */
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
        // 记录当前选中的项
        var selected by remember { mutableStateOf(initial) }

        Column(modifier = Modifier.fillMaxWidth()) {
            // 弹窗标题栏
            SheetTitleWidget(
                title = title, onSettingClick = onSettingClick
            ) {
                // 确认按钮点击逻辑
                onConfirm(selected)
                onDismiss()
            }

            // 网格选项列表
            LazyVerticalGrid(
                columns = column,  // 网格列数配置
                horizontalArrangement = Arrangement.spacedBy(13.dp),  // 水平间距
                verticalArrangement = Arrangement.spacedBy(13.dp),    // 垂直间距
                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(
                    start = 24.dp, top = 10.dp, end = 24.dp, bottom = 28.dp
                )
            ) {
                items(dataSource, key = { it.hashCode() }) { item ->
                    SelectableRoundedButton(
                        cornerSize = 8.dp,      // 圆角大小
                        fontSize = 14.sp,       // 字体大小
                        contentPadding = PaddingValues(
                            horizontal = 0.dp, vertical = 0.dp
                        ), modifier = Modifier.size(dpSize),  // 固定大小
                        text = displayText(item),          // 显示文本
                        selected = selected == item,       // 选中状态
                        onClick = {
                            selected = item  // 点击选中该项
                        })
                }
            }
        }
    }
}

/**
 * 通用网格底部弹窗组件（多选版本）
 * 以网格形式显示选项，支持多选操作
 *
 * @param T 数据类型，必须为 Any 的子类
 * @param initial 初始选中的项列表（可为空）
 * @param title 弹窗标题
 * @param dataSource 数据源列表
 * @param dpSize 每个网格项的大小
 * @param column 网格列数配置
 * @param visible 弹窗是否可见的函数
 * @param displayText 将数据项转换为显示文本的函数
 * @param onDismiss 弹窗关闭回调函数
 * @param onSettingClick 设置按钮点击回调（可选）
 * @param onConfirm 确认选择回调函数，返回选中的项列表
 */
@Composable inline fun <reified T : Any> GridBottomSheet(
    initial: List<T>? = null,  // 改为 List<T>，支持多选
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
        // 使用 Set 来存储选中的项，便于添加和删除操作
        var selected by remember(initial) {
            mutableStateOf(initial?.toSet() ?: emptySet())
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            // 弹窗标题栏
            SheetTitleWidget(
                title = title, onSettingClick = onSettingClick
            ) {
                // 确认按钮点击逻辑，将 Set 转换为 List 返回
                onConfirm(selected.toList())
                onDismiss()
            }

            // 网格选项列表
            LazyVerticalGrid(
                columns = column,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 24.dp, top = 10.dp, end = 24.dp, bottom = 32.dp
                ),
            ) {
                items(dataSource, key = { it.hashCode() }) { item ->
                    // 判断当前项是否被选中
                    val isSelected = selected.contains(item)

                    SelectableRoundedButton(
                        cornerSize = 8.dp, fontSize = 14.sp, contentPadding = PaddingValues(
                            horizontal = 0.dp, vertical = 0.dp
                        ), modifier = Modifier.size(dpSize), text = displayText(item), selected = isSelected, onClick = {
                            selected = if (isSelected) {
                                // 如果已选中，则从选中集合中移除
                                selected - item
                            } else {
                                // 如果未选中，则添加到选中集合中
                                selected + item
                            }
                        })
                }
            }
        }
    }
}