package com.fanda.homebook.book.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.quick.sheet.SheetTitleWidget
import com.fanda.homebook.tools.LogUtils

@Composable
fun MonthBottomSheet(
    month: Int,
    visible: Boolean,
    onDismiss: () -> Unit,
    onMonthSelected: (Int) -> Unit
) {
    var selectMonth by remember { mutableIntStateOf(month) }
    CustomBottomSheet(visible = visible, onDismiss = onDismiss) {
        Column() {
            SheetTitleWidget(title = "开封后保鲜期") {
                onMonthSelected(selectMonth)
            }
            Box(modifier = Modifier.padding(vertical = 20.dp)) {
                MonthPicker(
                    selectedMonth = selectMonth, onMonthSelected = { month ->
                        selectMonth = month
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}