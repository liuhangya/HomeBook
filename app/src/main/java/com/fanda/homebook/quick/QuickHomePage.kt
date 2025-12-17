package com.fanda.homebook.quick

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.components.CustomTopAppBar
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.R
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.quick.data.LocalDataSource
import com.fanda.homebook.ui.theme.HomeBookTheme

enum class TransactionType {
    EXPENSE,      // 支出
    INCOME,       // 入账
    EXCLUDED      // 不计入收支
}

// 支出分类
data class ExpenseCategory(val name: String, @DrawableRes val icon: Int)

/*
* 记一笔页面
* */
@Composable
fun QuickHomePage(modifier: Modifier = Modifier, navController: NavController) {
    Scaffold(modifier = modifier, topBar = {
        CustomTopAppBar(title = RoutePath.QUICK_ADD.title, onBackClick = {
            navController.navigateUp()
        }, rightText = "保存", onRightActionClick = {

        }, backIconPainter = painterResource(R.mipmap.icon_back))
    }) { padding ->
        val focusManager = LocalFocusManager.current
        // 创建一个覆盖整个屏幕的可点击区域（放在最外层）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // 检测任意点击（包括拖动开始）
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() },
                        onDoubleTap = { focusManager.clearFocus() },
                        onLongPress = { focusManager.clearFocus() }
                    )
                }
                .background(Color.Transparent) // 必须有背景或 clickable 才能响应点击
        ) {
            // 为了让 padding 内容能滑动，所以用 Column 包起来
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(  verticalArrangement = Arrangement.spacedBy(20.dp),modifier = Modifier.fillMaxWidth() .padding(20.dp)){
                    TopTypeSelector()
                    EditAmountField()

                    SelectCategoryGrid()

                    SelectCategoryGrid()

                    SelectCategoryGrid()
                }
            }
        }
    }
}

@Composable
fun SelectCategoryGrid() {
    GradientRoundedBoxWithStroke(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        var selectedCategory by remember { mutableIntStateOf(0) }
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {

            items(items = LocalDataSource.expenseCategoryData, key = { it.name }) { category ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        // 去掉默认的点击效果
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectedCategory = category.icon
                    }) {
                    // 通过 colorFilter 来改变图标颜色
                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier
                            .size(32.dp)
                            .clip(
                                CircleShape
                            )
                            .background(if (selectedCategory == category.icon) Color.Black else Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = category.icon),
                            contentDescription = null,
                            colorFilter = if (selectedCategory == category.icon) ColorFilter.tint(
                                Color.White
                            ) else null,
                            modifier = Modifier.scale(0.8f)

                        )

                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.name,
                        fontSize = 8.sp,
                        style = TextStyle.Default,
                        color = colorResource(R.color.color_333333),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

            }
        }
    }
}

@Composable
fun EditAmountField() {
    var amount by remember { mutableStateOf("") }
    // 1. 创建 FocusRequester
    val focusRequester = remember { FocusRequester() }
    GradientRoundedBoxWithStroke(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "¥",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp)
            )
            BasicTextField(
                value = amount,
                onValueChange = { newText ->
                    // 🔒 限制只能输入数字和一个小数点
                    if (isValidDecimalInput(newText)) {
                        amount = newText
                    }
                    // 否则忽略非法输入
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                textStyle = TextStyle.Default.copy(
                    fontSize = 32.sp,
                    color = Color.Black,
                )
            )
        }
        // 3. 在首次组合后请求焦点
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun TopTypeSelector(modifier: Modifier = Modifier) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        SelectableRoundedButton(
            text = "支出",
            selected = selectedType == TransactionType.EXPENSE,
            onClick = { selectedType = TransactionType.EXPENSE })
        SelectableRoundedButton(
            text = "入账",
            selected = selectedType == TransactionType.INCOME,
            onClick = { selectedType = TransactionType.INCOME })
        SelectableRoundedButton(
            text = "不计入收支",
            selected = selectedType == TransactionType.EXCLUDED,
            onClick = { selectedType = TransactionType.EXCLUDED })
        Spacer(modifier = Modifier.weight(1f))
        SelectableRoundedButton(
            text = "10月9日",
            selected = false,
            onClick = {},
            imageRes = R.mipmap.icon_down
        )
    }
}

// ✅ 校验函数：只允许 "123", "12.34", ".5", "0.1" 等格式
private fun isValidDecimalInput(text: String): Boolean {
    if (text.isEmpty()) return true

    // 不允许以多个 0 开头（如 "00"、"01"），但允许 "0." 或 "0.1"
    if (text.length > 1 && text[0] == '0' && text[1] != '.' && text[1] != ',') {
        return false
    }

    // 只允许数字和一个小数点
    val dotCount = text.count { it == '.' }
    if (dotCount > 1) return false

    // 检查每个字符是否为数字或小数点
    return text.all { it.isDigit() || it == '.' }
}

@Composable
@Preview(showBackground = true)
fun QuickHomePagePreview() {
    HomeBookTheme {
        QuickHomePage(modifier = Modifier.fillMaxWidth(), navController = rememberNavController())
    }
}