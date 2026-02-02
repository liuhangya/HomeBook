package com.fanda.homebook.data

import com.fanda.homebook.R
import com.fanda.homebook.data.closet.CategoryBottomMenuEntity
import com.fanda.homebook.data.stock.StockMenuEntity
import com.fanda.homebook.entity.DailyAmountEntity
import com.fanda.homebook.entity.DailyItemEntity
import com.fanda.homebook.entity.DashBoarItemEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.entity.TransactionCategory
import com.fanda.homebook.entity.TransactionAmountType

object LocalDataSource {
    val expenseCategoryData = listOf(
        TransactionCategory("餐饮", R.mipmap.icon_dining, TransactionAmountType.EXPENSE),
        TransactionCategory("交通", R.mipmap.icon_traffic, TransactionAmountType.EXPENSE),
        TransactionCategory("服饰", R.mipmap.icon_clothing, TransactionAmountType.EXPENSE),
        TransactionCategory("护肤", R.mipmap.icon_skincare, TransactionAmountType.EXPENSE),
        TransactionCategory("购物", R.mipmap.icon_shopping, TransactionAmountType.EXPENSE),
        TransactionCategory("服务", R.mipmap.icon_services, TransactionAmountType.EXPENSE),
        TransactionCategory("医疗", R.mipmap.icon_health, TransactionAmountType.EXPENSE),
        TransactionCategory("娱乐", R.mipmap.icon_play, TransactionAmountType.EXPENSE),
        TransactionCategory("生活", R.mipmap.icon_daily, TransactionAmountType.EXPENSE),
        TransactionCategory("旅行", R.mipmap.icon_travel, TransactionAmountType.EXPENSE),
        TransactionCategory("保险", R.mipmap.icon_insurance, TransactionAmountType.EXPENSE),
        TransactionCategory("发红包", R.mipmap.icon_red_envelope, TransactionAmountType.EXPENSE),
        TransactionCategory("人情", R.mipmap.icon_social, TransactionAmountType.EXPENSE),
        TransactionCategory("其他", R.mipmap.icon_others, TransactionAmountType.EXPENSE),
    )

    val incomeCategoryData = listOf(
        TransactionCategory("工资", R.mipmap.icon_salary, TransactionAmountType.INCOME),
        TransactionCategory("收红包", R.mipmap.icon_get_money, TransactionAmountType.INCOME),
        TransactionCategory("人情", R.mipmap.icon_social, TransactionAmountType.INCOME),
        TransactionCategory("奖金", R.mipmap.icon_bonus, TransactionAmountType.INCOME),
        TransactionCategory("其他", R.mipmap.icon_others, TransactionAmountType.INCOME),
    )

    val excludeCategoryData = listOf(
        TransactionCategory("理财", R.mipmap.icon_finance, TransactionAmountType.EXCLUDED),
        TransactionCategory("借还款", R.mipmap.icon_debts, TransactionAmountType.EXCLUDED),
        TransactionCategory("其他", R.mipmap.icon_others, TransactionAmountType.EXCLUDED),
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

    val dailyListData = mutableListOf<DailyAmountEntity>().apply {
        val children = mutableListOf<DailyItemEntity>().apply {
            repeat(5) {
                add(
                    DailyItemEntity(
                        1,
                        TransactionAmountType.EXPENSE,
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
                        TransactionAmountType.INCOME,
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
        DashBoarItemEntity(1, TransactionAmountType.INCOME, 10000f, "购物", 0.5f),
        DashBoarItemEntity(2, TransactionAmountType.INCOME, 450.23f, "护肤", 0.2f),
        DashBoarItemEntity(3, TransactionAmountType.INCOME, 220.66f, "生活", 0.3f),
    )

    val rankList = mutableListOf<DailyItemEntity>().apply {
        repeat(5) {
            add(
                DailyItemEntity(
                    it + 1,
                    TransactionAmountType.INCOME,
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
                    TransactionAmountType.EXPENSE,
                    100f,
                    "购物",
                    "支付宝",
                    "耐克",
                )
            )
        }
    }
}

