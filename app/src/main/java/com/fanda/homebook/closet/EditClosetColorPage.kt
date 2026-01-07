package com.fanda.homebook.closet

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.closet.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.closet.sheet.RenameOrDeleteType
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.quick.sheet.ColorType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils

@Composable fun EditClosetColorPage(modifier: Modifier = Modifier, navController: NavController) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var colorType by remember { mutableStateOf(ColorType("", -1L)) }
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "颜色管理",
            onBackClick = {
                navController.navigateUp()
            },
            rightIconPainter = painterResource(R.mipmap.icon_add_grady),
            onRightActionClick = {
                navController.navigate(RoutePath.ClosetAddColor.route)
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )


    }) { padding ->
        ClosetDragWidget(Modifier.padding(padding), onItemClick = {
            colorType = it
            showBottomSheet = true
        })
    }

    if (showEditDialog) {
        EditDialog(title = "重命名", value = colorType.name, placeholder = "不能与已有名称重复", onDismissRequest = {
            showEditDialog = false
        }, onConfirm = {
            showEditDialog = false
        })
    }
    RenameOrDeleteBottomSheet(visible = showBottomSheet, onDismiss = {
            showBottomSheet = false
        }) {
            showBottomSheet = false
            if (it == RenameOrDeleteType.RENAME){
                showEditDialog = true
            }else {
                LogUtils.d("点击了删除按钮:$it")
            }
        }
}

@Composable private fun ClosetDragWidget(modifier: Modifier = Modifier, onItemClick: (ColorType) -> Unit) {
    DragLazyColumn(modifier = modifier, items = LocalDataSource.colorData, onMove = { from, to ->
        Log.d("EditClosetCategoryPage", "from: $from, to: $to")
    }) { item, isDragging ->
        GradientRoundedBoxWithStroke {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDragging) colorResource(id = R.color.color_F5F5F5) else Color.Transparent)
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColoredCircleWithBorder(color = Color(item.color),modifier = Modifier.padding(start = 16.dp, end = 12.dp))
                Text(text = item.name, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.weight(1f))
                Image(painter = painterResource(id = R.mipmap.icon_edit),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(7.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            onItemClick(item)
                        })

                Image(
                    painter = painterResource(id = R.mipmap.icon_drag), contentDescription = null, modifier = Modifier.padding(start = 7.dp, top = 7.dp, end = 22.dp, bottom = 7.dp)
                )

            }
        }
    }
}

@Composable @Preview(showBackground = true) fun EditClosetColorPagePreview() {
    EditClosetColorPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}