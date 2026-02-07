package com.fanda.homebook.data.color

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 颜色类型实体类，用于数据库存储颜色信息
 * 包含颜色的ID、名称、颜色值和排序序号
 */
@Entity(tableName = "color") data class ColorTypeEntity(
    /**
     * 颜色类型的唯一标识符，自动生成
     * 这是数据库的主键，每次插入新记录时会自动递增
     */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /**
     * 颜色名称
     * 描述性名称，如"红色"、"蓝色"、"绿色"、"黄色"等
     */
    val name: String,

    /**
     * 颜色值
     * 使用Long类型存储，通常表示ARGB格式的十六进制颜色值
     * 例如：0xFF0000FF 表示不透明的蓝色
     */
    val color: Long,

    /**
     * 排序序号，默认为0
     * 用于控制颜色在列表中的显示顺序，数值越小排序越靠前
     * 可用于自定义颜色显示顺序，如常用颜色靠前显示
     */
    val sortOrder: Int = 0
)