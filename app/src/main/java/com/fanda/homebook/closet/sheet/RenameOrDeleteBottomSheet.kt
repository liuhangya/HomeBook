package com.fanda.homebook.closet.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.R
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.ui.theme.HomeBookTheme

enum class RenameOrDeleteType {
    RENAME,
    DELETE
}

@Composable fun RenameOrDeleteBottomSheet(modifier: Modifier = Modifier, visible: Boolean, onDismiss: () -> Unit,onConfirm: (RenameOrDeleteType) -> Unit) {
    CustomBottomSheet(visible = visible, onDismiss = onDismiss) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = "重命名", fontSize = 16.sp, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onConfirm(RenameOrDeleteType.RENAME)
                }
                .padding(vertical = 26.dp))
            HorizontalDivider(
                color = colorResource(id = R.color.color_E1E9F3), modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Text(text = "删除", fontSize = 16.sp, color = colorResource(id = R.color.color_FF2822), fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onConfirm(RenameOrDeleteType.DELETE)
                }
                .padding(vertical = 26.dp))
            HorizontalDivider(
                color = colorResource(id = R.color.color_E1E9F3), modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Box(modifier = Modifier.padding(24.dp)){
                SelectableRoundedButton(
                    text = "取消", selected = false, onClick = onDismiss, cornerSize = 27.dp , modifier = Modifier
                        .fillMaxWidth(), contentPadding = PaddingValues(vertical = 16.dp)
                    , fontSize = 16.sp
                )
            }
        }
    }
}

@Composable @Preview(showBackground = true) fun RenameOrDeleteBottomSheetPreview() {
    HomeBookTheme {
        RenameOrDeleteBottomSheet(visible = true, onDismiss = {}, onConfirm = {})
    }
}