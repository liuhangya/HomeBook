package com.fanda.homebook.data.season

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fanda.homebook.data.closet.ClosetEntity

/**
 * 季节
 */
@Entity(tableName = "season") data class SeasonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 季节类型的唯一标识符，自动生成
    val name: String,     // 名称
)

@Entity(
    tableName = "closet_season_relation",
    primaryKeys = ["closetId", "seasonId"],
    foreignKeys = [
        ForeignKey(
            entity = ClosetEntity::class,
            parentColumns = ["id"],
            childColumns = ["closetId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        // 为 seasonId 创建索引
        Index(value = ["seasonId"]),
        // 也可以为 closetId 创建索引（如果还没有）
        Index(value = ["closetId"]),
        // 复合索引，如果经常需要按 closetId 和 seasonId 一起查询
        Index(value = ["closetId", "seasonId"])
    ]
)
data class ClosetSeasonRelation(
    val closetId: Int,
    val seasonId: Int
)
