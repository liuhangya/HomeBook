package com.fanda.homebook.data.period

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 季节
 */
@Entity(tableName = "period") data class PeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 季节类型的唯一标识符，自动生成
    val name: String,     // 名称
)