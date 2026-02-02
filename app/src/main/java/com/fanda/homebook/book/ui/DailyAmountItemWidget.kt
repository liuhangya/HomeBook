package com.fanda.homebook.book.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.entity.DailyItemEntity
import com.fanda.homebook.entity.TransactionAmountType

@Composable fun DailyAmountItemWidget(modifier: Modifier = Modifier, item: AddQuickEntity) {
    Box(modifier = modifier) {
        GradientRoundedBoxWithStroke(
            colors = listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.2f)), modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(start = 15.dp)
                .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {

                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .size(32.dp)
                        .clip(
                            CircleShape
                        )
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.icon_shopping), contentDescription = null, modifier = Modifier.scale(0.8f)

                    )

                }
                Column(
                    modifier = Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.subCategory?.name ?: "", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 0.dp)) {
                        Text(
                            text = item.payWay?.name ?: "", fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colorResource(id = R.color.color_84878C)
                        )
                        VerticalDivider(modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .height(6.dp), color = colorResource(id = R.color.color_B2C6D9))
                        Text(
                            text = item.quick.quickComment, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = colorResource(id = R.color.color_84878C)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                val amount = when (item.category?.type) {
                    TransactionAmountType.INCOME.ordinal -> {
                        "+${item.quick.price}"
                    }
                    TransactionAmountType.EXPENSE.ordinal -> {
                        "-${item.quick.price}"
                    }
                    else -> {
                        "${item.quick.price}"
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
                Text(text = amount, fontSize = 18.sp, modifier = Modifier.padding(end = 16.dp), color = color)
            }
        }
    }
}

@Composable @Preview(showBackground = true) fun DailyAmountItemWidgetPreview() {
//    DailyAmountItemWidget(modifier = Modifier, DailyItemEntity(1, TransactionAmountType.EXPENSE, 100.0f, "购物", "微信", "无"))
}