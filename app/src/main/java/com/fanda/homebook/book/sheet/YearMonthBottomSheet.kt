package com.fanda.homebook.book.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.common.sheet.SheetTitleWidget
import com.fanda.homebook.tools.LogUtils

/**
 * 年月选择底部弹窗
 * 用于选择年份和月份的组合
 *
 * @param year 初始选中的年份
 * @param month 初始选中的月份
 * @param visible 弹窗是否可见
 * @param onDismiss 弹窗关闭回调
 * @param onYearMonthSelected 年份月份选择确认回调，返回选中的年份和月份
 */
@Composable fun YearMonthBottomSheet(
    year: Int, month: Int, visible: Boolean, onDismiss: () -> Unit, onYearMonthSelected: (Int, Int) -> Unit
) {
    // 内部状态管理：选中的年份和月份
    var selectYear by remember { mutableIntStateOf(year) }
    var selectMonth by remember { mutableIntStateOf(month) }

    // 【关键修改】监听外部参数变化，当弹窗可见时同步外部传入的年份和月份
    LaunchedEffect(visible, year, month) {
        if (visible) {
            selectYear = year
            selectMonth = month
            LogUtils.i("YearMonthBottomSheet", "弹窗打开，状态已同步: year=$year, month=$month")
        }
    }

    // 自定义底部弹窗组件
    CustomBottomSheet(visible = visible, onDismiss = {
        // 取消弹窗时，重置内部状态
        selectYear = year
        selectMonth = month
        onDismiss()
    }) {
        // 弹窗内容
        Column() {
            // 标题栏组件，包含标题和确认按钮
            SheetTitleWidget(title = "选择时间") {
                // 点击确认按钮时，将当前选中的年份和月份传递给回调函数
                onYearMonthSelected(selectYear, selectMonth)
            }

            // 年月选择器容器
            Box(modifier = Modifier.padding(vertical = 20.dp)) {
                // 年月选择器组件
                YearMonthPicker(
                    selectedYear = selectYear,      // 当前选中的年份
                    selectedMonth = selectMonth,    // 当前选中的月份
                    onYearMonthSelected = { newYear, newMonth ->
                        // 当选择器中的年份或月份发生变化时，更新内部状态
                        selectYear = newYear
                        selectMonth = newMonth
                    }, modifier = Modifier
                        .fillMaxWidth()     // 占满父容器宽度
                        .height(300.dp)     // 固定高度300dp
                )
            }
        }
    }
}