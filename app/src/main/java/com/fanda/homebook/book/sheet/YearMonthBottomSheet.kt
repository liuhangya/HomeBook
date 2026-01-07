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

@Composable fun YearMonthBottomSheet(year: Int, month: Int, visible: Boolean, onDismiss: () -> Unit, onYearMonthSelected: (Int, Int) -> Unit) {
    var selectYear by remember { mutableIntStateOf(year) }
    var selectMonth by remember { mutableIntStateOf(month) }
    CustomBottomSheet(visible = visible, onDismiss = onDismiss) {
        Column() {
            SheetTitleWidget(title = "选择时间") {
                onYearMonthSelected(selectYear, selectMonth)
            }
            Box(modifier = Modifier.padding(vertical = 20.dp)) {
                YearMonthPicker(
                    selectedYear = selectYear, selectedMonth = selectMonth, onYearMonthSelected = { year, month ->
                        selectYear = year
                        selectMonth = month
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}