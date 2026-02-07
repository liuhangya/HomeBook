package com.fanda.homebook.data.closet

import androidx.annotation.DrawableRes
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.season.ClosetSeasonRelation
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeEntity

/*
* 关联查询组合对象 - 不是数据库中的实体对象，是关联查询后组装的数据对象
* 包含衣橱物品的所有关联信息：颜色、季节、产品、尺码、所有者、分类等
*/
data class AddClosetEntity(
    // 基础衣橱实体
    @Embedded val closet: ClosetEntity,

    // 颜色类型（可空：当颜色被删除时为空）
    @Relation(
        parentColumn = "colorTypeId", entityColumn = "id"
    ) val colorType: ColorTypeEntity?,

    // 季节列表（多对多关系）
    @Relation(
        entity = SeasonEntity::class, parentColumn = "id", entityColumn = "id", associateBy = Junction(
            value = ClosetSeasonRelation::class, parentColumn = "closetId", entityColumn = "seasonId"
        )
    ) val seasons: List<SeasonEntity>?,

    // 产品信息（可空：当产品被删除时为空）
    @Relation(
        parentColumn = "productId", entityColumn = "id"
    ) val product: ProductEntity?,

    // 尺码信息（可空：当尺码被删除时为空）
    @Relation(
        parentColumn = "sizeId", entityColumn = "id"
    ) val size: SizeEntity?,

    // 所有者信息
    @Relation(
        parentColumn = "ownerId", entityColumn = "id"
    ) val owner: OwnerEntity?,

    // 主分类信息（可空：当分类被删除或未设置时为空）
    @Relation(
        parentColumn = "categoryId", entityColumn = "id"
    ) val category: CategoryEntity?,

    // 子分类信息（可空：当子分类被删除或未设置时为空）
    @Relation(
        parentColumn = "subCategoryId", entityColumn = "id"
    ) val subCategory: SubCategoryEntity?
)

/**
 * 衣橱实体类
 * 对应数据库中的closet表，存储衣橱物品的详细信息
 * 包含多个外键关联到其他表（颜色、产品、尺码、所有者、分类等）
 */
@Entity(
    tableName = "closet", foreignKeys = [
        // 关联颜色类型表：当颜色被删除时，外键设为NULL
        ForeignKey(
            entity = ColorTypeEntity::class, parentColumns = ["id"], childColumns = ["colorTypeId"], onDelete = ForeignKey.SET_NULL
        ),

        // 关联产品表：当产品被删除时，外键设为NULL
        ForeignKey(
            entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.SET_NULL
        ),

        // 关联尺码表：当尺码被删除时，外键设为NULL
        ForeignKey(
            entity = SizeEntity::class, parentColumns = ["id"], childColumns = ["sizeId"], onDelete = ForeignKey.SET_NULL
        ),

        // 关联用户表：当所有者被删除时，级联删除所有衣橱记录
        ForeignKey(
            entity = OwnerEntity::class, parentColumns = ["id"], childColumns = ["ownerId"], onDelete = ForeignKey.CASCADE
        ),

        // 关联分类表：当分类被删除时，外键设为NULL
        ForeignKey(
            entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL
        ),

        // 关联子分类表：当子分类被删除时，外键设为NULL
        ForeignKey(
            entity = SubCategoryEntity::class, parentColumns = ["id"], childColumns = ["subCategoryId"], onDelete = ForeignKey.SET_NULL
        )], indices = [
        // 为所有外键字段创建索引，提高查询性能
        Index(value = ["colorTypeId"]), Index(value = ["productId"]), Index(value = ["sizeId"]), Index(value = ["ownerId"]), Index(value = ["categoryId"]), Index(value = ["subCategoryId"]),

        // 复合索引：常用于按所有者和分类查询
        Index(value = ["ownerId", "categoryId"])]
) data class ClosetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 外键字段
    val colorTypeId: Int? = null,       // 颜色类型ID
    val productId: Int? = null,         // 产品ID
    val sizeId: Int? = null,            // 尺码ID
    val ownerId: Int = 0,               // 所有者ID

    // 基本信息字段
    val date: Long = -1L,                // 日期（如购买日期、使用日期）
    val imageLocalPath: String = "",     // 图片本地路径
    val comment: String = "",            // 备注/描述
    val syncBook: Boolean = false,       // 是否同步到账本
    val wearCount: Int = 1,              // 穿着次数
    val price: String = "",              // 价格

    // 分类字段
    val categoryId: Int? = null,         // 主分类ID
    val subCategoryId: Int? = null,      // 子分类ID

    // 系统字段
    val createDate: Long = System.currentTimeMillis(), // 创建时间
    val moveToTrash: Boolean = false     // 是否已移入回收站
)

/**
 * 衣橱网格项（用于主分类视图）
 * 显示每个分类下的衣橱物品汇总信息
 *
 * @property imageLocalPath 图片路径（通常显示分类下第一件物品的图片）
 * @property category 分类实体
 * @property count 该分类下的物品数量
 * @property moveToTrash 是否已移入回收站
 */
data class ClosetGridItem(
    val imageLocalPath: String, val category: CategoryEntity, val count: Int, val moveToTrash: Boolean = false
)

/**
 * 衣橱子分类网格项
 * 显示每个子分类下的衣橱物品汇总信息
 *
 * @property imageLocalPath 图片路径
 * @property category 子分类实体
 * @property count 该子分类下的物品数量
 */
data class ClosetSubCategoryGridItem(
    val imageLocalPath: String, val category: SubCategoryEntity, val count: Int
)

/**
 * 衣橱详情网格项
 * 用于显示单个衣橱物品的详细信息，支持选中状态
 *
 * @property addClosetEntity 完整的衣橱实体（包含所有关联信息）
 * @property isSelected 是否被选中（用于批量操作）
 */
data class ClosetDetailGridItem(
    val addClosetEntity: AddClosetEntity, var isSelected: Boolean = false
) {
    /**
     * 复制方法，方便更新选中状态
     *
     * @param newSelected 新的选中状态
     * @return 更新后的ClosetDetailGridItem
     */
    fun copyWithSelected(newSelected: Boolean): ClosetDetailGridItem {
        return this.copy(isSelected = newSelected)
    }
}

/**
 * 分类底部菜单项实体
 * 用于分类管理页面中的底部操作菜单
 *
 * @property name 菜单项名称
 * @property icon 菜单项图标资源ID
 * @property type 对应的底部弹窗类型
 */
data class CategoryBottomMenuEntity(
    val name: String, @DrawableRes val icon: Int, val type: ShowBottomSheetType
)