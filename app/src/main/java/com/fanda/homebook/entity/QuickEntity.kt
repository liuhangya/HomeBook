package com.fanda.homebook.entity

import androidx.annotation.DrawableRes

enum class TransactionType {
    EXPENSE,      // 支出
    INCOME,       // 入账
    EXCLUDED,      // 不计入收支
    PLAN,      // 预算
}

// 支出分类
data class TransactionCategory(val name: String, @DrawableRes val icon: Int, val type: TransactionType)

enum class ShowBottomSheetType {
    PAY_WAY, PRODUCT, STOCK_PRODUCT, GOODS_RACK, STOCK_CATEGORY, PERIOD, CATEGORY, USAGE_PERIOD, COLOR, SEASON, SIZE,EXPIRE_DATE,OPEN_DATE, OWNER, BUY_DATE, SELECT_IMAGE, DELETE, COPY, MOVE, ALL_SELECTED,
    RACK, NONE,

}