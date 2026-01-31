package com.fanda.homebook.data.book

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book") data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val name: String,     // 名称
    val sortOrder: Int = 0 // 排序序号，默认为0
)