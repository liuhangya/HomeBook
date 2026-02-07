package com.fanda.homebook.data.owner

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 所有者实体类
 * 对应数据库中的owner表，用于存储物品所有者/用户信息
 * 用于标识衣橱物品、账本记录等数据的归属
 */
@Entity(tableName = "owner") data class OwnerEntity(
    /**
     * 所有者唯一标识符，自动生成
     * 这是数据库的主键，每次插入新记录时会自动递增
     */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /**
     * 所有者名称
     * 用于显示的所有者名称，如"张三"、"李四"、"家庭公用"等
     */
    val name: String,

    /**
     * 是否被选中/是否为当前活跃用户
     * 用于标识应用当前正在使用的所有者账户
     * 通常只有一个所有者被标记为选中状态
     */
    val selected: Boolean = false
)