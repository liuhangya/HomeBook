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

@Composable fun EditDialog(
    title: String, modifier: Modifier = Modifier, placeholder: String = "请输入", value: String, onDismissRequest: () -> Unit, onConfirm: (String) -> Unit, maxChar: Int = 4, showSuffix: Boolean = true
) {
    var newValue by remember { mutableStateOf(value) }
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)) {
        GradientRoundedBoxWithStroke(modifier = modifier, colors = listOf(colorResource(R.color.color_E3EBF5), Color.White)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(top = 24.dp, bottom = 27.dp))
                TextField(shape = RoundedCornerShape(12.dp), colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ), placeholder = { Text(text = placeholder, fontSize = 16.sp, color = colorResource(id = R.color.color_84878C)) }, value = newValue, onValueChange = { newText ->
                   if (showSuffix){
                       // 限制最大长度
                       if (newText.length <= maxChar) {
                           newValue = newText
                       }
                   }else{
                        newValue = newText
                   }
                }, singleLine = true, modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp)), keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ), textStyle = TextStyle.Default.copy(
                    fontSize = 16.sp,
                    color = Color.Black,
                ), suffix = {
                    if (showSuffix) {
                        Text(text = "${newValue.length}/$maxChar", fontSize = 16.sp, color = colorResource(id = R.color.color_56585B))
                    }
                })
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 20.dp)
                ) {
                    SelectableRoundedButton(
                        interaction = true,
                        text = "取消", selected = false, onClick = onDismissRequest, cornerSize = 27.dp, contentPadding = PaddingValues(horizontal = 47.dp, vertical = 15.dp), fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    SelectableRoundedButton(
                        text = "确定", selected = true, onClick = { onConfirm(newValue) }, cornerSize = 27.dp, contentPadding = PaddingValues(horizontal = 47.dp, vertical = 15.dp), fontSize = 16.sp
                    )
                }
            }
        }

    }
}

@Composable @Preview(showBackground = true) fun EditDialogPreview() {
    HomeBookTheme {
        EditDialog(onDismissRequest = {}, onConfirm = {}, title = "添加分类", value = "")
    }
}