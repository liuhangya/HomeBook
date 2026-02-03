package com.fanda.homebook.data.quick

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.data.pay.PayWayEntity
import com.fanda.homebook.data.transaction.TRANSACTION_EXPENSE
import com.fanda.homebook.data.transaction.TRANSACTION_INCOME
import com.fanda.homebook.data.transaction.TransactionEntity
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.entity.TransactionAmountType
import com.fanda.homebook.tools.DATE_FORMAT_MD
import com.fanda.homebook.tools.convertMillisToDate

@Entity(
    tableName = "quick",
    foreignKeys = [ForeignKey(
        entity = TransactionEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = TransactionSubEntity::class, parentColumns = ["id"], childColumns = ["subCategoryId"], onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = BookEntity::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = PayWayEntity::class, parentColumns = ["id"], childColumns = ["payWayId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["categoryId"]), Index(value = ["bookId"]), Index(value = ["subCategoryId"]), Index(value = ["payWayId"]), Index(value = ["categoryId", "subCategoryId"]), Index(value = ["categoryId", "subCategoryId", "payWayId"]), Index(
        value = ["categoryId", "subCategoryId", "payWayId", "bookId"]
    )]
) data class QuickEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val price: String = "",
    val categoryId: Int = 1,
    val categoryType: Int = TransactionAmountType.EXPENSE.ordinal,
    val subCategoryId: Int? = null,
    val quickComment: String = "",
    val payWayId: Int = 0,
    val bookId: Int? = null,
    val closetId: Int? = null,
    val stockId: Int? = null,
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

    @Relation(
        parentColumn = "bookId", entityColumn = "id"
    ) val book: BookEntity? = null,
)

// 日期分组数据
data class TransactionDateGroup(
    val date: Long, // 时间戳，用于排序
    val displayDate: String, // 显示文本：今天、昨天、周一等
    val sortOrder: Int, // 排序顺序
    val transactions: List<AddQuickEntity>,

    // 每天的汇总信息
    val totalIncome: Double = transactions.filter { it.category?.type == TransactionAmountType.INCOME.ordinal }.sumOf { it.quick.price.toDouble() },
    val totalExpense: Double = transactions.filter { it.category?.type == TransactionAmountType.EXPENSE.ordinal }.sumOf { it.quick.price.toDouble() }
) {
    val dateFormat: String = convertMillisToDate(date, format = DATE_FORMAT_MD)
}

// 分组数据列表
data class TransactionGroupedData(
    val year: Int, val month: Int, val groups: List<TransactionDateGroup>,
    // 每月的汇总
    val monthTotalIncome: Double = groups.sumOf { it.totalIncome }, val monthTotalExpense: Double = groups.sumOf { it.totalExpense }
)


