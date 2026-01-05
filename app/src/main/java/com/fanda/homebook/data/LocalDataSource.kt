package com.fanda.homebook.data

import com.fanda.homebook.R
import com.fanda.homebook.entity.BaseCategoryEntity
import com.fanda.homebook.entity.CategoryBottomMenuEntity
import com.fanda.homebook.entity.ClosetCategoryBottomMenuType
import com.fanda.homebook.entity.ClosetGridEntity
import com.fanda.homebook.entity.StateMenuEntity
import com.fanda.homebook.entity.StockGridEntity
import com.fanda.homebook.entity.StockState
import com.fanda.homebook.entity.TransactionCategory
import com.fanda.homebook.entity.TransactionType
import com.fanda.homebook.entity.UserEntity
import com.fanda.homebook.quick.sheet.Category
import com.fanda.homebook.quick.sheet.ColorType
import com.fanda.homebook.quick.sheet.SubCategory

object LocalDataSource {
    val expenseCategoryData = listOf(
        TransactionCategory("餐饮", R.mipmap.icon_dining, TransactionType.EXPENSE),
        TransactionCategory("交通", R.mipmap.icon_traffic, TransactionType.EXPENSE),
        TransactionCategory("服饰", R.mipmap.icon_clothing, TransactionType.EXPENSE),
        TransactionCategory("护肤", R.mipmap.icon_skincare, TransactionType.EXPENSE),
        TransactionCategory("购物", R.mipmap.icon_shopping, TransactionType.EXPENSE),
        TransactionCategory("服务", R.mipmap.icon_services, TransactionType.EXPENSE),
        TransactionCategory("医疗", R.mipmap.icon_health, TransactionType.EXPENSE),
        TransactionCategory("娱乐", R.mipmap.icon_play, TransactionType.EXPENSE),
        TransactionCategory("生活", R.mipmap.icon_daily, TransactionType.EXPENSE),
        TransactionCategory("旅行", R.mipmap.icon_travel, TransactionType.EXPENSE),
        TransactionCategory("保险", R.mipmap.icon_insurance, TransactionType.EXPENSE),
        TransactionCategory("发红包", R.mipmap.icon_red_envelope, TransactionType.EXPENSE),
        TransactionCategory("人情", R.mipmap.icon_social, TransactionType.EXPENSE),
        TransactionCategory("其他", R.mipmap.icon_others, TransactionType.EXPENSE),
        TransactionCategory("其他1", R.mipmap.icon_others, TransactionType.EXPENSE),
        TransactionCategory("其他2", R.mipmap.icon_others, TransactionType.EXPENSE),
        TransactionCategory("其他3", R.mipmap.icon_others, TransactionType.EXPENSE),
    )

    val incomeCategoryData = listOf(
        TransactionCategory("工资", R.mipmap.icon_salary, TransactionType.INCOME),
        TransactionCategory("收红包", R.mipmap.icon_get_money, TransactionType.INCOME),
        TransactionCategory("人情", R.mipmap.icon_social, TransactionType.INCOME),
        TransactionCategory("奖金", R.mipmap.icon_bonus, TransactionType.INCOME),
        TransactionCategory("其他", R.mipmap.icon_others, TransactionType.INCOME),
    )

