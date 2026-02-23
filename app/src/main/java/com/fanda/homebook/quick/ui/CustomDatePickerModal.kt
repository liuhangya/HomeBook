package com.fanda.homebook.quick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fanda.homebook.R
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.tools.addCurrentTimeToDate
import com.hjq.toast.Toaster
import java.time.Instant
import java.time.ZoneOffset

/**
 * 自定义日期选择器模态对话框
 * 使用Material3的DatePicker，并添加自定义样式和按钮
 *
 * @param initialDate 初始日期时间戳（毫秒）
 * @param onDateSelected 日期选择回调，返回转换后的时间戳（带当前时间）
 * @param onDismiss 对话框关闭回调
 */
@OptIn(ExperimentalMaterial3Api::class) @Composable fun CustomDatePickerModal(
    initialDate: Long,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    // 转换为UTC的LocalDate
    val utcDate = Instant.ofEpochMilli(if (initialDate <=0L) System.currentTimeMillis() else initialDate)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
    // 将UTC日期转换为时间戳，作为初始日期
    val utcMillis = utcDate
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = utcMillis)


    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnClickOutside = true,  // 点击外部可关闭
            usePlatformDefaultWidth = false  // 不使用平台默认宽度
        )
    ) {
        // 自定义渐变圆角边框容器
        GradientRoundedBoxWithStroke(
            colors = listOf(
                colorResource(R.color.color_E3EBF5),  // 渐变起始颜色
                Color.White                            // 渐变结束颜色
            ), cornerRadius = 12.dp,                     // 圆角半径
            strokeColor = Color.White,                // 边框颜色
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)                       // 外边距
        ) {
            Column {
                // Material3 DatePicker组件
                DatePicker(
                    state = datePickerState, colors = DatePickerDefaults.colors().copy(
                        selectedDayContentColor = Color.White,        // 选中日期文字颜色
                        selectedDayContainerColor = Color.Black,      // 选中日期背景色
                        selectedYearContainerColor = Color.Black,     // 选中年份背景色
                        selectedYearContentColor = Color.White,       // 选中年份文字颜色
                        containerColor = colorResource(R.color.color_E3EBF5)  // 容器背景色
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))  // 间距

                // 底部按钮行
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp, bottom = 16.dp), horizontalArrangement = Arrangement.End  // 按钮靠右对齐
                ) {
                    // 取消按钮
                    Button(
                        onClick = onDismiss, colors = ButtonDefaults.buttonColors().copy(
                            containerColor = colorResource(R.color.color_E1E9F3)  // 浅灰色背景
                        )
                    ) {
                        Text("取消", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.width(20.dp))  // 按钮间距

                    // 确定按钮
                    FilledTonalButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis == null) {
                                // 未选择日期时提示
                                Toaster.show("请选择日期")
                            } else {
                                // 将选中的日期（UTC时间）转换为本地时区时间戳，并添加当前时间
                                onDateSelected(addCurrentTimeToDate(selectedMillis))
                                onDismiss()
                            }
                        }, colors = ButtonDefaults.filledTonalButtonColors().copy(
                            containerColor = Color.Black,  // 黑色背景
                        )
                    ) {
                        Text("确定", color = Color.White)
                    }
                }
            }
        }
    }
}