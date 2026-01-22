package com.fanda.homebook.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R


@Composable fun EditCommentsWidget(
    modifier: Modifier = Modifier, isEditState: Boolean = true, inputText: String = "", onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center, modifier = modifier
    ) {
        Text(
            style = TextStyle.Default, text = "备注", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
        )
        BasicTextField(
            enabled = isEditState,
            value = inputText, onValueChange = { newText ->
                // 否则忽略非法输入
                onValueChange(newText)
            }, singleLine = true, modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 20.dp), keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
            ), textStyle = TextStyle.Default.copy(
                color = colorResource(R.color.color_333333), fontSize = 14.sp, textAlign = TextAlign.Start
            ), decorationBox = { innerTextField ->
                Box() {
                    // 占位文本
                    if (inputText.isEmpty()) {
                        Text(
                            text = "请输入备注信息", color = colorResource(R.color.color_83878C), fontSize = 14.sp
                        )
                    }
                    // 输入框内容
                    innerTextField()
                }
            })
    }
}