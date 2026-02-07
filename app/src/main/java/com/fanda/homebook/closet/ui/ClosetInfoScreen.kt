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
import com.fanda.homebook.components.SelectClosetCategoryWidget
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.tools.isValidDecimalInput
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 衣橱信息屏幕组件
 *
 * 用于显示和编辑衣橱物品的详细信息
 *
 * @param bottomComment 底部备注信息
 * @param modifier 修饰符，用于自定义整体布局
 * @param closetCategory 一级分类名称
 * @param closetSubCategory 二级分类名称
 * @param product 品牌/产品名称
 * @param color 颜色值（Long类型颜色值）
 * @param season 季节信息
 * @param size 尺码信息
 * @param date 购买日期
 * @param syncBook 是否同步到账单状态
 * @param showSyncBook 是否显示同步到账单选项，默认为true
 * @param price 价格信息
 * @param isEditState 是否处于编辑状态，默认为true
 * @param onCheckedChange 同步到账单状态变化回调
 * @param onBottomCommentChange 备注信息变化回调
 * @param price 价格信息
 * @param isEditState 是否处于编辑状态
 * @param onCheckedChange 同步到账单状态变化回调
 * @param onBottomCommentChange 备注信息变化回调
 * @param onPriceChange 价格变化回调
 * @param onClick 选项点击回调，传递点击的底部弹窗类型
 */
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
    showSyncBook: Boolean = true,
    price: String = "",
    isEditState: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    onBottomCommentChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onClick: (ShowBottomSheetType) -> Unit,
) {
    // 获取焦点管理器，用于关闭软键盘
    val focusManager = LocalFocusManager.current

    // 统一的内边距修饰符
    val itemPadding = Modifier.padding(
        20.dp, 20.dp, 20.dp, 20.dp
    )

    /**
     * 包装点击事件：先关闭键盘，再执行原始点击回调
     *
     * @param type 点击的底部弹窗类型
     * @param original 原始点击回调函数
     */
    val wrapClick: (ShowBottomSheetType, (ShowBottomSheetType) -> Unit) -> Unit = { type, original ->
        focusManager.clearFocus()  // 先关闭键盘
        original(type)             // 再执行原始回调
    }

    Column {
        // 第一个渐变卡片：价格和购买时间
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                // 同步到账单选项（可控制显示/隐藏）
                if (showSyncBook) {
                    ItemOptionMenu(
                        title = "同步至当日账单",
                        showSwitch = true,
                        showRightArrow = false,
                        isEditState = isEditState,
                        showDivider = true,
                        checked = syncBook,
                        removeIndication = true,  // 移除点击效果
                        modifier = Modifier
                            .height(63.dp)       // 固定高度
                            .padding(horizontal = 20.dp),
                        onCheckedChange = {
                            focusManager.clearFocus()
                            onCheckedChange(it)
                        },
                    )
                }

                // 价格输入项
                ItemOptionMenu(
                    title = "价格", showTextField = true, isEditState = isEditState, showRightArrow = false, removeIndication = true, inputText = price.ifEmpty { "" },  // 处理空值
                    showDivider = true, showInputTextUnit = true,          // 显示"元"单位
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,  // 小数键盘
                        imeAction = ImeAction.Done           // 完成动作
                    ), modifier = itemPadding, onValueChange = { newText ->
                        // 验证输入：只允许数字和小数点
                        if (isValidDecimalInput(newText)) {
                            onPriceChange(newText)
                        }
                    })

                // 购买时间选择项
                ItemOptionMenu(
                    title = "购入时间", showText = true, rightText = date, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.BUY_DATE, onClick) })
            }
        }

        // 卡片间距
        Spacer(modifier = Modifier.height(12.dp))

        // 第二个渐变卡片：分类、颜色、季节等详细信息
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                // 分类选择组件（一级和二级分类）
                SelectClosetCategoryWidget(
                    firstType = closetCategory, secondType = closetSubCategory, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.CATEGORY, onClick) })

                // 颜色选择项
                ItemOptionMenu(
                    title = "颜色", showColor = true, inputColor = if (color != -1L) Color(color) else null,  // 转换颜色值
                    showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.COLOR, onClick) })

                // 季节选择项
                ItemOptionMenu(
                    title = "季节", showText = true, rightText = season, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.SEASON, onClick) })

                // 品牌/产品选择项
                ItemOptionMenu(
                    title = "品牌", showText = true, rightText = product, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.PRODUCT, onClick) })

                // 尺码选择项
                ItemOptionMenu(
                    title = "尺码", showText = true, rightText = size, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.SIZE, onClick) })

                // 备注输入组件
                EditCommentsWidget(
                    isEditState = isEditState, inputText = bottomComment, modifier = itemPadding, onValueChange = onBottomCommentChange
                )
            }
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览衣橱信息屏幕
 */
@Composable @Preview(showBackground = true) fun EditClosetScreenPreview() {
    HomeBookTheme {
        ClosetInfoScreen(syncBook = true, bottomComment = "", onCheckedChange = {}, onBottomCommentChange = {}, onClick = {}, onPriceChange = {})
    }
}