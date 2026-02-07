package com.fanda.homebook.common.sheet

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomBottomSheet

/**
 * 通用列表底部弹窗组件（单选版本）
 * 以垂直列表形式显示选项，支持单选操作
 *
 * @param T 数据类型，必须为 Any 的子类
 * @param initial 初始选中的项（可为空）
 * @param title 弹窗标题
 * @param dataSource 数据源列表
 * @param visible 弹窗是否可见的函数
 * @param displayText 将数据项转换为显示文本的函数
 * @param onDismiss 弹窗关闭回调函数
 * @param onSettingClick 设置按钮点击回调（用于跳转到编辑页面，可选）
 * @param onConfirm 确认选择回调函数，返回选中的项
 */
@Composable inline fun <reified T : Any> ListBottomSheet(
    initial: T?,
    title: String,
    dataSource: List<T>,
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

            // 垂直列表选项
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 9.dp),  // 垂直内边距
                flingBehavior = ScrollableDefaults.flingBehavior(),  // 使用默认的滚动行为（无弹性效果）
            ) {
                items(dataSource, key = { it.hashCode() }) { item ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,  // 左右对齐
                        verticalAlignment = Alignment.CenterVertically,     // 垂直居中对齐
                        modifier = Modifier
                            // 注意顺序：先添加点击事件，后添加内边距
                            .clickable(onClick = {
                                selected = item  // 点击选中该项
                            })
                            .padding(vertical = 15.dp, horizontal = 24.dp)  // 每个列表项的内边距
                    ) {
                        // 选项文本
                        Text(
                            text = displayText(item), style = TextStyle.Default.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ), fontSize = 16.sp
                        )

                        // 占位空间，将选中图标推到最右侧
                        Spacer(modifier = Modifier.weight(1f))

                        // 选中状态指示器
                        if (selected == item) {
                            Image(
                                painter = painterResource(R.mipmap.icon_selected), contentDescription = null,  // 无障碍描述
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}