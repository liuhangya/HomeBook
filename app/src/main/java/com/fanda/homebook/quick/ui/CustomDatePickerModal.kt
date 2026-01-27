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
import com.fanda.homebook.tools.convertToLocalMidnight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        GradientRoundedBoxWithStroke(
            colors = listOf(colorResource(R.color.color_E3EBF5), Color.White), cornerRadius = 12.dp,
            strokeColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DatePicker(
                    state = datePickerState, colors = DatePickerDefaults.colors().copy(
                        selectedDayContentColor = Color.White,
                        selectedDayContainerColor = Color.Black,
                        selectedYearContainerColor = Color.Black,
                        selectedYearContentColor = Color.White,
                        containerColor = colorResource(R.color.color_E3EBF5)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = colorResource(R.color.color_E1E9F3))
                    ) {
                        Text("取消", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    FilledTonalButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            // 转换为本地时区的 0 点时间戳
                            val localMidnightMillis = if (selectedMillis != null) {
                                convertToLocalMidnight(selectedMillis)
                            } else {
                                null
                            }
                            onDateSelected(localMidnightMillis)
                            onDismiss()
                        }, colors = ButtonDefaults.filledTonalButtonColors().copy(
                            containerColor = Color.Black,
                        )
                    ) {
                        Text("确定", color = Color.White)
                    }
                }
            }
        }
    }
}