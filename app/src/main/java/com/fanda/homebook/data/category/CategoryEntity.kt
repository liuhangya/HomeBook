package com.fanda.homebook.data.category

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 分类的ID，自动生成
    val name: String, // 分类的名称
    val sortOrder: Int = 0 // 排序序号，默认为0
)

/**
 * 子分类实体类，用于数据库存储子分类信息
 * 包含子分类的ID、名称、排序序号和所属分类的ID
 * 删除规则是父分类被删除时，所有子类也被删除
 */
@Entity(
    tableName = "sub_category",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["categoryId"]),
    ]
)
data class SubCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 子分类的ID，自动生成
    val name: String, // 子分类的名称
    val sortOrder: Int = 0, // 排序序号，默认为0
    val categoryId: Int // 所属分类的ID
)

/**
 * 分类和子分类的组合实体类，用于数据库查询分类和子分类信息
 * 包含分类实体类和子分类列表
 * 注意：不是数据类
 */
class CategoryWithSubCategories {
    @Embedded
    lateinit var category: CategoryEntity

    @Relation(
        parentColumn = "id", entityColumn = "categoryId", entity = SubCategoryEntity::class
    )
    var unsortedSubCategories: List<SubCategoryEntity> = emptyList()

    // 计算属性，返回排序后的列表
    val subCategories: List<SubCategoryEntity>
        get() = unsortedSubCategories.sortedBy { it.sortOrder }
}

