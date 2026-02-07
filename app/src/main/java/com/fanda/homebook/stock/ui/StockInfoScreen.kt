package com.fanda.homebook.stock.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fanda.homebook.components.EditCommentsWidget
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.tools.isValidDecimalInput
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 库存物品详细信息屏幕组件
 * 显示和编辑库存物品的详细信息，包括价格、日期、分类等
 *
 * @param bottomComment 底部备注/评论内容
 * @param modifier Compose修饰符，用于调整布局样式
 * @param subCategory 子分类名称
 * @param product 品牌/产品名称
 * @param usagePeriod 使用时段
 * @param date 购入时间
 * @param openDate 开封日期
 * @param expireDate 过期日期
 * @param syncBook 是否同步至当日账单的开关状态
 * @param showSyncBook 是否显示同步至账单选项
 * @param shelfMonth 开封后保鲜期（月数）
 * @param price 价格文本
 * @param isEditState 是否为编辑状态
 * @param onCheckedChange 同步至账单开关状态变化回调
 * @param onBottomCommentChange 底部备注内容变化回调
 * @param onPriceChange 价格文本变化回调
 * @param onClick 菜单项点击回调，参数为对应的弹窗类型
 */
@Composable fun StockInfoScreen(
    bottomComment: String,
    modifier: Modifier = Modifier,
    subCategory: String = "",
    product: String = "",
    usagePeriod: String = "",
    date: String = "",
    openDate: String = "",
    expireDate: String = "",
    syncBook: Boolean,
    showSyncBook: Boolean = true,
    shelfMonth: Int = 0,
    price: String = "",
    isEditState: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    onBottomCommentChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onClick: (ShowBottomSheetType) -> Unit,
) {
    // 获取焦点管理器，用于关闭软键盘
    val focusManager = LocalFocusManager.current

    // 统一的内部元素内边距
    val itemPadding = Modifier.padding(
        start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp
    )

    // 包装原始点击事件，先关闭键盘再执行原始点击逻辑
    val wrapClick: (ShowBottomSheetType, (ShowBottomSheetType) -> Unit) -> Unit = { type, original ->
        focusManager.clearFocus()  // 点击时先关闭软键盘
        original(type)             // 然后执行原始点击逻辑
    }

    Column {
        // 第一组：价格和购入信息（带圆角边框的容器）
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                // 同步至当日账单选项（可选显示）
                if (showSyncBook) {
                    ItemOptionMenu(
                        title = "同步至当日账单",
                        showSwitch = true,
                        showRightArrow = false,
                        isEditState = isEditState,
                        showDivider = true,
                        checked = syncBook,
                        removeIndication = true,  // 移除点击效果指示器
                        modifier = Modifier
                            .height(63.dp)        // 固定高度
                            .padding(horizontal = 20.dp),
                        onCheckedChange = {
                            focusManager.clearFocus()  // 开关状态变化时关闭键盘
                            onCheckedChange(it)
                        },
                    )
                }

                // 价格输入项
                ItemOptionMenu(
                    title = "价格", showTextField = true, isEditState = isEditState, showRightArrow = false, removeIndication = true, inputText = price.ifEmpty { "" },  // 空字符串处理
                    showDivider = true, showInputTextUnit = true,  // 显示单位（如"元"）
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,  // 十进制数字键盘
                        imeAction = ImeAction.Done           // 键盘完成按钮
                    ), modifier = itemPadding, onValueChange = { newText ->
                        // 🔒 限制只能输入数字和一个小数点
                        if (isValidDecimalInput(newText)) {
                            onPriceChange(newText)
                        }
                        // 否则忽略非法输入
                    })

                // 购入时间选择项
                ItemOptionMenu(
                    title = "购入时间", showText = true, rightText = date, showDivider = true, modifier = itemPadding, onClick = {
                        wrapClick(ShowBottomSheetType.BUY_DATE, onClick)
                    })
            }
        }

        // 间距
        Spacer(modifier = Modifier.height(12.dp))

        // 第二组：分类和使用信息（带圆角边框的容器）
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                // 品牌/产品选择项
                ItemOptionMenu(
                    title = "品牌", showText = true, rightText = product, showDivider = true, modifier = itemPadding, onClick = {
                        wrapClick(ShowBottomSheetType.PRODUCT, onClick)
                    })

                // 类别/子分类选择项
                ItemOptionMenu(
                    title = "类别", showText = true, rightText = subCategory, showDivider = true, modifier = itemPadding, onClick = {
                        wrapClick(ShowBottomSheetType.CATEGORY, onClick)
                    })

                // 使用时段选择项
                ItemOptionMenu(
                    title = "使用时段", showText = true, rightText = usagePeriod, showDivider = true, modifier = itemPadding, onClick = {
                        wrapClick(ShowBottomSheetType.USAGE_PERIOD, onClick)
                    })

                // 开封日期选择项
                ItemOptionMenu(
                    title = "开封日期", showText = true, rightText = openDate, showDivider = true, modifier = itemPadding, onClick = {
                        wrapClick(ShowBottomSheetType.OPEN_DATE, onClick)
                    })

                // 开封后保鲜期选择项
                ItemOptionMenu(
                    title = "开封后保鲜期", showText = true, rightText = if (shelfMonth > 0) "${shelfMonth}个月" else "",  // 格式化显示
                    showDivider = true, modifier = itemPadding, onClick = {
                        wrapClick(ShowBottomSheetType.SHELF_MONTH, onClick)
                    })

                // 过期日期选择项
                ItemOptionMenu(
                    title = "过期日期", showText = true, rightText = expireDate, showDivider = true, modifier = itemPadding, onClick = {
                        wrapClick(ShowBottomSheetType.EXPIRE_DATE, onClick)
                    })

                // 底部备注/评论编辑组件
                EditCommentsWidget(
                    isEditState = isEditState, inputText = bottomComment, modifier = itemPadding, onValueChange = onBottomCommentChange
                )
            }
        }
    }
}

/**
 * 预览函数 - 用于Android Studio的Compose预览
 *
 * @see StockInfoScreen 查看完整参数说明
 */
@Composable @Preview(showBackground = true) fun StockInfoScreenPreview() {
    HomeBookTheme {
        StockInfoScreen(syncBook = true, bottomComment = "", onCheckedChange = {}, onBottomCommentChange = {}, onClick = {}, onPriceChange = {})
    }
}