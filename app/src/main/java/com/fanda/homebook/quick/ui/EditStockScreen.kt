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
import com.fanda.homebook.components.EditCommentsWidget
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.ui.theme.HomeBookTheme


@Composable
fun EditStockScreen(
    modifier: Modifier = Modifier,
    showSyncStock: Boolean,
    bottomComment: String,
    onCheckedChange: (Boolean) -> Unit,
    onBottomCommentChange: (String) -> Unit
) {
    val itemPadding = Modifier.padding(
        20.dp, 0.dp, 10.dp, 20.dp
    )
    GradientRoundedBoxWithStroke(modifier = modifier) {
        Column {
            ItemOptionMenu(
                title = "同步至囤货",
                showSwitch = true,
                showRightArrow = false,
                showDivider = showSyncStock,
                checked = showSyncStock,
                dividerPadding = 5.dp,
                modifier = Modifier.padding(
                    20.dp, 10.dp, 10.dp, if (showSyncStock) 22.dp else 10.dp
                ),
                onCheckedChange = {
                    Log.d("QuickHomePage", "同步至衣橱：$it")
                    onCheckedChange(it)
                },
            )
            if (showSyncStock) {
                ItemOptionMenu(
                    title = "名称",
                    showText = true,
                    showRightArrow = false,
                    rightText = "嘟嘟",
                    showDivider = true,
                    modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }

                ItemOptionMenu(
                    title = "品牌",
                    showText = true,
                    rightText = "潘婷",
                    showDivider = true,
                    modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }
                ItemOptionMenu(
                    title = "货架",
                    showText = true,
                    rightText = "梳妆台",
                    showDivider = true,
                    modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }
                ItemOptionMenu(
                    title = "类别",
                    showText = true,
                    rightText = "护发",
                    showDivider = true,
                    modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }
                ItemOptionMenu(
                    title = "使用时段",
                    showText = true,
                    rightText = "全天",
                    showDivider = true,
                    modifier = itemPadding
                ) {
                    Log.d("QuickHomePage", "点击了归属")
                }
                EditCommentsWidget( inputText = bottomComment, modifier = Modifier.padding(20.dp, 0.dp, 10.dp, 0.dp), onValueChange = onBottomCommentChange)
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun EditStockScreenPreview() {
    HomeBookTheme {
        EditStockScreen(showSyncStock = true,bottomComment = "", onCheckedChange = {
        }, onBottomCommentChange = {})
    }
}
