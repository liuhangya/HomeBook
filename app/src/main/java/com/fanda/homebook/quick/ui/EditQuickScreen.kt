package com.fanda.homebook.quick.ui

import androidx.compose.foundation.layout.Column
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
import com.fanda.homebook.components.SelectClosetCategoryWidget
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 编辑快速记账-衣橱同步屏幕
 * 用于处理与衣橱相关的同步设置和数据输入
 *
 * @param showSyncCloset 是否显示衣橱同步选项
 * @param bottomComment 底部备注文本
 * @param modifier 修饰符
 * @param closetCategory 衣橱分类
 * @param closetSubCategory 衣橱子分类
 * @param product 产品/品牌
 * @param color 颜色（Long类型颜色值）
 * @param season 季节
 * @param size 尺码
 * @param owner 归属人
 * @param onCheckedChange 同步开关状态变化回调
 * @param onBottomCommentChange 底部备注文本变化回调
 * @param onClick 选项点击回调（带弹窗类型参数）
 */
@Composable fun EditQuickClosetScreen(
    showSyncCloset: Boolean,
    bottomComment: String,
    modifier: Modifier = Modifier,
    closetCategory: String = "",
    closetSubCategory: String = "",
    product: String = "",
    color: Long = -1,
    season: String = "",
    size: String = "",
    owner: String = "",
    onCheckedChange: (Boolean) -> Unit,
    onBottomCommentChange: (String) -> Unit,
    onClick: (ShowBottomSheetType) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val itemPadding = Modifier.padding(
        20.dp, 20.dp, 20.dp, 20.dp  // 统一的选项内边距
    )

    // 包装原始点击事件，点击前先关闭键盘
    val wrapClick: (ShowBottomSheetType, (ShowBottomSheetType) -> Unit) -> Unit = { type, original ->
        focusManager.clearFocus()  // 关闭键盘
        original(type)
    }

    Column {
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                // 同步至衣橱开关
                ItemOptionMenu(
                    title = "同步至衣橱",
                    showSwitch = true,          // 显示开关
                    showRightArrow = false,     // 不显示右侧箭头
                    showDivider = showSyncCloset,  // 开关开启时显示分隔线
                    checked = showSyncCloset,      // 开关状态
                    removeIndication = true,       // 移除点击效果
                    modifier = Modifier
                        .height(63.dp)          // 固定高度
                        .padding(horizontal = 20.dp),  // 水平内边距
                    onCheckedChange = {
                        focusManager.clearFocus()  // 切换开关时关闭键盘
                        onCheckedChange(it)
                    },
                )

                // 衣橱同步选项（仅在开关开启时显示）
                if (showSyncCloset) {
                    // 归属人选项
                    ItemOptionMenu(
                        title = "归属", showText = true,       // 显示右侧文本
                        rightText = owner,     // 右侧显示的文本
                        showDivider = true,    // 显示分隔线
                        modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.OWNER, onClick) })

                    // 分类选择器
                    SelectClosetCategoryWidget(
                        firstType = closetCategory, secondType = closetSubCategory, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.CATEGORY, onClick) })

                    // 颜色选择
                    ItemOptionMenu(
                        title = "颜色", showColor = true,      // 显示颜色块
                        inputColor = if (color != -1L) Color(color) else null,  // 颜色值转换
                        showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.COLOR, onClick) })

                    // 季节选择
                    ItemOptionMenu(
                        title = "季节", showText = true, rightText = season, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.SEASON, onClick) })

                    // 品牌选择
                    ItemOptionMenu(
                        title = "品牌", showText = true, rightText = product, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.PRODUCT, onClick) })

                    // 尺码选择
                    ItemOptionMenu(
                        title = "尺码", showText = true, rightText = size, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.SIZE, onClick) })

                    // 备注输入框
                    EditCommentsWidget(
                        inputText = bottomComment, modifier = itemPadding, onValueChange = onBottomCommentChange
                    )
                }
            }
        }
    }
}

