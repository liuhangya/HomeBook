package com.fanda.homebook.data.quick

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.data.pay.PayWayEntity
import com.fanda.homebook.data.transaction.TransactionEntity
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.common.entity.TransactionAmountType
import com.fanda.homebook.tools.DATE_FORMAT_MD
import com.fanda.homebook.tools.convertMillisToDate

/**
 * 快速记账实体类
 * 对应数据库中的quick表，用于存储快速记账记录
 * 包含多个外键关联到其他表（分类、子分类、账本、付款方式等）
 */
@Entity(
    tableName = "quick", foreignKeys = [
        // 关联主分类表：当分类被删除时，外键设为NULL
        ForeignKey(
            entity = TransactionEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联子分类表：当子分类被删除时，外键设为NULL
        ForeignKey(
            entity = TransactionSubEntity::class, parentColumns = ["id"], childColumns = ["subCategoryId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联账本表：当账本被删除时，外键设为NULL
        ForeignKey(
            entity = BookEntity::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联付款方式表：当付款方式被删除时，外键设为NULL
        ForeignKey(
            entity = PayWayEntity::class, parentColumns = ["id"], childColumns = ["payWayId"], onDelete = ForeignKey.SET_NULL
        )], indices = [
        // 为所有外键字段创建索引，提高查询性能
        Index(value = ["categoryId"]), Index(value = ["bookId"]), Index(value = ["subCategoryId"]), Index(value = ["payWayId"]),

        // 复合索引用于常用查询
        Index(value = ["categoryId", "subCategoryId"]), Index(value = ["categoryId", "subCategoryId", "payWayId"]), Index(value = ["categoryId", "subCategoryId", "payWayId", "bookId"])]
) data class QuickEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 交易日期（时间戳）
    val date: Long = System.currentTimeMillis(),

    // 交易金额（字符串类型，可包含小数点）
    val price: String = "",

    // 分类相关字段
    val categoryId: Int = 1,                            // 主分类ID，默认值为1
    val categoryType: Int = TransactionAmountType.EXPENSE.ordinal, // 分类类型（支出/收入）
    val subCategoryId: Int? = null,                     // 子分类ID（可选）

    // 备注信息
    val quickComment: String = "",

    // 支付和归属相关字段
    val payWayId: Int? = null,                          // 付款方式ID（可选）
    val bookId: Int? = null,                            // 账本ID（可选）

    // 同步标记字段
    val syncCloset: Boolean = false,                    // 是否同步到衣橱
    val syncStock: Boolean = false,                     // 是否同步到库存
)

/**
 * 关联查询组合对象
 * 包含快速记账记录的所有关联信息：分类、子分类、付款方式、账本等
 */
data class AddQuickEntity(
    // 基础快速记账实体
    @Embedded val quick: QuickEntity,

    // 主分类信息（可空：当分类被删除时为空）
    @Relation(
        parentColumn = "categoryId", entityColumn = "id"
    ) val category: TransactionEntity? = null,

    // 子分类信息（可空：当子分类被删除或未设置时为空）
    @Relation(
        parentColumn = "subCategoryId", entityColumn = "id"
    ) val subCategory: TransactionSubEntity? = null,

    // 付款方式信息（可空：当付款方式被删除或未设置时为空）
    @Relation(
        parentColumn = "payWayId", entityColumn = "id"
    ) val payWay: PayWayEntity? = null,

    // 账本信息（可空：当账本被删除或未设置时为空）
    @Relation(
        parentColumn = "bookId", entityColumn = "id"
    ) val book: BookEntity? = null,
)

/**
 * 日期分组数据
 * 用于按日期对交易记录进行分组显示
 *
 * @property date 时间戳，用于排序
 * @property displayDate 显示文本：今天、昨天、周一等用户友好格式
 * @property sortOrder 排序顺序，数值越小越靠前
 * @property transactions 该日期下的所有交易记录
 * @property totalIncome 该日期总收入（计算属性）
 * @property totalExpense 该日期总支出（计算属性）
 */
data class TransactionDateGroup(
    val date: Long, val displayDate: String, val sortOrder: Int, val transactions: List<AddQuickEntity>,

    // 计算每天的汇总信息
    val totalIncome: Double = transactions.filter { it.category?.type == TransactionAmountType.INCOME.ordinal }.sumOf { it.quick.price.toDouble() },

    val totalExpense: Double = transactions.filter { it.category?.type == TransactionAmountType.EXPENSE.ordinal }.sumOf { it.quick.price.toDouble() }
) {
    /**
     * 日期格式化字符串（月/日格式）
     */
    val dateFormat: String = convertMillisToDate(date, format = DATE_FORMAT_MD)
}

/**
 * 分组数据列表（按月分组）
 * 用于按年月对交易记录进行分组显示
 *
 * @property year 年份
 * @property month 月份
 * @property groups 该月下的所有日期分组
 * @property monthTotalIncome 该月总收入（计算属性）
 * @property monthTotalExpense 该月总支出（计算属性）
 */
data class TransactionGroupedData(
    val year: Int, val month: Int, val groups: List<TransactionDateGroup>,

    // 计算每月的汇总信息
    val monthTotalIncome: Double = groups.sumOf { it.totalIncome }, val monthTotalExpense: Double = groups.sumOf { it.totalExpense }
)