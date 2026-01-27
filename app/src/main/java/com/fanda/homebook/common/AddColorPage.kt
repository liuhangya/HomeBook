package com.fanda.homebook.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.HSVColorPicker
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.common.viewmodel.ColorTypeViewModel
import com.fanda.homebook.tools.LogUtils

@Composable
fun AddColorPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    colorTypeViewModel: ColorTypeViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    val uiState by colorTypeViewModel.uiState.collectAsState()

    // 获取焦点管理器
    val focusManager = LocalFocusManager.current

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = if (uiState.entity != null) "编辑颜色" else "添加颜色",
            onBackClick = {
                navController.navigateUp()
            },
            rightText = "完成",
            onRightActionClick = {
                if (uiState.entity != null) {
                    colorTypeViewModel.updateEntityDatabase { success ->
                        if (success) {
                            navController.navigateUp()
                        }
                    }
                } else {
                    colorTypeViewModel.insertWithAutoOrder { success ->
                        if (success) {
                            navController.navigateUp()
                        }
                    }
                }
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )


    }) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .then(Modifier.padding(padding))
                .pointerInput(Unit) {// 给最外层添加事件，用于取消输入框的焦点，从而关闭输入法
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() },
                        onDoubleTap = { focusManager.clearFocus() },
                        onLongPress = { focusManager.clearFocus() })
                }
                .background(Color.Transparent) // 必须有背景或 clickable 才能响应事件
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EditColorNameWidget(
                    name = if (uiState.entity != null) uiState.entity!!.name else uiState.addEntity.name,
                    color = if (uiState.entity != null) Color(uiState.entity!!.color) else Color(
                        uiState.addEntity.color
                    )
                ) {
                    if (uiState.entity != null) {
                        colorTypeViewModel.updateEntity(it)
                    } else {
                        colorTypeViewModel.updateAddEntity(it)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                HSVColorPicker(
                    initialColor = if (uiState.entity != null) Color(uiState.entity!!.color) else Color(
                        uiState.addEntity.color
                    ),
                    onColorSelected = {
                        LogUtils.d("HSVColorPicker", "onColorSelected: $it")
                        if (uiState.entity != null) {
                            colorTypeViewModel.updateEntity(it.toArgb().toLong())
                        } else {
                            colorTypeViewModel.updateAddEntity(it.toArgb().toLong())
                        }
                    })
            }
        }

    }

}

@Composable
fun EditColorNameWidget(
    modifier: Modifier = Modifier,
    color: Color,
    name: String,
    onNameChange: (String) -> Unit
) {
    GradientRoundedBoxWithStroke(modifier = Modifier.height(64.dp)) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically
        ) {
            ColoredCircleWithBorder(
                color = color,
                modifier = Modifier.padding(start = 16.dp, end = 0.dp)
            )
            TextField(
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                placeholder = {
                    Text(
                        text = "请输入名称",
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.color_84878C)
                    )
                },
                value = name,
                onValueChange = { newText ->
                    onNameChange(newText)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                textStyle = TextStyle.Default.copy(
                    fontSize = 16.sp,
                    color = Color.Black,
                )
            )
        }
    }
}


@Composable
@Preview(showBackground = true)
private fun AddColorPagePreview() {
    AddColorPage(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .statusBarsPadding(), navController = rememberNavController()
    )
}