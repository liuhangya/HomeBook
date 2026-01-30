package com.fanda.homebook.data.quick

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fanda.homebook.entity.TransactionType

@Entity(tableName = "quick") data class QuickEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionType: Int = TransactionType.EXPENSE.ordinal,
    val date: Long = System.currentTimeMillis(),
    val price: String = "",
    val categoryId: Int? = null,
    val subCategoryId: Int? = null,
)

