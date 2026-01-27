package com.fanda.homebook.data

import com.fanda.homebook.R
import com.fanda.homebook.data.closet.CategoryBottomMenuEntity
import com.fanda.homebook.data.stock.StockMenuEntity
import com.fanda.homebook.data.stock.StockStatusEntity
import com.fanda.homebook.entity.AmountItemEntity
import com.fanda.homebook.entity.BaseCategoryEntity
import com.fanda.homebook.entity.DailyAmountEntity
import com.fanda.homebook.entity.DailyItemEntity
import com.fanda.homebook.entity.DashBoarItemEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.entity.StateMenuEntity
import com.fanda.homebook.entity.StockGridEntity
import com.fanda.homebook.entity.StockState
import com.fanda.homebook.entity.TransactionCategory
import com.fanda.homebook.entity.TransactionType
import com.fanda.homebook.quick.sheet.Category
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

    val closetCategoryBottomMenuList = listOf(
        CategoryBottomMenuEntity("全选", R.mipmap.icon_bottom_all_select, ShowBottomSheetType.ALL_SELECTED),
        CategoryBottomMenuEntity("复制", R.mipmap.icon_bottom_copy, ShowBottomSheetType.COPY),
        CategoryBottomMenuEntity("移动", R.mipmap.icon_bottom_move, ShowBottomSheetType.MOVE),
        CategoryBottomMenuEntity("删除", R.mipmap.icon_bottom_delete, ShowBottomSheetType.DELETE)

    )

    val stockMenuList = listOf(
        StockMenuEntity("编辑商品", ShowBottomSheetType.EDIT), StockMenuEntity("复制商品", ShowBottomSheetType.COPY), StockMenuEntity("删除商品", ShowBottomSheetType.DELETE)
    )

    val remainData = listOf(
        "空瓶",
        "较少",
        "较多",
    )

    val feelData = listOf(
        "不好用",
        "一般",
        "好用",
        "回购",
    )

    // 账本页面

    val amountItemList = mutableListOf<AmountItemEntity>().apply {
        add(
            AmountItemEntity(
                name = "本月支出", amount = 5800f, type = TransactionType.EXPENSE
            )
        )
        add(
            AmountItemEntity(
                name = "本月收入", amount = 10000f, type = TransactionType.INCOME
            )
        )
        add(
            AmountItemEntity(
                name = "添加预算", amount = 4200f, type = TransactionType.PLAN
            )
        )

    }

    val bookList = mutableListOf<String>().apply {
        repeat(15) {
            add("居家生活$it")
        }
    }

    val dailyListData = mutableListOf<DailyAmountEntity>().apply {
        val children = mutableListOf<DailyItemEntity>().apply {
            repeat(5) {
                add(
                    DailyItemEntity(
                        1,
                        TransactionType.EXPENSE,
                        100f,
                        "购物",
                        "支付宝",
                        "耐克",
                    )
                )
            }
        }
        add(
            DailyAmountEntity(1, "10月8日", "今天", 1000f, 500f, children)
        )
        val children2 = mutableListOf<DailyItemEntity>().apply {
            repeat(5) {
                add(
                    DailyItemEntity(
                        1,
                        TransactionType.INCOME,
                        100f,
                        "购物",
                        "支付宝",
                        "耐克",
                    )
                )
            }
        }
        add(
            DailyAmountEntity(2, "10月8日", "今天", 1000f, 500f, children2)
        )
        add(
            DailyAmountEntity(3, "10月8日", "今天", 1000f, 500f, children2)
        )
    }

    val dashBoarList = listOf(
        DashBoarItemEntity(1, TransactionType.INCOME, 10000f, "购物", 0.5f),
        DashBoarItemEntity(2, TransactionType.INCOME, 450.23f, "护肤", 0.2f),
        DashBoarItemEntity(3, TransactionType.INCOME, 220.66f, "生活", 0.3f),
    )

    val rankList = mutableListOf<DailyItemEntity>().apply {
        repeat(5) {
            add(
                DailyItemEntity(
                    it + 1,
                    TransactionType.INCOME,
                    100f,
                    "购物",
                    "支付宝",
                    "耐克",
                )
            )
        }
        repeat(5) {
            add(
                DailyItemEntity(
                    it + 10,
                    TransactionType.EXPENSE,
                    100f,
                    "购物",
                    "支付宝",
                    "耐克",
                )
            )
        }
    }
}

