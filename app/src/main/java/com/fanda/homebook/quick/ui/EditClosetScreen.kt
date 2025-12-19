package com.fanda.homebook.quick.ui

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.ui.theme.HomeBookTheme


@Composable fun EditClosetScreen(modifier: Modifier = Modifier, showSyncCloset: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val itemPadding = Modifier.padding(
        20.dp, 0.dp, 10.dp, 20.dp
    )
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
                    onCheckedChange(it)
                },
            )
            if (showSyncCloset) {
                ItemOptionMenu(
                    title = "归属", showText = true, rightText = "嘟嘟", showDivider = true, modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }
                EditTypeWidget(title = "分类", firstType = "上装", secondType = "短袖" , modifier = itemPadding){
                    Log.d("QuickHomePage", "点击了分类")
                }

                ItemOptionMenu(
                    title = "颜色", showColor = true, inputColor = Color.Red, showDivider = true, modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了付款方式")
                }
                ItemOptionMenu(
                    title = "季节", showText = true, rightText = "春秋", showDivider = true, modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }
                ItemOptionMenu(
                    title = "品牌", showText = true, rightText = "耐克", showDivider = true, modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }
                ItemOptionMenu(
                    title = "尺码", showText = true, rightText = "S", showDivider = true, modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }
            }

        }

    }
}

@Composable private fun EditTypeWidget(modifier: Modifier = Modifier, title: String, firstType: String, secondType: String, onClick: (() -> Unit)? = null) {
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
                Image(painter = painterResource(R.mipmap.icon_right), contentDescription = null, colorFilter = ColorFilter.tint(colorResource(R.color.color_CFD5DE)))
                Text(
                    style = TextStyle.Default, text = secondType, color = colorResource(R.color.color_333333), fontSize = 16.sp
                )
                Image(painter = painterResource(R.mipmap.icon_right), contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.padding(top = 20.dp))
        HorizontalDivider(thickness = 0.5.dp, color = colorResource(R.color.color_D9E1EB), modifier = Modifier.padding(end = 10.dp))
    }
}

@Composable @Preview(showBackground = true) fun EditTypeWidgetPreview() {
    HomeBookTheme {
        EditTypeWidget(Modifier.padding(20.dp), "分类", "上装", "短袖") {
            Log.d("QuickHomePage", "点击了分类")
        }
    }
}
