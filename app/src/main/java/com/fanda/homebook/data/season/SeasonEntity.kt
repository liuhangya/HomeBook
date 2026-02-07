package com.fanda.homebook.data.season

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fanda.homebook.data.closet.ClosetEntity

/**
 * 季节实体类
 * 对应数据库中的season表，用于存储季节信息
 * 用于记录衣物适合的季节，如"春季"、"夏季"、"秋季"、"冬季"
 *
 * @property id 季节的唯一标识符，自动生成
 * @property name 季节名称，如"春季"、"夏季"、"秋季"、"冬季"、"四季"
 */
@Entity(tableName = "season") data class SeasonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 季节名称
    val name: String,
)

/**
 * 衣柜季节关联实体类
 * 对应数据库中的closet_season_relation表，用于存储衣柜物品与季节的多对多关联关系
 * 实现衣柜物品与季节的关联（一个衣物可能适合多个季节，一个季节可能包含多件衣物）
 */
@Entity(
    tableName = "closet_season_relation", primaryKeys = ["closetId", "seasonId"],  // 复合主键
    foreignKeys = [
        // 关联衣柜表：当衣柜物品被删除时，级联删除关联关系
        ForeignKey(
            entity = ClosetEntity::class, parentColumns = ["id"], childColumns = ["closetId"], onDelete = ForeignKey.CASCADE
        ),
        // 关联季节表：当季节被删除时，级联删除关联关系
        ForeignKey(
            entity = SeasonEntity::class, parentColumns = ["id"], childColumns = ["seasonId"], onDelete = ForeignKey.CASCADE
        )], indices = [
        // 为 seasonId 创建索引，提高按季节查询的效率
        Index(value = ["seasonId"]),
        // 也可以为 closetId 创建索引（如果还没有）
        Index(value = ["closetId"]),
        // 复合索引，如果经常需要按 closetId 和 seasonId 一起查询
        Index(value = ["closetId", "seasonId"])]
) data class ClosetSeasonRelation(
    // 衣柜物品ID
    val closetId: Int,

    // 季节ID
    val seasonId: Int
)