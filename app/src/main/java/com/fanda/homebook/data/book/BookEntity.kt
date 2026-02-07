package com.fanda.homebook.data.book

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账本实体类
 * 对应数据库中的book表，用于存储账本信息
 *
 * @property id 主键ID，自增长
 * @property name 账本名称，如"家庭账本"、"个人账本"等
 * @property sortOrder 排序序号，用于控制账本在列表中的显示顺序，默认值为0
 */
@Entity(tableName = "book")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 账本名称
    val name: String,

    // 排序序号，默认为0，数值越小排序越靠前
    val sortOrder: Int = 0
)