    val excludeCategoryData = listOf(
        TransactionCategory("理财", R.mipmap.icon_finance, TransactionType.EXCLUDED),
        TransactionCategory("借还款", R.mipmap.icon_debts, TransactionType.EXCLUDED),
        TransactionCategory("其他", R.mipmap.icon_others, TransactionType.EXCLUDED),
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
        "医保",
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

    val ownerData = listOf(
        "番茄",
        "阿凡达",
        "圆圆",
        "家庭",
        "送人",
    )

    val periodData = listOf(
        "日用",
        "夜用",
        "全天",
    )
    val goodsRackData = listOf(
        BaseCategoryEntity(1, "梳妆台"),
        BaseCategoryEntity(2, "米面粮油"),
        BaseCategoryEntity(3, "日用百货"),
        BaseCategoryEntity(4, "圆圆"),
        BaseCategoryEntity(5, "其他囤货"),
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

    val colorData = mutableListOf(
        // 颜色值要加上透明度值
        ColorType("棕色系", 0xFF6A3D06),
        ColorType("黑色系", 0xFF000000),
        ColorType("蓝色系", 0xFF9CD4EB),
        ColorType("绿色系", 0xFFA4D66B),
        ColorType("紫色系", 0xFFB398F1),
        ColorType("红色系", 0xFFDA4851),
        ColorType("灰色系", 0xFFDADADA),
        ColorType("玫红系", 0xFFE360BE),
        ColorType("橙色系", 0xFFEC9F4C),
        ColorType("金色系", 0xFFEFDD8B),
        ColorType("裸色系", 0xFFEFE5CE),
        ColorType("黄色系", 0xFFF8D854),
        ColorType("白色系", 0xFFFFFFFF),
    )

    val closetCategoryData = listOf(
        Category(
            "1", "上装", listOf(
                SubCategory("1-1", "打底"),
                SubCategory("1-2", "毛衣"),
                SubCategory("1-3", "T恤"),
                SubCategory("1-4", "卫衣"),
                SubCategory("1-5", "外套"),
                SubCategory("1-6", "开衫"),
                SubCategory("1-7", "大衣"),
                SubCategory("1-8", "羽绒服")
            )
        ), Category(
            "2", "下装", listOf(
                SubCategory("2-1", "休闲裤"),
                SubCategory("2-2", "牛仔裤"),
                SubCategory("2-3", "运动裤"),
                SubCategory("2-4", "打底裤"),
                SubCategory("2-5", "半身裙"),
                SubCategory("2-6", "短裤"),
                SubCategory("2-7", "短裙"),
                SubCategory("2-8", "西装裤"),
            )
        ), Category(
            "3", "鞋靴", listOf(
                SubCategory("3-1", "皮鞋"),
                SubCategory("3-2", "帆布鞋"),
                SubCategory("3-3", "单鞋"),
                SubCategory("3-4", "短靴"),
                SubCategory("3-5", "长筒靴"),
            )
        ), Category(
            "4", "包包", listOf(
                SubCategory("4-1", "钱包"), SubCategory("4-2", "斜挎包"), SubCategory("4-3", "背包"), SubCategory("4-4", "妈妈包"), SubCategory("4-5", "手提包"), SubCategory("4-6", "箱包")
            )
        ), Category(
            "5", "配饰", listOf(
                SubCategory("5-1", "帽子"), SubCategory("5-2", "围巾"), SubCategory("5-3", "项链"), SubCategory("5-4", "眼镜"), SubCategory("5-5", "手表")
            )
        )
    )

    val stockCategoryData = listOf(
        Category(
            "1", "梳妆台", listOf(
                SubCategory("1-1", "洁面"),
                SubCategory("1-2", "水"),
                SubCategory("1-3", "乳液"),
                SubCategory("1-4", "精华"),
                SubCategory("1-5", "面霜"),
                SubCategory("1-6", "眼霜"),
                SubCategory("1-7", "面膜"),
                SubCategory("1-8", "防晒"),
                SubCategory("1-9", "底妆"),
                SubCategory("1-10", "唇膏"),
                SubCategory("1-11", "眼影"),
                SubCategory("1-12", "润肤"),
            )
        ),
        Category(
            "2", "米面粮油", listOf(
                SubCategory("2-1", "零食"),
                SubCategory("2-2", "主食"),
                SubCategory("2-3", "调料"),
            )
        ),
        Category(
            "3", "日用百货", listOf(
                SubCategory("3-1", "纸巾"),
                SubCategory("3-2", "面巾"),
                SubCategory("3-3", "洗头"),
                SubCategory("3-4", "洗澡"),
                SubCategory("3-5", "洗衣"),
                SubCategory("3-6", "护发"),
                SubCategory("3-7", "片剂"),
            )
        ),
        Category(
            "4", "圆圆", listOf(
                SubCategory("4-1", "护肤"),
                SubCategory("4-2", "补剂"),
            )
        ),
    )

    //  =========================== 衣橱
    val closetGridList = mutableListOf<ClosetGridEntity>().apply {
        repeat(20) {
            add(
                ClosetGridEntity(
                    name = "上装$it", count = 10, photoUrl = ""
                )
            )
        }
    }

    val closetDetailGridList = mutableListOf<ClosetGridEntity>().apply {
        repeat(20) {
            add(
                ClosetGridEntity(
                    name = "上装$it", count = 10, photoUrl = "", isSelected = false
                )
            )
        }
    }

    val closetCategoryBottomMenuList = listOf(
        CategoryBottomMenuEntity("全选", R.mipmap.icon_bottom_all_select, ClosetCategoryBottomMenuType.ALL_SELECTED),
        CategoryBottomMenuEntity("复制", R.mipmap.icon_bottom_copy, ClosetCategoryBottomMenuType.COPY),
        CategoryBottomMenuEntity("移动", R.mipmap.icon_bottom_move, ClosetCategoryBottomMenuType.MOVE),
        CategoryBottomMenuEntity("删除", R.mipmap.icon_bottom_delete, ClosetCategoryBottomMenuType.DELETE)

    )

    // 用户列表
    val userList = mutableListOf(
        UserEntity(1, "番茄"), UserEntity(2, "阿凡达"), UserEntity(3, "圆圆"), UserEntity(4, "家庭"), UserEntity(5, "送人")
    )

    // 囤货状态菜单列表
    val stockStateList = listOf(
        StateMenuEntity(1,"全部", 100),
        StateMenuEntity(2,"使用中", 200),
        StateMenuEntity(3,"未开封", 10,),
        StateMenuEntity(4,"已用完", 20)
    )

    val stockGridList = mutableListOf<StockGridEntity>().apply {
        repeat(3) {
            add(
                StockGridEntity(
                    name = "潘婷3分钟奇迹发膜$it", "洁面", "", 10, "剩余2年以上", "50g", StockState.USING
                )
            )
        }
        repeat(3) {
            add(
                StockGridEntity(
                    name = "潘婷3分钟奇迹发膜$it", "洁面", "", 10, "剩余2年以上", "50g", StockState.NOT_OPEN
                )
            )
        }
        repeat(3) {
            add(
                StockGridEntity(
                    name = "潘婷3分钟奇迹发膜$it", "洁面", "", 10, "剩余2年以上", "50g", StockState.USED_UP
                )
            )
        }
    }
}

