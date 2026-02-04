package com.fanda.homebook.book.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.book.entity.CategoryData
import com.fanda.homebook.book.entity.MonthData
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.tools.roundToString

@Composable fun MonthItemWidget(modifier: Modifier = Modifier, item: MonthData, onMonthClick: (MonthData) -> Unit, onItemClick: (CategoryData) -> Unit) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(top = 22.dp, bottom = 8.dp)
                .clickable(       // 去掉默认的点击效果
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    onMonthClick(item)
                }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.monthDisplay, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "出",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.Black,
                modifier = Modifier
                    .background(color = Color.White.copy(0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 0.dp)
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = item.totalExpense.toFloat().roundToString(),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = colorResource(id = R.color.color_FF2822)
            )
            Text(
                text = "入",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .background(color = Color.White.copy(0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 0.dp)
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = item.totalIncome.toFloat().roundToString(),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = colorResource(id = R.color.color_106CF0)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item.categories.forEach {
                MonthAmountItemWidget(item = it, onItemClick = onItemClick)
            }
        }

    }
}

