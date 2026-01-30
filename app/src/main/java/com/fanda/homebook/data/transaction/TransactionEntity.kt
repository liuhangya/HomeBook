package com.fanda.homebook.data.transaction

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fanda.homebook.data.category.CategoryEntity


@Entity(tableName = "transaction_category") data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 分类的ID，自动生成
    val name: String, // 分类的名称
)

@Entity(
    tableName = "transaction_sub_category",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["categoryId"]),
    ]
) data class TransactionSubEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 子分类的ID，自动生成
    val name: String, // 子分类的名称
    val sortOrder: Int = 0, // 排序序号，默认为0
    val categoryId: Int,// 所属分类的ID
    val type: Int = 0
)

class TransactionWithSubCategories {
    @Embedded lateinit var category: TransactionEntity

    @Relation(
        parentColumn = "id", entityColumn = "categoryId", entity = TransactionSubEntity::class
    ) var unsortedSubCategories: List<TransactionSubEntity> = emptyList()

    // 计算属性，返回排序后的列表
    val subCategories: List<TransactionSubEntity>
        get() = unsortedSubCategories.sortedBy { it.sortOrder }
}


enum class TransactionType(val type: Int) {
    DINING(0), TRAFFIC(1), CLOTHING(2), SKINCARE(3), SHOPPING(4), SERVICES(5), HEALTH(6), PLAY(7), DAILY(8), TRAVEL(9), INSURANCE(10), RED_ENVELOPE(11), SOCIAL(12), SALARY(13), GET_ENVELOPE(14), BONUS(
        15
    ),
    FINANCE(16), DEBTS(17), OTHERS(18),
}