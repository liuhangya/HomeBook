package com.fanda.homebook.quick.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.tools.isValidDecimalInput
import kotlinx.coroutines.delay

@Composable fun EditAmountField(modifier: Modifier = Modifier, price: String = "", onValueChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 用于触发键盘刷新的标志
    var refreshKeyboard by remember { mutableStateOf(true) }

    GradientRoundedBoxWithStroke(
        modifier = modifier
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
                value = price, onValueChange = { newText ->
                // 🔒 限制只能输入数字和一个小数点
                if (isValidDecimalInput(newText)) {
                    onValueChange(newText)
                }
                // 否则忽略非法输入
            }, singleLine = true, modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        // 为了解决不同类型的输入法无法切换的问题，比如数字和文本类型
                        if (state.isFocused && refreshKeyboard) {
                            // 每次获取焦点都触发键盘刷新
                            refreshKeyboard = false
                            // 用户点击时，先清焦再聚焦
                            focusManager.clearFocus(true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                focusRequester.requestFocus()
                            }, 50)
                            Handler(Looper.getMainLooper()).postDelayed({
                                refreshKeyboard = true
                            }, 100)
                        }

                    }, keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
            ), textStyle = TextStyle.Default.copy(
                fontSize = 32.sp,
                color = Color.Black,
            )
            )
        }
    }
}