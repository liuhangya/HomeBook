package com.fanda.homebook.quick.ui

import android.os.Handler
import android.os.Looper
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.isValidDecimalInput

/**
 * 金额输入字段组件
 * 自定义样式的金额输入框，支持小数输入和键盘优化
 *
 * @param modifier 修饰符
 * @param price 当前金额值（字符串形式）
 * @param onValueChange 金额值变化回调
 */
@Composable fun EditAmountField(
    modifier: Modifier = Modifier, price: String = "", onValueChange: (String) -> Unit
) {
    // 焦点管理
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 用于触发键盘刷新的标志
    // 为了解决某些输入法类型切换问题
    var refreshKeyboard by remember { mutableStateOf(true) }

    // 初始化时，将 selection 设置为文本末尾
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = price, selection = TextRange(price.length)
            )
        )
    }

    // 2. 同步外部价格变化
    LaunchedEffect(price) {
        // 只有当外部价格确实不同时才更新，避免不必要的重组
        if (textFieldValue.text != price) {
            textFieldValue = TextFieldValue(
                text = price,
                selection = TextRange(price.length)
            )
        }
    }


    // 渐变圆角边框容器
    GradientRoundedBoxWithStroke(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)  // 固定高度
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()
        ) {
            // 人民币符号
            Text(
                text = "¥", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp)
            )

            // 自定义文本输入框
            BasicTextField(
                value = textFieldValue, onValueChange = { newValue ->
                    val newText = newValue.text

                    if (isValidDecimalInput(newText)) {
                        // 【关键修改 3】构造新的 TextFieldValue，强制将光标设在末尾
                        textFieldValue = newValue.copy(
                            text = newText,
                            selection = TextRange(newText.length) // 强制光标在最后
                        )
                        onValueChange(newText)
                    }
                }, singleLine = true,  // 单行输入
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)  // 内边距
                    .focusRequester(focusRequester)  // 绑定焦点请求器
                    .onFocusChanged { state ->
                        // 为了解决不同类型的输入法无法切换的问题，比如数字和文本类型
                        if (state.isFocused && refreshKeyboard) {
                            // 每次获取焦点都触发键盘刷新
                            refreshKeyboard = false

                            // 用户点击时，先清焦再聚焦（强制刷新键盘）
                            focusManager.clearFocus(true)

                            // 延迟50ms后重新请求焦点
                            Handler(Looper.getMainLooper()).postDelayed({
                                focusRequester.requestFocus()
                            }, 50)

                            // 延迟100ms后重置刷新标志
                            Handler(Looper.getMainLooper()).postDelayed({
                                refreshKeyboard = true
                            }, 100)
                        }
                    }, keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,  // 小数键盘类型
                    imeAction = ImeAction.Done             // 完成动作
                ), textStyle = TextStyle.Default.copy(
                    fontSize = 32.sp,   // 大字号，突出金额显示
                    color = Color.Black, // 黑色文字
                )
            )
        }
    }
}