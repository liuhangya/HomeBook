package com.fanda.homebook.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R


@Composable fun SelectTypeWidget(
    firstType: String,
    secondType: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    dividerPadding: Dp = 20.dp,
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(
            // 去掉默认的点击效果
//            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) {
            onClick?.invoke()
        }) {
        Column(
            verticalArrangement = Arrangement.Center, modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    style = TextStyle.Default, text = "分类", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))

                if (firstType.isNotEmpty() && secondType.isNotEmpty()) {
                    Text(
                        style = TextStyle.Default, text = firstType, color = colorResource(R.color.color_333333), fontSize = 16.sp
                    )
                    Image(
                        painter = painterResource(R.mipmap.icon_right),
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 7.dp),
                        colorFilter = ColorFilter.tint(colorResource(R.color.color_CFD5DE))
                    )
                    Text(
                        style = TextStyle.Default, text = secondType, color = colorResource(R.color.color_333333), fontSize = 16.sp
                    )
                    Image(painter = painterResource(R.mipmap.icon_right), contentDescription = null, modifier = Modifier.padding(start = 9.dp))
                } else {
                    Image(painter = painterResource(R.mipmap.icon_right), contentDescription = null)
                }
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp, color = colorResource(R.color.color_D9E1EB), modifier = Modifier.padding(horizontal = dividerPadding)
        )
    }
}