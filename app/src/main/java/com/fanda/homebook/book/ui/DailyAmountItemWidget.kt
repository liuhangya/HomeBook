package com.fanda.homebook.book.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.fanda.homebook.R
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.common.entity.TransactionAmountType
import com.fanda.homebook.quick.ui.getCategoryIcon
import com.fanda.homebook.tools.DATE_FORMAT_MD_HM
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.tools.roundToString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DailyAmountItemWidget(
    modifier: Modifier = Modifier,
    item: AddQuickEntity,              // 交易数据实体
    enableClick: Boolean = false,      // 是否启用点击功能
    onItemClick: (AddQuickEntity) -> Unit,  // 点击回调
    onDelete: (AddQuickEntity) -> Unit      // 删除回调
) {
    // 控制工具提示弹出窗口的显示状态
    var showTooltip by remember { mutableStateOf(false) }
    // 记录长按位置，用于定位弹出窗口
    var longPressPosition by remember { mutableStateOf<Offset?>(null) }

    // 跟踪按压状态，用于涟漪效果
    var isPressed by remember { mutableStateOf(false) }

    // 交互状态管理
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(64.dp)                    // 固定高度64dp
            .clip(RoundedCornerShape(16.dp))  // 圆角裁剪
            // 👇 添加涟漪指示器（Material Design效果）
            .indication(interactionSource, rememberRipple(bounded = true))
            .then(
                if (enableClick) {
                    // 👇 关键：使用pointerInput手动处理点击和长按手势，同时保持涟漪效果
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                // 开始按压时触发涟漪效果
                                val press = PressInteraction.Press(offset)
                                interactionSource.emit(press)
                                isPressed = true

                                try {
                                    // 等待手势释放
                                    tryAwaitRelease()
                                } finally {
                                    // 释放时结束涟漪效果
                                    val release = PressInteraction.Release(press)
                                    interactionSource.emit(release)
                                    isPressed = false
                                }
                            },
                            // 点击（短按）处理
                            onTap = {
                                // 只有当前没有显示工具提示时才响应点击
                                if (!showTooltip) {
                                    onItemClick(item)
                                }
                            },
                            // 长按处理
                            onLongPress = { position ->
                                // 记录长按位置并显示工具提示
                                longPressPosition = position
                                showTooltip = true
                            }
                        )
                    }
                } else Modifier // 如果未启用点击功能，不添加手势处理
            )
    ) {
        // 显示交易内容（无点击功能）
        DailyItemContentWithoutClick(item = item)

        // 工具提示弹出层
        if (showTooltip && longPressPosition != null) {
            // 获取屏幕密度，用于dp和px转换
            val density = LocalDensity.current
            val touchX = longPressPosition!!.x
            val touchY = longPressPosition!!.y

            // 计算弹出窗口的尺寸
            val (popupWidthPx, popupHeightPx, spacingPx) = with(density) {
                Triple(120.dp.toPx(), 48.dp.toPx(), 16.dp.toPx())
            }

            // 计算弹出窗口的偏移位置（使其在长按位置上方显示）
            val offsetX = (touchX - popupWidthPx / 2).toInt()
            val offsetY = (touchY - popupHeightPx - spacingPx).toInt()

            Popup(
                onDismissRequest = { showTooltip = false },  // 关闭回调
                alignment = Alignment.TopStart,              // 从左上角对齐
                offset = IntOffset(offsetX, offsetY),        // 偏移位置
                properties = PopupProperties(
                    focusable = true,                        // 可获取焦点
                    dismissOnBackPress = true,               // 按返回键关闭
                    dismissOnClickOutside = true,            // 点击外部关闭
                    usePlatformDefaultWidth = false,         // 不使用平台默认宽度
                )
            ) {
                // 透明背景容器，用于捕获点击事件并关闭工具提示
                Box(
                    modifier = Modifier
                        .background(Color.Transparent)  // 完全透明背景
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showTooltip = false  // 点击透明区域关闭工具提示
                        }
                ) {
                    // 删除按钮
                    ElevatedButton(
                        onClick = {
                            onDelete(item)       // 执行删除操作
                            showTooltip = false  // 关闭工具提示
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.White,  // 白色背景
                            contentColor = Color.Red       // 红色文字
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 8.dp,       // 默认高度
                            pressedElevation = 12.dp       // 按下时高度
                        ),
                        modifier = Modifier
                            .width(120.dp)                 // 固定宽度
                            .align(Alignment.Center)       // 居中显示
                    ) {
                        Text(
                            text = "删除",
                            fontSize = 14.sp,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}

/**
 * 交易项内容组件（无点击功能）
 * 显示单笔交易的详细信息，包括分类图标、名称、支付方式、备注、时间和金额
 *
 * @param item 交易数据实体
 */
@Composable
fun DailyItemContentWithoutClick(
    item: AddQuickEntity
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 渐变圆角边框容器
        GradientRoundedBoxWithStroke(
            colors = listOf(
                Color.White.copy(alpha = 0.4f),  // 渐变起始颜色
                Color.White.copy(alpha = 0.2f)   // 渐变结束颜色
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            // 水平布局，包含图标和信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp)      // 左侧内边距
                    .fillMaxHeight(),           // 占满高度
                verticalAlignment = Alignment.CenterVertically  // 垂直居中对齐
            ) {
                // 分类图标容器
                Box(
                    contentAlignment = Alignment.Center,  // 内容居中
                    modifier = Modifier
                        .size(32.dp)                     // 固定尺寸
                        .clip(CircleShape)                // 圆形裁剪
                        .background(Color.White)          // 白色背景
                ) {
                    // 分类图标
                    Image(
                        painter = painterResource(id = getCategoryIcon(item.subCategory?.type ?: 0)),
                        contentDescription = null,       // 无障碍描述
                        modifier = Modifier.scale(0.8f)  // 缩放80%
                    )
                }

                // 信息列
                Column(
                    modifier = Modifier.padding(start = 12.dp),  // 左侧间距
                    verticalArrangement = Arrangement.Center     // 垂直居中
                ) {
                    // 分类名称
                    Text(
                        text = item.subCategory?.name ?: "",  // 子分类名称，为空时显示空字符串
                        fontWeight = FontWeight.Medium,       // 中等字重
                        fontSize = 14.sp,                     // 字号14sp
                        color = Color.Black                   // 黑色字体
                    )

                    // 详细信息行
                    Row(
                        verticalAlignment = Alignment.CenterVertically,  // 垂直居中
                        modifier = Modifier.padding(top = 0.dp)         // 顶部间距
                    ) {
                        // 支付方式
                        Text(
                            text = item.payWay?.name ?: "",                 // 支付方式名称
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = colorResource(id = R.color.color_84878C)  // 灰色字体
                        )

                        // 分隔线（当有支付方式且有备注时显示）
                        if (item.quick.quickComment.isNotEmpty()) {
                            VerticalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)  // 左右间距
                                    .clip(RoundedCornerShape(2.dp))  // 圆角分隔线
                                    .height(6.dp),                // 高度6dp
                                color = colorResource(id = R.color.color_B2C6D9)  // 分隔线颜色
                            )
                        }

                        // 备注信息
                        Text(
                            text = item.quick.quickComment,               // 交易备注
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = colorResource(id = R.color.color_84878C)
                        )

                        // 分隔线（当有支付方式或备注，并且有时间时显示）
                        if (!item.payWay?.name.isNullOrEmpty() || item.quick.quickComment.isNotEmpty()) {
                            VerticalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .height(6.dp),
                                color = colorResource(id = R.color.color_B2C6D9)
                            )
                        }

                        // 交易时间
                        Text(
                            text = convertMillisToDate(item.quick.date, DATE_FORMAT_MD_HM),  // 格式化时间
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = colorResource(id = R.color.color_84878C)
                        )
                    }
                }

                // 弹性空白，分隔左侧内容和右侧金额
                Spacer(modifier = Modifier.weight(1f))

                // 金额显示逻辑
                val amount = when (item.category?.type) {
                    TransactionAmountType.INCOME.ordinal -> {
                        // 收入：显示正号和金额
                        "+${item.quick.price.toFloat().roundToString()}"
                    }
                    TransactionAmountType.EXPENSE.ordinal -> {
                        // 支出：显示负号和金额
                        "-${item.quick.price.toFloat().roundToString()}"
                    }
                    else -> {
                        // 其他类型：直接显示金额
                        item.quick.price.toFloat().roundToString()
                    }
                }

                // 金额颜色逻辑
                val color = when (item.category?.type) {
                    TransactionAmountType.INCOME.ordinal -> {
                        // 收入：蓝色
                        colorResource(id = R.color.color_106CF0)
                    }
                    TransactionAmountType.EXPENSE.ordinal -> {
                        // 支出：红色
                        colorResource(id = R.color.color_FF2822)
                    }
                    else -> {
                        // 其他类型：灰色
                        colorResource(id = R.color.color_84878C)
                    }
                }

                // 金额文本
                Text(
                    text = amount,                       // 格式化后的金额
                    fontSize = 18.sp,                    // 大字号18sp
                    modifier = Modifier.padding(end = 16.dp),  // 右侧间距
                    color = color                        // 根据类型设置的颜色
                )
            }
        }
    }
}