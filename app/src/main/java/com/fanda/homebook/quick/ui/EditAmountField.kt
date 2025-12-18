package com.fanda.homebook.quick.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.tools.isValidDecimalInput
import kotlinx.coroutines.delay

@Composable fun EditAmountField() {
    val keyboardController = LocalSoftwareKeyboardController.current
    var amount by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    GradientRoundedBoxWithStroke(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "¥", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp)
            )
            BasicTextField(
                value = amount, onValueChange = { newText ->
                    // 🔒 限制只能输入数字和一个小数点
                    if (isValidDecimalInput(newText)) {
                        amount = newText
                    }
                    // 否则忽略非法输入
                }, singleLine = true, modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .focusRequester(focusRequester), keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                ), textStyle = TextStyle.Default.copy(
                    fontSize = 32.sp,
                    color = Color.Black,
                )
            )
        }
        LaunchedEffect(Unit) {
            delay(100) // 短暂延迟确保布局完成
            focusRequester.requestFocus()   // 先获取焦点
            keyboardController?.hide() // 只想获取焦点，不想自动弹出键盘
        }
    }
}