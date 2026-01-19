package com.fanda.homebook.data.closet

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fanda.homebook.data.color.ColorTypeEntity


/*
* 不是数据库中的实体对象，是关联查询后组装的数据对象
* */
data class AddClosetEntity(
    @Embedded val closet: ClosetEntity,

    @Relation(
        parentColumn = "colorTypeId", entityColumn = "id"
    ) val colorType: ColorTypeEntity? // 可空，删除颜色被删除了
)

@Entity(tableName = "closet")
data class ClosetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val colorTypeId: Int = -1,
    var seasonId: Int = -1
)


