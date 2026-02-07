package com.fanda.homebook.data.product

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 产品实体类
 * 对应数据库中的product表，用于存储产品信息
 * 可用于记录品牌、商品名称等信息
 *
 * @property id 主键ID，自增长
 * @property name 产品名称，如品牌名或商品名
 * @property sortOrder 排序序号，用于控制产品在列表中的显示顺序，默认值为0
 */
@Entity(tableName = "product")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 产品名称（如品牌名称：Nike, Adidas, 华为, 苹果等）
    val name: String,

    // 排序序号，默认为0，数值越小排序越靠前
    val sortOrder: Int = 0
)