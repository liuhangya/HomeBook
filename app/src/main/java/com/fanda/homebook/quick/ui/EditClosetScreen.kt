package com.fanda.homebook.quick.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.ui.theme.HomeBookTheme


@Composable fun EditClosetScreen(
    showSyncCloset: Boolean,
    bottomComment: String,
    modifier: Modifier = Modifier,
    closetCategory: String = "",
    closetSubCategory: String = "",
    product: String = "",
    color: Long = -1,
    season: String = "",
    size: String = "",
    onCheckedChange: (Boolean) -> Unit,
    onBottomCommentChange: (String) -> Unit,
    onClosetCategoryClick: (() -> Unit),
    onProductClick: (() -> Unit),
    onColorClick: (() -> Unit),
    onSeasonClick: (() -> Unit),
    onSizeClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val itemPadding = Modifier.padding(
        20.dp, 0.dp, 10.dp, 20.dp
    )

    // 包装原始点击事件，先关闭键盘
    val wrapClick = { original: () -> Unit ->
        focusManager.clearFocus()
        original()
    }

    GradientRoundedBoxWithStroke(modifier = modifier) {
        Column {
            ItemOptionMenu(
                title = "同步至衣橱",
                showSwitch = true,
                showRightArrow = false,
                showDivider = showSyncCloset,
                checked = showSyncCloset,
                dividerPadding = 5.dp,
                modifier = Modifier.padding(
                    20.dp, 10.dp, 10.dp, if (showSyncCloset) 22.dp else 10.dp
                ),
                onCheckedChange = {
                    Log.d("QuickHomePage", "同步至衣橱：$it")
                    focusManager.clearFocus()
                    onCheckedChange(it)
                },
            )
            if (showSyncCloset) {
                ItemOptionMenu(
                    title = "归属", showText = true, rightText = "嘟嘟", showDivider = true, modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }
                SelectTypeWidget(title = "分类", firstType = closetCategory, secondType = closetSubCategory, modifier = itemPadding, onClick = { wrapClick(onClosetCategoryClick) })

                ItemOptionMenu(title = "颜色",
                    showColor = true,
                    inputColor = if (color != -1L) Color(color) else null,
                    showDivider = true,
                    modifier = itemPadding,
                    onClick = { wrapClick(onColorClick) })
                ItemOptionMenu(title = "季节", showText = true, rightText = season, showDivider = true, modifier = itemPadding, onClick = { wrapClick(onSeasonClick) })
                ItemOptionMenu(title = "品牌", showText = true, rightText = product, showDivider = true, modifier = itemPadding, onClick = { wrapClick(onProductClick) })
                ItemOptionMenu(title = "尺寸", showText = true, rightText = size, showDivider = true, modifier = itemPadding, onClick = { wrapClick(onSizeClick) })
                EditCommentsWidget(inputText = bottomComment, modifier = Modifier.padding(20.dp, 0.dp, 10.dp, 0.dp), onValueChange = onBottomCommentChange)
            }
        }
    }
}

@Composable private fun EditCommentsWidget(
    modifier: Modifier = Modifier, inputText: String = "", onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center, modifier = modifier
    ) {
        Text(
            style = TextStyle.Default, text = "备注", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
        )
        BasicTextField(value = inputText, onValueChange = { newText ->
            // 否则忽略非法输入
            onValueChange(newText)
        }, singleLine = true, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 22.dp), keyboardOptions = KeyboardOptions.Default.copy(
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


@Composable private fun SelectTypeWidget(
    title: String, firstType: String, secondType: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.Center, modifier = modifier
        .fillMaxWidth()
        .clickable(
            // 去掉默认的点击效果
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) {
            onClick?.invoke()
        }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                style = TextStyle.Default, text = title, color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))

            if (firstType.isNotEmpty() && secondType.isNotEmpty()) {
                Text(
                    style = TextStyle.Default, text = firstType, color = colorResource(R.color.color_333333), fontSize = 16.sp
                )
                Image(
                    painter = painterResource(R.mipmap.icon_right), contentDescription = null, colorFilter = ColorFilter.tint(colorResource(R.color.color_CFD5DE))
                )
                Text(
                    style = TextStyle.Default, text = secondType, color = colorResource(R.color.color_333333), fontSize = 16.sp
                )
                Image(painter = painterResource(R.mipmap.icon_right), contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.padding(top = 20.dp))
        HorizontalDivider(
            thickness = 0.5.dp, color = colorResource(R.color.color_D9E1EB), modifier = Modifier.padding(end = 10.dp)
        )
    }
}

@Composable @Preview(showBackground = true) fun EditClosetScreenPreview() {
    HomeBookTheme {
        EditClosetScreen(
            showSyncCloset = true,
            bottomComment = "",
            onCheckedChange = {},
            onBottomCommentChange = {},
            onClosetCategoryClick = {},
            onProductClick = {},
            onColorClick = {},
            onSeasonClick = {},
            onSizeClick = {},
        )
    }
}
