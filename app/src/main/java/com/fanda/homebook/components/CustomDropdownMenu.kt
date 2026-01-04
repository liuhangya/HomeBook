package com.fanda.homebook.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.fanda.homebook.R

// 自定义 Popup 菜单组件
@Composable fun CustomDropdownMenu(
    dpOffset: DpOffset,
    modifier: Modifier = Modifier,
    expanded: Boolean, onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit,
) {
    if (expanded) {
        BackHandler { onDismissRequest() }
    }
    if (!expanded) {
        return
    }
    // 获取密度以转换 dp → px
    val density = LocalDensity.current
    val offset = with(density) {
        IntOffset(
            x = dpOffset.x.roundToPx(), y = dpOffset.y.roundToPx() // 向下偏移 48dp
        )
    }
    Log.d("CustomDropdownMenu", "offset: $offset")
    Popup(
        onDismissRequest = onDismissRequest, properties = PopupProperties(
            dismissOnClickOutside = true, dismissOnBackPress = true
        ), offset = offset
    ) {
        AnimatedVisibility(
            visible = expanded, enter = fadeIn(animationSpec = tween(0)), exit = fadeOut(animationSpec = tween(200))
        ) {
            // 渐变背景 + 圆角容器
            Column(
                modifier = modifier
                    .width(136.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.color_E3EBF5), Color.White
                            )
                        )
                    )
                    .border(1.dp, Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

// 自定义菜单项
@Composable fun MenuItem(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(end = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text, color = Color.Black, fontSize = 14.sp, style = TextStyle.Default, modifier = Modifier

                .padding(horizontal = 16.dp, vertical = 9.dp)
        )
        if (selected) {
            Image(
                painter = painterResource(id = R.mipmap.icon_selected), contentDescription = null, modifier = Modifier.size(14.dp)
            )
        }
    }

}

@Composable @Preview(showBackground = true) fun CustomGradientDropdownMenuPreview() {
    CustomDropdownMenu(dpOffset = DpOffset(0.dp, 0.dp), expanded = true, onDismissRequest = {}) {
        MenuItem(text = "菜单项1", selected = true) {}
        MenuItem(text = "菜单项2", selected = false) {}
        MenuItem(text = "菜单项3", selected = false) {}
    }
}