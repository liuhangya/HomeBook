package com.fanda.homebook.data.stock

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock")
data class StockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name : String = "",
    val rackId : Int = 0,
    val rackSubCategoryId : Int = 0,
    val subCategoryId : Int = 0,
    val productId : Int = 0,
    val periodId : Int = 0,
    val syncBook: Boolean = false,
    val comment: String = "",
    val price: String = "",
    val imageLocalPath: String = "",
    val buyDate: Long = System.currentTimeMillis(),
    val openDate: Long = -1,
    val expireDate: Long = -1,
)