package com.fanda.homebook.data.owner

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 归属
 */
@Entity(tableName = "owner") data class OwnerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,     // 名称
)