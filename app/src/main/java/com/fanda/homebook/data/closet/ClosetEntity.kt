package com.fanda.homebook.data.closet

import androidx.annotation.DrawableRes
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.entity.ShowBottomSheetType


/*
* 不是数据库中的实体对象，是关联查询后组装的数据对象
* */
data class AddClosetEntity(
    @Embedded val closet: ClosetEntity,

    @Relation(
        parentColumn = "colorTypeId", entityColumn = "id",
    ) val colorType: ColorTypeEntity?, // 可空，删除颜色被删除了

    @Relation(
        parentColumn = "seasonId", entityColumn = "id"
    ) val season: SeasonEntity?,// 可空，删除季节被删除了

    @Relation(
        parentColumn = "productId", entityColumn = "id"
    ) val product: ProductEntity?, // 可空，删除产品被删除了

    @Relation(
        parentColumn = "sizeId", entityColumn = "id"
    ) val size: SizeEntity?,// 可空，删除尺寸被删除了

    @Relation(
        parentColumn = "ownerId", entityColumn = "id"
    ) val owner: OwnerEntity?,

    @Relation(
        parentColumn = "categoryId", entityColumn = "id"
    ) val category: CategoryEntity?, @Relation(
        parentColumn = "subCategoryId", entityColumn = "id"
    ) val subCategory: SubCategoryEntity?
)

@Entity(
    tableName = "closet", foreignKeys = [
        // 关联颜色类型表
        ForeignKey(
            entity = ColorTypeEntity::class, parentColumns = ["id"], childColumns = ["colorTypeId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联季节表
        ForeignKey(
            entity = SeasonEntity::class, parentColumns = ["id"], childColumns = ["seasonId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联产品表
        ForeignKey(
            entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联尺码表
        ForeignKey(
            entity = SizeEntity::class, parentColumns = ["id"], childColumns = ["sizeId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联用户表
        ForeignKey(
            entity = OwnerEntity::class, parentColumns = ["id"], childColumns = ["ownerId"], onDelete = ForeignKey.CASCADE
        ),
        // 关联分类表
        ForeignKey(
            entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联子分类表
        ForeignKey(
            entity = SubCategoryEntity::class, parentColumns = ["id"], childColumns = ["subCategoryId"], onDelete = ForeignKey.SET_NULL
        )], indices = [
        // 为所有外键字段创建索引
        Index(value = ["colorTypeId"]), Index(value = ["seasonId"]), Index(value = ["productId"]), Index(value = ["sizeId"]), Index(value = ["ownerId"]), Index(value = ["categoryId"]), Index(value = ["subCategoryId"]),
        // 复合索引用于常用查询
        Index(value = ["ownerId", "categoryId"]), Index(value = ["ownerId", "seasonId", "categoryId"])]
) data class ClosetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val colorTypeId: Int? = null,
    val seasonId: Int? = null,
    val productId: Int? = null,
    val sizeId: Int? = null,
    val ownerId: Int = 0,
    val date: Long = -1L,
    val imageLocalPath: String = "",
    val comment: String = "",
    val syncBook: Boolean = false,
    val wearCount: Int = 1,
    val price: String = "",
    val categoryId: Int? = null,
    val createDate: Long = System.currentTimeMillis(),
    val subCategoryId: Int? = null,
    val moveToTrash: Boolean = false
)

data class ClosetGridItem(
    val imageLocalPath: String, val category: CategoryEntity, val count: Int ,val moveToTrash: Boolean = false
)

data class ClosetSubCategoryGridItem(
    val imageLocalPath: String, val category: SubCategoryEntity, val count: Int
)

data class ClosetDetailGridItem(val addClosetEntity: AddClosetEntity, var isSelected: Boolean = false) {
    // 提供一个复制方法，方便更新选中状态
    fun copyWithSelected(newSelected: Boolean): ClosetDetailGridItem {
        return this.copy(isSelected = newSelected)
    }
}

data class CategoryBottomMenuEntity(val name: String, @DrawableRes val icon: Int, val type: ShowBottomSheetType)


