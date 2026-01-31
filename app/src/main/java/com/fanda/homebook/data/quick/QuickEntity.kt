package com.fanda.homebook.data.quick

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fanda.homebook.data.pay.PayWayEntity
import com.fanda.homebook.data.transaction.TransactionEntity
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.entity.TransactionType

@Entity(tableName = "quick", foreignKeys = [
    ForeignKey(
        entity = TransactionEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    ),
    ForeignKey(
        entity = TransactionSubEntity::class,
        parentColumns = ["id"],
        childColumns = ["subCategoryId"],
        onDelete = ForeignKey.SET_NULL
    ),
    ForeignKey(
        entity = PayWayEntity::class,
        parentColumns = ["id"],
        childColumns = ["payWayId"],
        onDelete = ForeignKey.CASCADE
    )
], indices = [
    Index(value = ["categoryId"]),
    Index(value = ["subCategoryId"]),
    Index(value = ["payWayId"]),
    Index(value = ["categoryId", "subCategoryId"]),
    Index(value = ["categoryId", "subCategoryId", "payWayId"])
])
data class QuickEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val price: String = "",
    val categoryId: Int? = null,
    val subCategoryId: Int? = null,
    val quickComment: String = "",
    val payWayId: Int = 0,
)

data class AddQuickEntity(

    @Embedded val quick: QuickEntity,

    @Relation(
        parentColumn = "categoryId", entityColumn = "id"
    ) val category: TransactionEntity? = null,

    @Relation(
        parentColumn = "subCategoryId", entityColumn = "id"
    ) val subCategory: TransactionSubEntity? = null,

    @Relation(
        parentColumn = "payWayId", entityColumn = "id"
    ) val payWay: PayWayEntity? = null,
)

