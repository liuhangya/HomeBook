package com.fanda.homebook.data

import androidx.compose.ui.graphics.Color
import com.fanda.homebook.R
import com.fanda.homebook.entity.ExpenseCategory
import com.fanda.homebook.quick.sheet.Category
import com.fanda.homebook.quick.sheet.ColorType
import com.fanda.homebook.quick.sheet.SubCategory

object LocalDataSource {
    val expenseCategoryData = listOf(
        ExpenseCategory("保险", R.mipmap.icon_insurance),
        ExpenseCategory("餐饮", R.mipmap.icon_dining),
        ExpenseCategory("发红包", R.mipmap.icon_red_envelope),
        ExpenseCategory("服饰", R.mipmap.icon_clothing),
        ExpenseCategory("服务", R.mipmap.icon_services),
        ExpenseCategory("购物", R.mipmap.icon_shopping),
        ExpenseCategory("护肤", R.mipmap.icon_skincare),
        ExpenseCategory("交通", R.mipmap.icon_traffic),
        ExpenseCategory("旅行", R.mipmap.icon_travel),
        ExpenseCategory("其他", R.mipmap.icon_others),
        ExpenseCategory("人情", R.mipmap.icon_social),
        ExpenseCategory("生活", R.mipmap.icon_daily),
        ExpenseCategory("医疗", R.mipmap.icon_health),
        ExpenseCategory("娱乐", R.mipmap.icon_play),
    )

    val payWayData = listOf(
        "微信",
        "支付宝",
        "现金",
        "淘宝",
        "京东",
        "唯品会",
        "阿里",
        "小红书",
        "拼多多",
        "云闪付",
        "银行卡",
        "信用卡",
        "医宝",
    )

    val productData = listOf(
        "安踏",
        "阿里",
        "竿竿",
        "耐克",
        "山姆",
        "其他",
    )

    val seasonData = listOf(
        "春季",
        "夏季",
        "秋季",
        "冬季",
    )

    val sizeData = listOf(
        "XS",
        "S",
        "M",
        "L",
        "XL",
        "XXL",
        "XXXL",
        "34",
        "35",
        "36",
        "37",
        "38",
        "39",
        "40",
        "41",
        "42",
        "43",
        "44",
        "45",
        "小码",
        "中码",
        "大码",
        "超大",
    )

    val colorData = listOf(
        // 颜色值要加上透明度值
        ColorType("棕色系", 0xFF83878C),
        ColorType("黑色系", 0xFFFF1234),
        ColorType("蓝色系", 0xFFFF2222),
        ColorType("绿色系", 0xFFFF3333),
        ColorType("紫色系", 0xFFFF4444),
        ColorType("红色系", 0xFFFF5555),
        ColorType("灰色系", 0xFFFF6666),
        ColorType("玫红系", 0xFFFF6666),
        ColorType("橙色系", 0xFFFF6666),
        ColorType("金色系", 0xFFFF6666),
        ColorType("裸色系", 0xFFFF6666),
        ColorType("黄色系", 0xFFFF6666),
        ColorType("白色系", 0xFFFF6666),
    )

    val closetCategoryData = listOf(
        Category("1", "上装", listOf(
            SubCategory("1-1", "打底"),
            SubCategory("1-2", "毛衣"),
            SubCategory("1-3", "T恤"),
            SubCategory("1-4", "卫衣"),
            SubCategory("1-5", "外套"),
            SubCategory("1-6", "开衫"),
            SubCategory("1-7", "大衣"),
            SubCategory("1-8", "羽绒服")
        )),
        Category("2", "下装", listOf(
            SubCategory("2-1", "休闲裤"),
            SubCategory("2-2", "牛仔裤"),
            SubCategory("2-3", "运动裤"),
            SubCategory("2-4", "打底裤"),
            SubCategory("2-5", "半身裙"),
            SubCategory("2-6", "短裤"),
            SubCategory("2-7", "短裙"),
            SubCategory("2-8", "西装裤"),
        )),
        Category("3", "鞋靴", listOf(
            SubCategory("3-1", "皮鞋"),
            SubCategory("3-2", "帆布鞋"),
            SubCategory("3-3", "单鞋"),
            SubCategory("3-4", "短靴"),
            SubCategory("3-5", "长筒靴"),
        )),
        Category("4", "包包", listOf(
            SubCategory("4-1", "钱包"),
            SubCategory("4-2", "斜挎包"),
            SubCategory("4-3", "背包"),
            SubCategory("4-4", "妈妈包"),
            SubCategory("4-5", "手提包"),
            SubCategory("4-6", "箱包")
        )),
        Category("5", "配饰", listOf(
            SubCategory("5-1", "帽子"),
            SubCategory("5-2", "围巾"),
            SubCategory("5-3", "项链"),
            SubCategory("5-4", "眼镜"),
            SubCategory("5-5", "手表")
        ))
    )
}

