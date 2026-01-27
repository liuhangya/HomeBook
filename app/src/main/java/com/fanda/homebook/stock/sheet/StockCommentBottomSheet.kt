package com.fanda.homebook.stock.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.SheetTitleWidget
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.fanda.homebook.R

@Composable
fun StockCommentBottomSheet(
    remain: String,
    date: String,
    feel: String,
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onDateClick: () -> Unit,
    onRemainClick: (String) -> Unit,
    onFeelClick: (String) -> Unit
) {
    val itemPadding = Modifier.padding(
        24.dp, 24.dp, 24.dp, 24.dp
    )
    CustomBottomSheet(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center
        ) {
            SheetTitleWidget(title = "用完了") {
                onConfirm()
            }
            ItemOptionMenu(
                title = "用完日期",
                showText = true,
                rightText = date,
                showDivider = true,
                modifier = itemPadding,
                onClick = { onDateClick()})
            Text(
                style = TextStyle.Default, text = "用完后剩余量", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp,modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 20.dp)
            )
            Row(modifier = Modifier.padding(start = 24.dp, bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LocalDataSource.remainData.forEach {
                    SelectableRoundedButton(cornerSize = 8.dp,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        modifier = Modifier.size(66.dp,36.dp),
                        text = it,
                        selected = remain == it,
                        onClick = { onRemainClick(it)})
                }
            }
            HorizontalDivider(color = colorResource(id = R.color.color_E1E9F3), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
            Text(
                style = TextStyle.Default, text = "使用感受", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp,modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 20.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp),
            ) {
                items(LocalDataSource.feelData, key = { it }) {
                    SelectableRoundedButton(cornerSize = 8.dp,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        text = it,
                        modifier = Modifier.size(66.dp,36.dp),
                        selected = feel == it,
                        onClick = { onFeelClick(it) })
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SelectPhotoBottomSheetPreview() {
    HomeBookTheme {
        StockCommentBottomSheet(visible = true, onDismiss = {}, remain = "1", date = "2023-05-05", feel = "1", onConfirm = {}, onDateClick = {}, onRemainClick = {}, onFeelClick = {})
    }
}