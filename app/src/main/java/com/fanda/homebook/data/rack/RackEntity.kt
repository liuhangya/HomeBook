package com.fanda.homebook.data.rack

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * 货架
 */
@Entity(tableName = "rack") data class RackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val name: String,     // 名称
    val selected: Boolean = false, val sortOrder: Int = 0 // 排序序号，默认为0
)

/**
 * 货架下的子分类
 */
@Entity(
    tableName = "rack_sub_category",
    foreignKeys = [ForeignKey(entity = RackEntity::class, parentColumns = ["id"], childColumns = ["rackId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["rackId"])]
) data class RackSubCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val name: String, val sortOrder: Int = 0, val rackId: Int
)

class RackWithSubCategories {
    @Embedded lateinit var rack: RackEntity

    @Relation(
        parentColumn = "id", entityColumn = "rackId", entity = RackSubCategoryEntity::class
    ) var unsortedSubCategories: List<RackSubCategoryEntity> = emptyList()

    // 获取排序后的列表
    val subCategories: List<RackSubCategoryEntity>
        get() = unsortedSubCategories.sortedBy { it.sortOrder }
}

