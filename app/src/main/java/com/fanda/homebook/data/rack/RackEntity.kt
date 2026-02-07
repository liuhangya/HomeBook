package com.fanda.homebook.data.rack

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * 货架实体类
 * 对应数据库中的rack表，用于存储货架信息
 * 用于管理物品的存放位置，如"衣柜"、"鞋柜"、"书架"等
 *
 * @property id 主键ID，自动生成
 * @property name 货架名称，如"主衣柜"、"鞋柜"、"书架"等
 * @property selected 是否被选中/是否为当前活跃货架
 * @property sortOrder 排序序号，用于控制货架在列表中的显示顺序，默认值为0
 */
@Entity(tableName = "rack") data class RackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 货架名称
    val name: String,

    // 是否被选中/是否为当前活跃货架
    val selected: Boolean = false,

    // 排序序号，默认为0，数值越小排序越靠前
    val sortOrder: Int = 0
)

/**
 * 货架子分类实体类
 * 对应数据库中的rack_sub_category表，用于存储货架下的子分类信息
 * 用于更精细地管理货架内的物品分区，如"上层"、"中层"、"下层"等
 *
 * @property id 主键ID，自动生成
 * @property name 子分类名称，如"上层"、"中层"、"下层"等
 * @property sortOrder 排序序号，默认值为0
 * @property rackId 所属货架的ID，外键关联到RackEntity表的id字段
 */
@Entity(
    tableName = "rack_sub_category", foreignKeys = [ForeignKey(
        entity = RackEntity::class, parentColumns = ["id"], childColumns = ["rackId"], onDelete = ForeignKey.CASCADE // 级联删除：当货架被删除时，相关子分类自动删除
    )], indices = [
        // 为rackId字段创建索引，提高查询效率
        Index(value = ["rackId"])]
) data class RackSubCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 子分类名称
    val name: String,

    // 排序序号，默认为0，数值越小排序越靠前
    val sortOrder: Int = 0,

    // 所属货架的ID
    val rackId: Int
)

/**
 * 货架和货架子分类的组合实体类
 * 用于数据库查询货架和货架子分类信息
 * 包含货架实体和货架子分类列表
 *
 * @property rack 货架实体
 * @property unsortedSubCategories 未排序的货架子分类列表（直接从数据库查询的结果）
 * @property subCategories 计算属性，返回按sortOrder排序后的货架子分类列表
 */
class RackWithSubCategories {
    @Embedded lateinit var rack: RackEntity

    @Relation(
        parentColumn = "id",                 // 货架的ID字段
        entityColumn = "rackId",             // 货架子分类的外键字段
        entity = RackSubCategoryEntity::class // 关联的实体类
    ) var unsortedSubCategories: List<RackSubCategoryEntity> = emptyList()

    /**
     * 计算属性，返回排序后的货架子分类列表
     * 通过sortOrder字段升序排列
     */
    val subCategories: List<RackSubCategoryEntity>
        get() = unsortedSubCategories.sortedBy { it.sortOrder }
}