package com.fanda.homebook.data.period

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 使用期限实体类
 * 对应数据库中的period表，用于存储物品的使用期限类型信息
 * 用于记录化妆品、食品等物品的开封后使用期限
 */
@Entity(tableName = "period") data class PeriodEntity(
    /**
     * 使用期限类型的唯一标识符，自动生成
     * 这是数据库的主键，每次插入新记录时会自动递增
     */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /**
     * 使用期限名称
     * 描述性名称，如"开封后3个月"、"开封后6个月"、"开封后12个月"、
     * "未开封2年"、"开封后30天"等
     */
    val name: String
)