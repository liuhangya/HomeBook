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

@Entity(tableName = "closet") data class ClosetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val colorTypeId: Int = -1,
    val seasonId: Int = -1,
    val productId: Int = -1,
    val sizeId: Int = -1,
    val ownerId: Int = -1,
    val date: Long = System.currentTimeMillis(),
    val imageLocalPath: String = "",
    val comment: String = "",
    val syncBook: Boolean = false,
    val wearCount: Int = 0,
    val price: String = "",
    val categoryId: Int = -1,
    val subCategoryId: Int = -1
)


