package com.fanda.homebook.components

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.tools.isValidDecimalInput
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 多功能选项菜单项组件
 *
 * 一个高度可配置的列表项组件，支持多种显示模式：
 * 开关、文本显示、输入框、颜色指示器、加号按钮等
 *
 * @param title 左侧标题文本
 * @param modifier 修饰符，用于自定义整体布局
 * @param rightText 右侧显示的文本内容
 * @param inputText 输入框的当前文本内容
 * @param isEditState 是否处于编辑状态（控制开关和输入框的可交互性）
 * @param showSwitch 是否显示开关控件
 * @param showText 是否显示右侧文本
 * @param showPlus 是否显示"+1"按钮
 * @param showTextField 是否显示输入框
 * @param showRightArrow 是否显示右侧箭头图标
 * @param showColor 是否显示颜色指示器
 * @param inputColor 颜色指示器的颜色（当showColor为true时需要）
 * @param onCheckedChange 开关状态变化回调函数
 * @param onValueChange 输入框文本变化回调函数
 * @param checked 开关的当前状态
 * @param showDivider 是否显示底部分割线
 * @param removeIndication 是否移除点击反馈效果
 * @param dividerPadding 分割线的水平内边距
 * @param showInputTextUnit 是否在输入框后显示"元"单位
 * @param keyboardOptions 输入框的键盘选项配置
 * @param onPlusClick "+1"按钮点击回调函数
 * @param onClick 整个项目点击回调函数
 */
