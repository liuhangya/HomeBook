package com.fanda.homebook.entity

import androidx.annotation.DrawableRes

enum class TransactionType {
    EXPENSE,      // 支出
    INCOME,       // 入账
    EXCLUDED      // 不计入收支
}

// 支出分类
data class ExpenseCategory(val name: String, @DrawableRes val icon: Int)