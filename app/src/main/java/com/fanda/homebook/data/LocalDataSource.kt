package com.fanda.homebook.data

import com.fanda.homebook.R
import com.fanda.homebook.entity.ExpenseCategory

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
}

