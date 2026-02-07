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
import com.fanda.homebook.common.viewmodel.ColorTypeViewModel
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.HSVColorPicker
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.tools.LogUtils

/**
 * 添加/编辑颜色页面
 * 用于创建新颜色或编辑现有颜色
 * @param modifier 修饰符
 * @param navController 导航控制器，用于页面跳转
 * @param colorTypeViewModel 颜色类型ViewModel，由Compose自动注入
 */
@Composable fun AddColorPage(
    modifier: Modifier = Modifier, navController: NavController, colorTypeViewModel: ColorTypeViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    // 收集ViewModel中的UI状态
    val uiState by colorTypeViewModel.uiState.collectAsState()

    // 获取焦点管理器，用于管理输入法显示/隐藏
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopIconAppBar(
                title = if (uiState.entity != null) "编辑颜色" else "添加颜色", // 根据是否在编辑模式显示不同标题
                onBackClick = {
                    navController.navigateUp() // 点击返回按钮返回上一页
                },
                rightText = "完成",
                onRightActionClick = {
                    if (uiState.entity != null) {
                        // 编辑模式：更新现有颜色
                        colorTypeViewModel.updateEntityDatabase { success ->
                            if (success) {
                                navController.navigateUp() // 更新成功后返回
                            }
                        }
                    } else {
                        // 添加模式：插入新颜色
                        colorTypeViewModel.insertWithAutoOrder { success ->
                            if (success) {
                                navController.navigateUp() // 添加成功后返回
                            }
                        }
                    }
                },
                backIconPainter = painterResource(R.mipmap.icon_back),
            )
        }) { padding ->
        Box(modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .then(Modifier.padding(padding))
            .pointerInput(Unit) {
                // 给最外层添加触摸事件，用于取消输入框的焦点，从而关闭输入法
                detectTapGestures(onTap = { focusManager.clearFocus() }, onDoubleTap = { focusManager.clearFocus() }, onLongPress = { focusManager.clearFocus() })
            }
            .background(Color.Transparent) // 必须有背景或clickable才能响应事件
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 颜色名称编辑组件
                EditColorNameWidget(
                    name = if (uiState.entity != null) uiState.entity!!.name else uiState.addEntity.name, color = if (uiState.entity != null) Color(uiState.entity!!.color)
                    else Color(uiState.addEntity.color)
                ) { newName ->
                    // 根据模式调用不同的更新函数
                    if (uiState.entity != null) {
                        colorTypeViewModel.updateEntity(newName)
                    } else {
                        colorTypeViewModel.updateAddEntity(newName)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // HSV颜色选择器组件
                HSVColorPicker(
                    initialColor = if (uiState.entity != null) Color(uiState.entity!!.color)
                    else Color(uiState.addEntity.color), onColorSelected = { selectedColor ->
                        LogUtils.d("HSVColorPicker", "onColorSelected: $selectedColor")
                        // 将Color转换为Long类型存储
                        val colorLong = selectedColor.toArgb().toLong()
                        // 根据模式调用不同的更新函数
                        if (uiState.entity != null) {
                            colorTypeViewModel.updateEntity(colorLong)
                        } else {
                            colorTypeViewModel.updateAddEntity(colorLong)
                        }
                    })
            }
        }
    }
}

/**
 * 颜色名称编辑部件
 * 包含颜色圆圈和名称输入框
 * @param modifier 修饰符
 * @param color 当前选择的颜色，用于显示在圆圈中
 * @param name 当前颜色名称
 * @param onNameChange 名称改变时的回调函数
 */
@Composable fun EditColorNameWidget(
    modifier: Modifier = Modifier, color: Color, name: String, onNameChange: (String) -> Unit
) {
    // 使用渐变的圆角背景框
    GradientRoundedBoxWithStroke(modifier = Modifier.height(64.dp)) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(), verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧颜色圆圈
            ColoredCircleWithBorder(
                color = color, modifier = Modifier.padding(start = 16.dp, end = 0.dp)
            )

            // 右侧名称输入框
            TextField(
                colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, // 隐藏选中时的下划线
                unfocusedIndicatorColor = Color.Transparent, // 隐藏未选中时的下划线
            ), placeholder = {
                Text(
                    text = "请输入名称", fontSize = 16.sp, color = colorResource(id = R.color.color_84878C) // 使用资源颜色
                )
            }, value = name, onValueChange = { newText ->
                onNameChange(newText) // 名称变化时回调
            }, singleLine = true, // 单行输入
                modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done // 键盘右下角显示"完成"按钮
                ), textStyle = TextStyle.Default.copy(
                    fontSize = 16.sp,
                    color = Color.Black,
                )
            )
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览UI效果
 */
@Composable @Preview(showBackground = true) private fun AddColorPagePreview() {
    AddColorPage(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .statusBarsPadding(), navController = rememberNavController()
    )
}