/**
 * 编辑快速记账-囤货同步屏幕
 * 用于处理与囤货相关的同步设置和数据输入
 *
 * @param sync 是否显示囤货同步选项
 * @param bottomStockComment 底部备注文本
 * @param modifier 修饰符
 * @param stockProduct 囤货品牌
 * @param name 名称
 * @param goodsRack 货架
 * @param stockCategory 囤货类别
 * @param period 使用时段
 * @param onCheckedChange 同步开关状态变化回调
 * @param onBottomCommentChange 底部备注文本变化回调
 * @param onNameChange 名称变化回调
 * @param onClick 选项点击回调（带弹窗类型参数）
 */
@Composable fun EditQuickStockScreen(
    sync: Boolean,
    bottomStockComment: String,
    modifier: Modifier = Modifier,
    stockProduct: String = "",
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
        20.dp, 20.dp, 20.dp, 20.dp  // 统一的选项内边距
    )

    // 包装原始点击事件，点击前先关闭键盘
    val wrapClick: (ShowBottomSheetType, (ShowBottomSheetType) -> Unit) -> Unit = { type, original ->
        focusManager.clearFocus()  // 关闭键盘
        original(type)
    }

    Column {
        GradientRoundedBoxWithStroke(modifier = modifier) {
            Column {
                // 同步至囤货开关
                ItemOptionMenu(
                    title = "同步至囤货",
                    showSwitch = true,          // 显示开关
                    showRightArrow = false,     // 不显示右侧箭头
                    showDivider = sync,         // 开关开启时显示分隔线
                    removeIndication = true,    // 移除点击效果
                    checked = sync,             // 开关状态
                    modifier = Modifier
                        .height(63.dp)          // 固定高度
                        .padding(horizontal = 20.dp),  // 水平内边距
                    onCheckedChange = {
                        onCheckedChange(it)
                    },
                )

                // 囤货同步选项（仅在开关开启时显示）
                if (sync) {
                    // 名称输入框
                    ItemOptionMenu(
                        title = "名称", showTextField = true,    // 显示文本输入框
                        showRightArrow = false,  // 不显示右侧箭头
                        removeIndication = true, // 移除点击效果
                        inputText = name,        // 输入框文本
                        showDivider = true,      // 显示分隔线
                        modifier = itemPadding, onValueChange = onNameChange
                    )

                    // 品牌选择
                    ItemOptionMenu(
                        title = "品牌", showText = true, rightText = stockProduct, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.STOCK_PRODUCT, onClick) })

                    // 货架选择
                    ItemOptionMenu(
                        title = "货架", showText = true, rightText = goodsRack, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.RACK, onClick) })

                    // 类别选择
                    ItemOptionMenu(
                        title = "类别", showText = true, rightText = stockCategory, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.STOCK_CATEGORY, onClick) })

                    // 使用时段选择
                    ItemOptionMenu(
                        title = "使用时段", showText = true, rightText = period, showDivider = true, modifier = itemPadding, onClick = { wrapClick(ShowBottomSheetType.USAGE_PERIOD, onClick) })

                    // 备注输入框
                    EditCommentsWidget(
                        inputText = bottomStockComment, modifier = itemPadding, onValueChange = onBottomCommentChange
                    )
                }
            }
        }
    }
}

@Composable @Preview(showBackground = true) fun EditClosetScreenPreview() {
    HomeBookTheme {
        EditQuickClosetScreen(
            showSyncCloset = true,
            bottomComment = "",
            onCheckedChange = {},
            onBottomCommentChange = {},
            onClick = {},
        )
    }
}

@Composable @Preview(showBackground = true) fun StockScreenPreview() {
    HomeBookTheme {
        EditQuickStockScreen(sync = true, bottomStockComment = "", onCheckedChange = {}, onBottomCommentChange = {}, onClick = {}, onNameChange = {})
    }
}