@Composable fun ItemOptionMenu(
    title: String,
    modifier: Modifier = Modifier,
    rightText: String = "",
    inputText: String = "",
    isEditState: Boolean = true,
    showSwitch: Boolean = false,
    showText: Boolean = false,
    showPlus: Boolean = false,
    showTextField: Boolean = false,
    showRightArrow: Boolean = true,
    showColor: Boolean = false,
    inputColor: Color? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onValueChange: ((String) -> Unit)? = null,
    checked: Boolean = false,
    showDivider: Boolean = false,
    removeIndication: Boolean = false,
    dividerPadding: Dp = 20.dp,
    showInputTextUnit: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
    ),
    onPlusClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    // 焦点管理相关
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 用于触发键盘刷新的标志
    var refreshKeyboard by remember { mutableStateOf(true) }

    // 初始化时，将 selection 设置为文本末尾
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = inputText, selection = TextRange(inputText.length)
            )
        )
    }

    // 2. 同步外部价格变化
    LaunchedEffect(inputText) {
        // 只有当外部价格确实不同时才更新，避免不必要的重组
        if (textFieldValue.text != inputText) {
            textFieldValue = TextFieldValue(
                text = inputText, selection = TextRange(inputText.length)
            )
        }
    }

    // 主容器：可点击的Column
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                // 根据removeIndication参数决定是否显示点击反馈
                if (removeIndication) {
                    Modifier.clickable(
                        // 移除默认的涟漪效果
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) {
                        onClick?.invoke()
                    }
                } else {
                    Modifier.clickable {
                        onClick?.invoke()
                    }
                })) {
        // 内容区域
        Column(
            verticalArrangement = Arrangement.Center, modifier = modifier
        ) {
            // 水平布局：左侧标题 + 右侧各种控件
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 左侧标题
                Text(
                    style = TextStyle.Default, text = title, color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
                )

                // 占位Spacer，将右侧内容推到最右
                Spacer(modifier = Modifier.weight(1f))

                // 开关控件
                if (showSwitch) {
                    Switch(
                        modifier = Modifier.scale(0.8f), // 缩小到80%
                        enabled = isEditState,           // 受编辑状态控制
                        checked = checked, onCheckedChange = {
                            onCheckedChange?.invoke(it)
                        }, colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,          // 选中状态滑块颜色
                            checkedTrackColor = Color.Black,          // 选中状态轨道颜色
                            uncheckedThumbColor = Color.White,        // 未选中状态滑块颜色
                            uncheckedBorderColor = Color.Transparent, // 未选中状态边框颜色
                            uncheckedTrackColor = colorResource(R.color.color_CBD4E0), // 未选中状态轨道颜色
                        )
                    )
                }

                // 右侧文本显示
                if (showText) {
                    Text(
                        style = TextStyle.Default, text = rightText, color = colorResource(R.color.color_333333), // 深灰色
                        fontSize = 16.sp, modifier = Modifier.padding(end = if (showRightArrow) 0.dp else 10.dp)
                    )
                }

                // "+1"按钮
                if (showPlus) {
                    SelectableRoundedButton(
                        text = "+1", selected = false, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp), onClick = { onPlusClick?.invoke() }, interaction = true
                    )
                }

                // 输入框
                if (showTextField) {
                    BasicTextField(
                        enabled = isEditState, // 受编辑状态控制
                        value = textFieldValue, onValueChange = { newValue ->
                            val newText = newValue.text
                            textFieldValue = newValue.copy(
                                text = newText, selection = TextRange(newText.length) // 强制光标在最后
                            )
                            onValueChange?.invoke(newText)
                        }, singleLine = true, modifier = Modifier
                            .wrapContentWidth(Alignment.End)
                            .focusRequester(focusRequester)
                            .onFocusChanged { state ->
                                // 焦点变化处理：刷新键盘显示
                                if (state.isFocused && refreshKeyboard) {
                                    refreshKeyboard = false
                                    // 先清除焦点再重新获取，确保键盘正确显示
                                    focusManager.clearFocus(true)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        focusRequester.requestFocus()
                                    }, 50)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        refreshKeyboard = true
                                    }, 100)
                                }
                            }, keyboardOptions = keyboardOptions, textStyle = TextStyle.Default.copy(
                            color = colorResource(R.color.color_333333), // 深灰色
                            fontSize = 16.sp, textAlign = TextAlign.End // 文本右对齐
                        ), decorationBox = { innerTextField ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(contentAlignment = Alignment.CenterEnd) {
                                    // 占位文本（当输入为空时显示）
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = "请输入", color = colorResource(R.color.color_83878C), // 浅灰色
                                            textAlign = TextAlign.End, fontSize = 16.sp
                                        )
                                    }
                                    // 输入框内容
                                    innerTextField()
                                }
                                // 单位文本（可选）
                                Text(
                                    text = if (showInputTextUnit) " 元" else "", color = colorResource(R.color.color_333333), fontSize = 16.sp
                                )
                            }
                        })
                }

                // 颜色指示器
                if (showColor && inputColor != null) {
                    ColoredCircleWithBorder(color = inputColor)
                }

                // 右侧箭头图标
                if (showRightArrow) {
                    Image(
                        painter = painterResource(R.mipmap.icon_right), contentDescription = "更多", // 无障碍描述
                        modifier = Modifier.padding(start = 9.dp)
                    )
                }
            }
        }

        // 底部分割线
        if (showDivider) {
            HorizontalDivider(
                thickness = 0.5.dp, color = colorResource(R.color.color_D9E1EB), // 浅蓝色分割线
                modifier = Modifier.padding(horizontal = dividerPadding)
            )
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览选项菜单项
 */
@Composable @Preview(showBackground = true) fun ItemOptionMenuPreview() {
    HomeBookTheme {
        ItemOptionMenu(
            "付款方式",                           // 标题
            modifier = Modifier.padding(16.dp),  // 内边距
            showRightArrow = false,              // 不显示右侧箭头
            showTextField = true,                // 显示输入框
            showColor = false,                   // 不显示颜色指示器
            showText = false,                    // 不显示右侧文本
            inputText = "山姆",                  // 输入框初始文本
            rightText = "测试",                  // 右侧文本（但showText为false，不显示）
            inputColor = Color.Red               // 颜色（但showColor为false，不显示）
        )
    }
}