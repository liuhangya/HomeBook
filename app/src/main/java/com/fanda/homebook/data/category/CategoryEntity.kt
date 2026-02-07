package com.fanda.homebook.data.category

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * 主分类实体类
 * 对应数据库中的category表，用于存储一级分类信息
 *
 * @property id 主键ID，自动生成
 * @property name 分类名称，如"餐饮"、"交通"、"购物"等
 * @property sortOrder 排序序号，默认值为0，用于控制分类在列表中的显示顺序
 */
@Entity(tableName = "category") data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 分类名称
    val name: String,

    // 排序序号，默认为0，数值越小排序越靠前
    val sortOrder: Int = 0
)

/**
 * 子分类实体类，用于数据库存储子分类信息
 * 包含子分类的ID、名称、排序序号和所属分类的ID
 * 删除规则是父分类被删除时，所有子类也被删除
 *
 * @property id 主键ID，自动生成
 * @property name 子分类名称，如"早餐"、"午餐"、"晚餐"（属于餐饮分类）
 * @property sortOrder 排序序号，默认值为0
 * @property categoryId 所属主分类的ID，外键关联到CategoryEntity表的id字段
 */
@Entity(
    tableName = "sub_category", foreignKeys = [ForeignKey(
        entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE // 级联删除：当主分类被删除时，相关子分类自动删除
    )], indices = [
        // 为categoryId字段创建索引，提高查询效率
        Index(value = ["categoryId"]),
    ]
) data class SubCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 子分类名称
    val name: String,

    // 排序序号，默认为0，数值越小排序越靠前
    val sortOrder: Int = 0,

    // 所属主分类的ID
    val categoryId: Int,
)

/**
 * 分类和子分类的组合实体类，用于数据库查询分类和子分类信息
 * 包含分类实体类和子分类列表
 * 注意：不是数据类，因为使用了Room的@Embedded和@Relation注解
 *
 * @property category 主分类实体
 * @property unsortedSubCategories 未排序的子分类列表（直接从数据库查询的结果）
 * @property subCategories 计算属性，返回按sortOrder排序后的子分类列表
 */
class CategoryWithSubCategories {
    @Embedded lateinit var category: CategoryEntity

    @Relation(
        parentColumn = "id",                 // 主分类的ID字段
        entityColumn = "categoryId",         // 子分类的外键字段
        entity = SubCategoryEntity::class    // 关联的实体类
    ) var unsortedSubCategories: List<SubCategoryEntity> = emptyList()

    /**
     * 计算属性，返回排序后的子分类列表
     * 通过sortOrder字段升序排列
     */
    val subCategories: List<SubCategoryEntity>
        get() = unsortedSubCategories.sortedBy { it.sortOrder }
}