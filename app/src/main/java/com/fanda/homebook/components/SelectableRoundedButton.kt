package com.fanda.homebook.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R

@Composable fun SelectableRoundedButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    cornerSize: Dp = 4.dp,
    fontSize: TextUnit = 14.sp,
    selectedBackgroundColor: Color = Color.Black,
    unselectedBackgroundColor: Color = colorResource(R.color.color_D7DEE9),
    selectedContentColor: Color = Color.White,
    unselectedContentColor: Color = Color.Black,
    @DrawableRes imageRes: Int? = null
) {
    // 给 Box 裁剪成圆角矩形并添加点击事件
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerSize))
            .background(if (selected) selectedBackgroundColor else unselectedBackgroundColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(contentPadding)
        ) {
            Text(
                text = text, fontSize = fontSize, color = if (selected) selectedContentColor else unselectedContentColor, style = TextStyle.Default,  // 用默认样式，才不会有字体的间距
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
            if (imageRes != null) {
                Image(
                    painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }

    // Surface 默认会确保其 可点击区域至少 ≥ 48dp ，小控件不要用这种方式实现，不然间距不对
//    Surface(
//        onClick = onClick,
//        modifier = modifier,
//        shape = RoundedCornerShape(cornerSize),
//        color = if (selected) selectedBackgroundColor else unselectedBackgroundColor,
//        contentColor = if (selected) selectedContentColor else unselectedContentColor,
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(contentPadding)) {
//            Text(text = text, fontSize = fontSize, style = TextStyle.Default, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,modifier = modifier)
//            if (imageRes != null){
//                Image(painter = painterResource(id = imageRes), contentDescription = null,modifier = Modifier.padding(end = 8.dp))
//            }
//        }
//    }
}