package com.fanda.homebook.closet

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomTopAppBar
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.ClosetGridEntity
import com.fanda.homebook.route.RoutePath

/*
*
* 衣橱页面
* */
@Composable fun EditClosetCategoryPage(modifier: Modifier = Modifier, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(ClosetGridEntity("", 0, "")) }

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        CustomTopAppBar(
            title = "自定义分类",
            onBackClick = {
                navController.navigateUp()
            },
            rightIconPainter = painterResource(R.mipmap.icon_add_grady),
            onRightActionClick = {
                showDialog = true
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )


    }) { padding ->
        CategoryListWidget(Modifier.padding(padding), onItemClick = {
            category = it
            showDialog = true
        })
    }

    if (showDialog) {
        EditDialog(title = "添加分类", value = category.name, placeholder = "不能与已有类型名重复", onDismissRequest = {
            showDialog = false
        }, onConfirm = {
            showDialog = false
            Log.d("EditClosetCategoryPage", "点击了确定： $it")
        })
    }

}

@Composable private fun CategoryListWidget(modifier: Modifier = Modifier, onItemClick: (ClosetGridEntity) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier, contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, bottom = 20.dp
        )
    ) {
        items(LocalDataSource.closetGridList) {
            GradientRoundedBoxWithStroke() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = it.name, fontSize = 16.sp, modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = Color.Black)
                    Image(
                        painter = painterResource(id = R.mipmap.icon_right), contentDescription = null, modifier = Modifier.size(4.dp, 8.dp), colorFilter = ColorFilter.tint(Color.Black)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Image(painter = painterResource(id = R.mipmap.icon_edit),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(7.dp)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                onItemClick(it)
                            })

                    Image(painter = painterResource(id = R.mipmap.icon_drag),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 7.dp, top = 7.dp, end = 22.dp, bottom = 7.dp)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                Log.d("EditClosetCategoryPage", "点击了拖动： $it")
                            })

                }
            }
        }
    }
}


@Composable @Preview(showBackground = true) fun EditClosetCategoryPagePreview() {
    EditClosetCategoryPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}