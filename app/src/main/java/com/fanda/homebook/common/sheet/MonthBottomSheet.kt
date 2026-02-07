package com.fanda.homebook.common.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fanda.homebook.book.sheet.MonthPicker
import com.fanda.homebook.components.CustomBottomSheet

/**
 * 月份选择底部弹窗组件
 *
 * 用于选择开封后保鲜期月份（如食品、化妆品等）
 *
 * @param month 当前选中的月份（1-12）
 * @param visible 控制弹窗的显示/隐藏状态
 * @param onDismiss 弹窗关闭回调函数
 * @param onMonthSelected 月份选择确认回调，返回选中的月份
 */
@Composable fun MonthBottomSheet(
    month: Int, visible: Boolean, onDismiss: () -> Unit, onMonthSelected: (Int) -> Unit
) {
    // 内部状态管理：当前选中的月份
    var selectMonth by remember { mutableIntStateOf(month) }

    CustomBottomSheet(
        visible = visible, onDismiss = onDismiss
    ) {
        Column() {
            // 标题栏（包含确认按钮）
            SheetTitleWidget(
                title = "开封后保鲜期" // 特定的使用场景描述
            ) {
                // 点击确认时回调选择的月份
                onMonthSelected(selectMonth)
            }

            // 月份选择器区域
            Box(
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                MonthPicker(
                    selectedMonth = selectMonth, onMonthSelected = { month ->
                        // 月份选择变化时更新内部状态
                        selectMonth = month
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // 固定高度
                )
            }
        }
    }
}