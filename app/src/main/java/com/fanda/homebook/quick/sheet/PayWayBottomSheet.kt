package com.fanda.homebook.quick.sheet

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.data.LocalDataSource

@Composable fun PayWayBottomSheet(payWay: String, showBottomSheet: Boolean, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    CustomBottomSheet(visible = showBottomSheet, onDismiss = onDismiss) {
        var selected by remember { mutableStateOf(payWay) }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center), style = TextStyle.Default, text = "付款方式", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
                )
                TextButton(modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp), onClick = {
                    onConfirm(selected)
                    onDismiss()
                }) {
                    Text(
                        style = TextStyle.Default, text = "确定", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 9.dp),
                flingBehavior = ScrollableDefaults.flingBehavior(), // 使用默认（无弹性）
            ) {
                items(LocalDataSource.payWayData, key = { it }) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier     // 要注意顺序，先点击事件，后加padding
                            .clickable(onClick = {
                                selected = it
                                Log.d("QuickHomePage", "选择支付方式：${it}")
                            })
                            .padding(vertical = 15.dp, horizontal = 24.dp)
                    ) {
                        Text(
                            text = it, style = TextStyle.Default.copy(platformStyle = PlatformTextStyle(includeFontPadding = false)), fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (selected == it) {
                            Image(
                                painter = painterResource(R.mipmap.icon_selected), contentDescription = null, modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}