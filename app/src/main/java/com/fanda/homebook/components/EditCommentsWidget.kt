package com.fanda.homebook.components

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R

/**
 * 编辑备注组件
 *
 * 用于输入单行备注信息的自定义输入框，带有标题和占位符，
 * 特别处理了焦点获取时的键盘刷新问题
 *
 * @param modifier 修饰符，用于自定义整体布局
 * @param isEditState 是否处于编辑状态，true时可编辑，false时只读
 * @param inputText 当前输入的文本内容
 * @param onValueChange 文本变化时的回调函数，接收新文本
 */
@Composable fun EditCommentsWidget(
    modifier: Modifier = Modifier, isEditState: Boolean = true, inputText: String = "", onValueChange: (String) -> Unit
) {
    // 焦点管理相关
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 用于触发键盘刷新的标志
    var refreshKeyboard by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.Center, modifier = modifier
    ) {
        // 标题：显示"备注"
        Text(
            style = TextStyle.Default, text = "备注", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
        )

        // 自定义输入框
        BasicTextField(
            enabled = isEditState, // 控制是否可编辑
            value = inputText, onValueChange = { newText ->
                // 文本变化时直接传递给外部
                onValueChange(newText)
            }, singleLine = true, // 单行输入
            modifier = Modifier
                .focusRequester(focusRequester) // 关联焦点请求器
                .onFocusChanged { state ->
                    // 焦点变化监听：当获得焦点且需要刷新键盘时
                    if (state.isFocused && refreshKeyboard) {
                        // 每次获取焦点都触发键盘刷新
                        refreshKeyboard = false

                        // 临时清除焦点，然后重新获取，以触发键盘刷新
                        // 这有助于解决某些情况下键盘不弹出的问题
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
                }
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 20.dp), // 上下内边距
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text, // 文本输入类型
                imeAction = ImeAction.Done         // 输入法动作：完成
            ), textStyle = TextStyle.Default.copy(
                color = colorResource(R.color.color_333333), // 深灰色文本
                fontSize = 14.sp, textAlign = TextAlign.Start
            ), decorationBox = { innerTextField ->
                // 自定义装饰框：包含占位符和实际输入框
                Box() {
                    // 占位文本：当输入为空时显示
                    if (inputText.isEmpty()) {
                        Text(
                            text = "请输入备注信息", color = colorResource(R.color.color_83878C), // 浅灰色占位符
                            fontSize = 14.sp
                        )
                    }
                    // 输入框内容
                    innerTextField()
                }
            })
    }
}