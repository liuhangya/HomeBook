package com.fanda.homebook.stock.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.SheetTitleWidget
import com.fanda.homebook.ui.theme.HomeBookTheme

@Composable
fun StockCommentBottomSheet(
    remain: String,
    date: String,
    feel: String,
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ShowBottomSheetType) -> Unit
) {
    val itemPadding = Modifier.padding(
        24.dp, 24.dp, 24.dp, 24.dp
    )
    CustomBottomSheet(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SheetTitleWidget(title = "用完了") {
                onConfirm(ShowBottomSheetType.DONE)
                onDismiss()
            }
            ItemOptionMenu(
                title = "用完后剩余量",
                showText = true,
                rightText = remain,
                showDivider = true,
                modifier = itemPadding,
                onClick = { onConfirm(ShowBottomSheetType.REMAIN)})
            ItemOptionMenu(
                title = "用完日期",
                showText = true,
                rightText = date,
                showDivider = true,
                modifier = itemPadding,
                onClick = { onConfirm(ShowBottomSheetType.USE_UP_DATE)})
            ItemOptionMenu(
                title = "使用感受",
                showText = true,
                rightText = feel,
                showDivider = false,
                modifier = itemPadding,
                onClick = { onConfirm(ShowBottomSheetType.FEEL)})
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SelectPhotoBottomSheetPreview() {
    HomeBookTheme {
        StockCommentBottomSheet(visible = true, onDismiss = {}, remain = "1", date = "2023-05-05", feel = "1", onConfirm = {})
    }
}