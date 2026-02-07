package com.fanda.homebook.data.size

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 尺码实体类
 * 对应数据库中的size表，用于存储尺码信息
 * 可用于记录衣物、鞋子等的尺码标准
 *
 * @property id 主键ID，自增长
 * @property name 尺码名称，如"S"、"M"、"L"、"XL"或具体尺码如"36"、"37"等
 * @property sortOrder 排序序号，用于控制尺码在列表中的显示顺序，默认值为0
 */
@Entity(tableName = "size") data class SizeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 尺码名称
    val name: String,

    // 排序序号，默认为0，数值越小排序越靠前
    val sortOrder: Int = 0
)