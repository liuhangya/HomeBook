package com.fanda.homebook.quick.sheet

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.data.LocalDataSource

data class ColorType(val name: String, val color: Long)

@Composable
fun ColorTypeBottomSheet(
    color: ColorType,
    visible: () -> Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ColorType) -> Unit
) {
    CustomBottomSheet(visible = visible(), onDismiss = onDismiss) {
        var selected by remember { mutableStateOf(color) }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SheetTitleWidget(title = "颜色") {
                onConfirm(selected)
                onDismiss()
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 24.dp),
            ) {
                items(LocalDataSource.colorData, key = { it.name }) {

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier     // 要注意顺序，先点击事件，后加padding
                            .clickable(
                                onClick = {
                                    selected = it
                                },  // 去掉默认的点击效果
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            )
                            .padding(8.dp)
                    ) {
                        ColoredCircleWithBorder(
                            color = Color(it.color),
                            size = 24.dp,
                            borderColor = if (selected == it) Color.Black else colorResource(R.color.color_EAF0F7),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it.name, style = TextStyle.Default, fontSize = 10.sp
                        )
                    }


                }
            }
        }
    }
}