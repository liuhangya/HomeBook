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

