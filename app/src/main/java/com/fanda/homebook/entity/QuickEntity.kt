package com.fanda.homebook.entity

import androidx.annotation.DrawableRes

enum class TransactionType {
    EXPENSE,      // 支出
    INCOME,       // 入账
    EXCLUDED      // 不计入收支
}

// 支出分类
data class TransactionCategory(val name: String, @DrawableRes val icon: Int, val type: TransactionType)

enum class QuickShowBottomSheetType {
    PAY_WAY,
    PRODUCT,
    STOCK_PRODUCT,
    GOODS_RACK,
    STOCK_CATEGORY,
    PERIOD ,
    CATEGORY,
    COLOR,
    SEASON,
    SIZE,
    OWNER,
    BUY_DATE,
    NONE,

}