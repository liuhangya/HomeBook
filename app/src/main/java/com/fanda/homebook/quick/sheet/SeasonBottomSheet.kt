package com.fanda.homebook.quick.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.LocalDataSource


@Composable fun SeasonBottomSheet(
    season: String, showBottomSheet: Boolean, onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    CustomBottomSheet(visible = showBottomSheet, onDismiss = onDismiss) {
        var selected by remember { mutableStateOf(season) }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center), style = TextStyle.Default, text = "季节", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
                )
                TextButton(modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp), onClick = {
                    onConfirm(selected)
                    onDismiss()
                }) {
                    Text(
                        style = TextStyle.Default, text = "确定", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 24.dp, top = 10.dp, end = 24.dp, bottom = 0.dp),
            ) {
                items(LocalDataSource.seasonData, key = { it }) {
                    SelectableRoundedButton(
                        cornerSize = 8.dp,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 19.dp, vertical = 9.dp),
                        text = it,
                        selected = selected == it,
                        onClick = { selected = it })
                }
            }
        }
    }
}