package com.fanda.homebook.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.ui.theme.HomeBookTheme

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

    Column(modifier = Modifier
        .fillMaxWidth()
        .then(if (removeIndication) {
            Modifier.clickable(
                // 去掉默认的点击效果
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) {
                onClick?.invoke()
            }
        } else {
            Modifier.clickable {
                onClick?.invoke()
            }
        })) {
        Column(
            verticalArrangement = Arrangement.Center, modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    style = TextStyle.Default, text = title, color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (showSwitch) {
                    Switch(
                        modifier = Modifier.scale(0.8f),enabled = isEditState,  checked = checked, onCheckedChange = {
                            onCheckedChange?.invoke(it)
                        }, colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black,
                            uncheckedThumbColor = Color.White,
                            uncheckedBorderColor = Color.Transparent,
                            uncheckedTrackColor = colorResource(R.color.color_CBD4E0),
                        )
                    )
                }

                if (showText) {
                    Text(
                        style = TextStyle.Default,
                        text = rightText,
                        color = colorResource(R.color.color_333333),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = if (showRightArrow) 0.dp else 10.dp)
                    )
                }
                if (showPlus) {
                    SelectableRoundedButton(text = "+1", selected = false, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp), onClick = { onPlusClick?.invoke() }, interaction = true)
                }

                if (showTextField) {
                    BasicTextField(enabled = isEditState, value = inputText, onValueChange = { newText ->
                        // 否则忽略非法输入
                        onValueChange?.invoke(newText)
                    }, singleLine = true, modifier = Modifier.wrapContentWidth(Alignment.End), keyboardOptions = keyboardOptions, textStyle = TextStyle.Default.copy(
                        color = colorResource(R.color.color_333333), fontSize = 16.sp, textAlign = TextAlign.End
                    ), decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(contentAlignment = Alignment.CenterEnd) {
                                // 占位文本
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "请输入", color = colorResource(R.color.color_83878C), textAlign = TextAlign.End
                                    )
                                }
                                // 输入框内容
                                innerTextField()
                            }
                            if (showInputTextUnit) {
                                Text(text = " 元", color = colorResource(R.color.color_333333), fontSize = 16.sp)
                            }
                        }
                    })
                }

                if (showColor && inputColor != null) {
                    ColoredCircleWithBorder(color = inputColor)
                }

                if (showRightArrow) {
                    Image(painter = painterResource(R.mipmap.icon_right), contentDescription = null, modifier = Modifier.padding(start = 9.dp))
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(thickness = 0.5.dp, color = colorResource(R.color.color_D9E1EB), modifier = Modifier.padding(horizontal = dividerPadding))
        }
    }

}


@Composable @Preview(showBackground = true) fun ItemOptionMenuPreview() {
    HomeBookTheme {
        ItemOptionMenu(
            "付款方式",
            modifier = Modifier.padding(16.dp),
            showRightArrow = false,
            showTextField = true,
            showColor = false,
            showText = false,
            inputText = "山姆",
            rightText = "测试",
            inputColor = Color.Red
        )
    }
}