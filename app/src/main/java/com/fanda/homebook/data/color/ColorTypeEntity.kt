package com.fanda.homebook.data.color

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 颜色类型实体类，用于数据库存储颜色信息
 * 包含颜色的ID、名称、颜色值和排序序号
 */
@Entity(tableName = "color_type") data class ColorTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 颜色类型的唯一标识符，自动生成
    val name: String,     // 颜色名称
    val color: Long,      // 颜色值
    val sortOrder: Int = 0 // 排序序号，默认为0
)