package com.fanda.homebook.closet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fanda.homebook.components.EditCommentsWidget
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.SelectTypeWidget
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.tools.isValidDecimalInput
import com.fanda.homebook.ui.theme.HomeBookTheme


@Composable fun ClosetInfoScreen(
    bottomComment: String,
    modifier: Modifier = Modifier,
    closetCategory: String = "",
    closetSubCategory: String = "",
    product: String = "",
    color: Long = -1,
    season: String = "",
    size: String = "",
    date: String = "",
    syncBook: Boolean,
    price: String = "",
    onCheckedChange: (Boolean) -> Unit,
    onBottomCommentChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onClick: (ShowBottomSheetType) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val itemPadding = Modifier.padding(
        20.dp, 20.dp, 20.dp, 20.dp
    )

    // 包装原始点击事件，先关闭键盘
    val wrapClick: (ShowBottomSheetType, (ShowBottomSheetType) -> Unit) -> Unit = { type, original ->
        focusManager.clearFocus()
        original(type)

    }
    Column {
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                ItemOptionMenu(
                    title = "同步至当日账单",
                    showSwitch = true,
                    showRightArrow = false,
                    showDivider = true,
                    checked = syncBook,
                    removeIndication = true,
                    modifier = Modifier
                        .height(63.dp)
                        .padding(horizontal = 20.dp),
                    onCheckedChange = {
                        focusManager.clearFocus()
                        onCheckedChange(it)
                    },
                )
                ItemOptionMenu(
                    title = "价格", showTextField = true, showRightArrow = false, removeIndication = true, inputText = price.ifEmpty {
                        ""
                    }, showDivider = true, showInputTextUnit = true, keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                    ), modifier = itemPadding, onValueChange = { newText ->
                        // 🔒 限制只能输入数字和一个小数点
                        if (isValidDecimalInput(newText)) {
                            onPriceChange(newText)
                        }
                        // 否则忽略非法输入
                    })
                ItemOptionMenu(title = "购入时间", showText = true, rightText = date, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.BUY_DATE, onClick) })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                SelectTypeWidget(firstType = closetCategory, secondType = closetSubCategory, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.CATEGORY, onClick) })
                ItemOptionMenu(
                    title = "颜色",
                    showColor = true,
                    inputColor = if (color != -1L) Color(color) else null,
                    showDivider = true,
                    modifier = itemPadding,
                    onClick = { wrapClick(ShowBottomSheetType.COLOR, onClick) })
                ItemOptionMenu(title = "季节", showText = true, rightText = season, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.SEASON, onClick) })
                ItemOptionMenu(title = "品牌", showText = true, rightText = product, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.PRODUCT, onClick) })
                ItemOptionMenu(title = "尺码", showText = true, rightText = size, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.SIZE, onClick) })
                EditCommentsWidget(
                    inputText = bottomComment, modifier = itemPadding, onValueChange = onBottomCommentChange
                )
            }
        }
    }
}

@Composable @Preview(showBackground = true) fun EditClosetScreenPreview() {
    HomeBookTheme {
        ClosetInfoScreen(syncBook = true, bottomComment = "", onCheckedChange = {}, onBottomCommentChange = {}, onClick = {}, onPriceChange = {})
    }
}
