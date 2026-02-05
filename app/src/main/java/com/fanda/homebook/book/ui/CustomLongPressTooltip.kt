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
import com.fanda.homebook.entity.TransactionAmountType
import com.fanda.homebook.quick.ui.getCategoryIcon
import com.fanda.homebook.tools.DATE_FORMAT_MD_HM
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.tools.roundToString

@OptIn(ExperimentalFoundationApi::class) @Composable fun CustomLongPressTooltip(
    modifier: Modifier = Modifier, item: AddQuickEntity, // 替换为你的真实类型
    enableClick: Boolean = false, onItemClick: (AddQuickEntity) -> Unit, onDelete: (AddQuickEntity) -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }
    var longPressPosition by remember { mutableStateOf<Offset?>(null) }

    // 跟踪按压状态
    var isPressed by remember { mutableStateOf(false) }

    // 交互状态
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            // 👇 添加涟漪指示器
        .indication(interactionSource, rememberRipple(bounded = true))
            .then(
                if (enableClick) {
                // 👇 关键：只用 pointerInput，自己处理点击/长按 + 涟漪
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            // 开始按压时触发涟漪
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            isPressed = true

                            try {
                                // 等待释放
                                tryAwaitRelease()
                            } finally {
                                // 释放时结束涟漪
                                val release = PressInteraction.Release(press)
                                interactionSource.emit(release)
                                isPressed = false
                            }
                        },
                        // 点击（短按）
                        onTap = {
                            if (!showTooltip) {

                                onItemClick(item)
                            }
                        },
                        // 长按（带坐标）
                        onLongPress = { position ->
                            longPressPosition = position
                            showTooltip = true
                        })
                }
            } else Modifier)

    ) {
        DailyItemContentWithoutClick(item = item)

        // Tooltip 弹出层
        if (showTooltip && longPressPosition != null) {
            val density = LocalDensity.current
            val touchX = longPressPosition!!.x
            val touchY = longPressPosition!!.y

            val (popupWidthPx, popupHeightPx, spacingPx) = with(density) {
                Triple(120.dp.toPx(), 48.dp.toPx(), 16.dp.toPx())
            }

            val offsetX = (touchX - popupWidthPx / 2).toInt()
            val offsetY = (touchY - popupHeightPx - spacingPx).toInt()

            Popup(
                onDismissRequest = { showTooltip = false }, alignment = Alignment.TopStart, offset = IntOffset(offsetX, offsetY), properties = PopupProperties(
                    focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = false, // 不使用平台默认宽度
                )
            ) {
                // 透明的背景容器
                Box(
                    modifier = Modifier
                        .background(Color.Transparent) // 完全透明背景
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showTooltip = false } // 点击透明区域关闭
                ) {
                    // ElevatedButton 在屏幕中央
                    ElevatedButton(
                        onClick = {
                            onDelete(item)
                            showTooltip = false
                        }, colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.White, contentColor = Color.Red
                        ), elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 8.dp, pressedElevation = 12.dp
                        ), modifier = Modifier
                            .width(120.dp)
                            .align(Alignment.Center) // 居中
                    ) {
                        Text(text = "删除", fontSize = 14.sp, color = Color.Red)
                    }
                }
            }
        }
    }
}

// 修改 DailyItemContent，移除点击事件
@Composable fun DailyItemContentWithoutClick(
    item: AddQuickEntity
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GradientRoundedBoxWithStroke(
            colors = listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.2f)), modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp)
                    .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(id = getCategoryIcon(item.subCategory?.type ?: 0)), contentDescription = null, modifier = Modifier.scale(0.8f)
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.subCategory?.name ?: "", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 0.dp)
                    ) {
                        Text(
                            text = item.payWay?.name ?: "", fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colorResource(id = R.color.color_84878C)
                        )

                        if (item.quick.quickComment.isNotEmpty()) {
                            VerticalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .height(6.dp), color = colorResource(id = R.color.color_B2C6D9)
                            )
                        }

                        Text(
                            text = item.quick.quickComment, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colorResource(id = R.color.color_84878C)
                        )

                        if (!item.payWay?.name.isNullOrEmpty() || item.quick.quickComment.isNotEmpty()) {
                            VerticalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .height(6.dp), color = colorResource(id = R.color.color_B2C6D9)
                            )
                        }

                        Text(
                            text = convertMillisToDate(item.quick.date, DATE_FORMAT_MD_HM), fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colorResource(id = R.color.color_84878C)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                val amount = when (item.category?.type) {
                    TransactionAmountType.INCOME.ordinal -> {
                        "+${item.quick.price.toFloat().roundToString()}"
                    }

                    TransactionAmountType.EXPENSE.ordinal -> {
                        "-${item.quick.price.toFloat().roundToString()}"
                    }

                    else -> {
                        item.quick.price.toFloat().roundToString()
                    }
                }

                val color = when (item.category?.type) {
                    TransactionAmountType.INCOME.ordinal -> {
                        colorResource(id = R.color.color_106CF0)
                    }

                    TransactionAmountType.EXPENSE.ordinal -> {
                        colorResource(id = R.color.color_FF2822)
                    }

                    else -> {
                        colorResource(id = R.color.color_84878C)
                    }
                }

                Text(
                    text = amount, fontSize = 18.sp, modifier = Modifier.padding(end = 16.dp), color = color
                )
            }
        }
    }
}
