package com.fanda.homebook.quick.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fanda.homebook.components.EditCommentsWidget
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.SelectTypeWidget
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.ui.theme.HomeBookTheme


@Composable
fun EditClosetScreen(
    showSyncCloset: Boolean,
    bottomComment: String,
    bottomStockComment: String,
    modifier: Modifier = Modifier,
    closetCategory: String = "",
    closetSubCategory: String = "",
    product: String = "",
    stockProduct: String = "",
    color: Long = -1,
    season: String = "",
    size: String = "",
    owner: String = "",
    name: String = "",
    goodsRack: String = "",
    stockCategory: String = "",
    period: String = "",
    onCheckedChange: (Boolean) -> Unit,
    onBottomCommentChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onClick: (ShowBottomSheetType) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val itemPadding = Modifier.padding(
        20.dp, 20.dp, 20.dp, 20.dp
    )

    // 包装原始点击事件，先关闭键盘
    val wrapClick: (ShowBottomSheetType, (ShowBottomSheetType) -> Unit) -> Unit =
        { type, original ->
            focusManager.clearFocus()
            original(type)

        }
    Column {
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                ItemOptionMenu(
                    title = "同步至衣橱",
                    showSwitch = true,
                    showRightArrow = false,
                    showDivider = showSyncCloset,
                    checked = showSyncCloset,
                    removeIndication = true,
                    modifier = Modifier
                        .height(63.dp)
                        .padding(horizontal = 20.dp),
                    onCheckedChange = {
                        Log.d("QuickHomePage", "同步至衣橱：$it")
                        focusManager.clearFocus()
                        onCheckedChange(it)
                    },
                )
                if (showSyncCloset) {
                    ItemOptionMenu(
                        title = "归属",
                        showText = true,
                        rightText = owner,
                        showDivider = true,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.OWNER, onClick) })
                    SelectTypeWidget(
                        firstType = closetCategory,
                        secondType = closetSubCategory,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.CATEGORY, onClick) })
                    ItemOptionMenu(
                        title = "颜色",
                        showColor = true,
                        inputColor = if (color != -1L) Color(color) else null,
                        showDivider = true,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.COLOR, onClick) })
                    ItemOptionMenu(
                        title = "季节",
                        showText = true,
                        rightText = season,
                        showDivider = true,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.SEASON, onClick) })
                    ItemOptionMenu(
                        title = "品牌",
                        showText = true,
                        rightText = product,
                        showDivider = true,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.PRODUCT, onClick) })
                    ItemOptionMenu(
                        title = "尺码",
                        showText = true,
                        rightText = size,
                        showDivider = true,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.SIZE, onClick) })
                    EditCommentsWidget(
                        inputText = bottomComment,
                        modifier = itemPadding,
                        onValueChange = onBottomCommentChange
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                ItemOptionMenu(
                    title = "同步至囤货",
                    showSwitch = true,
                    showRightArrow = false,
                    showDivider = !showSyncCloset,
                    removeIndication = true,
                    checked = !showSyncCloset,
                    modifier = Modifier
                        .height(63.dp)
                        .padding(horizontal = 20.dp),
                    onCheckedChange = {
                        onCheckedChange(!it)
                    },
                )
                if (!showSyncCloset) {
                    ItemOptionMenu(
                        title = "名称",
                        showTextField = true,
                        showRightArrow = false,
                        removeIndication = true,
                        inputText = name,
                        showDivider = true,
                        modifier = itemPadding,
                        onValueChange = onNameChange
                    )
                    ItemOptionMenu(
                        title = "品牌",
                        showText = true,
                        rightText = stockProduct,
                        showDivider = true,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.STOCK_PRODUCT, onClick) })
                    ItemOptionMenu(
                        title = "货架",
                        showText = true,
                        rightText = goodsRack,
                        showDivider = true,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.GOODS_RACK, onClick) })
                    ItemOptionMenu(
                        title = "类别",
                        showText = true,
                        rightText = stockCategory,
                        showDivider = true,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.STOCK_CATEGORY, onClick) })
                    ItemOptionMenu(
                        title = "使用时段",
                        showText = true,
                        rightText = period,
                        showDivider = true,
                        modifier = itemPadding,
                        onClick = { wrapClick(ShowBottomSheetType.PERIOD, onClick) })
                    EditCommentsWidget(
                        inputText = bottomStockComment,
                        modifier = itemPadding,
                        onValueChange = onBottomCommentChange
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun EditClosetScreenPreview() {
    HomeBookTheme {
        EditClosetScreen(
            showSyncCloset = true,
            bottomComment = "",
            bottomStockComment = "",
            onCheckedChange = {},
            onBottomCommentChange = {},
            onClick = {},
            onNameChange = {}
        )
    }
}
