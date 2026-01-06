package com.fanda.homebook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fanda.homebook.R
import com.fanda.homebook.ui.theme.HomeBookTheme

@Composable fun ConfirmDialog(
    title: String, modifier: Modifier = Modifier, onDismissRequest: () -> Unit, onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)) {
        GradientRoundedBoxWithStroke(modifier = modifier, colors = listOf(colorResource(R.color.color_E3EBF5), Color.White)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(horizontal = 16.dp, vertical = 40.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding( bottom = 20.dp)
                ) {
                    SelectableRoundedButton(
                        interaction = true,
                        text = "取消", selected = false, onClick = onDismissRequest, cornerSize = 27.dp, contentPadding = PaddingValues(horizontal = 47.dp, vertical = 15.dp), fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    SelectableRoundedButton(
                        text = "确定", selected = true, onClick = { onConfirm() }, cornerSize = 27.dp, contentPadding = PaddingValues(horizontal = 47.dp, vertical = 15.dp), fontSize = 16.sp
                    )
                }
            }
        }

    }
}

@Composable @Preview(showBackground = true) fun ConfirmDialogPreview() {
    HomeBookTheme {
        ConfirmDialog(onDismissRequest = {}, onConfirm = {}, title = "添加分类")
    }
}