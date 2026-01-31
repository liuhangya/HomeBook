package com.fanda.homebook.entity

import androidx.annotation.DrawableRes

enum class TransactionType {
    EXPENSE,      // 支出
    INCOME,       // 入账
    EXCLUDED,      // 不计入收支
    PLAN,      // 预算
}

// 支出分类
data class TransactionCategory(val name: String,  val icon: Int, val type: TransactionType)

enum class ShowBottomSheetType {
    PAY_WAY, PRODUCT, STOCK_PRODUCT, STOCK_CATEGORY, CATEGORY, SHELF_MONTH, USAGE_PERIOD, COLOR, SEASON, SIZE,EXPIRE_DATE,DATE,OPEN_DATE, OWNER, BUY_DATE, SELECT_IMAGE, DELETE, EDIT,COPY, MOVE, ALL_SELECTED,USED_UP,
    RACK, NONE,

}