package com.fanda.homebook.book.sheet

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionWithSubCategories
import com.fanda.homebook.quick.sheet.SheetTitleWidget

@Composable fun TransactionTypeBottomSheet(
    initial: TransactionSubEntity?,
    data: List<TransactionWithSubCategories>,
    title: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    onSettingClick: () -> Unit,
    onConfirm: (TransactionSubEntity?) -> Unit
) {
    CustomBottomSheet(visible = visible, onDismiss = onDismiss) {
        var selected by remember { mutableStateOf(initial) }
        Column {
            SheetTitleWidget(title = title, onSettingClick = onSettingClick) {
                onConfirm(selected)
                onDismiss()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)) {
                    SelectableRoundedButton(
                        cornerSize = 8.dp,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        modifier = Modifier.size(96.dp, 44.dp),
                        text = "全部类型",
                        selected = selected == null,
                        onClick = { selected = null })
                    data.forEach {
                        TransactionTypeItem(initial = selected, title = it.category.name, data = it.subCategories) { subEntity ->
                            selected = subEntity
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope") @OptIn(ExperimentalLayoutApi::class) @Composable fun TransactionTypeItem(
    initial: TransactionSubEntity?, modifier: Modifier = Modifier, title: String, data: List<TransactionSubEntity?>, onSelect: (TransactionSubEntity?) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp), text = title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black
        )
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val itemSpacing = 16.dp
            val maxColumns = 3
            val totalSpacing = itemSpacing * (maxColumns - 1)
            val itemWidth = (maxWidth - totalSpacing) / maxColumns  // 根据父容器的宽度和间距，动态计算每个item的宽度

            FlowRow(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(itemSpacing), verticalArrangement = Arrangement.spacedBy(itemSpacing), maxItemsInEachRow = maxColumns
            ) {
                data.forEach { category ->
                    SelectableRoundedButton(
                        cornerSize = 8.dp,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .width(itemWidth),
                        text = category?.name ?: "",
                        selected = initial?.name == category?.name && initial?.id == category?.id,
                        onClick = { onSelect(category) })
                }
            }
        }

    }
}


@Composable @Preview(showBackground = true) fun TransactionTypeBottomSheetPreview() {
    TransactionTypeBottomSheet(initial = null, title = "选择类型", visible = true, onDismiss = {}, onConfirm = {}, onSettingClick = {}, data = emptyList())
}