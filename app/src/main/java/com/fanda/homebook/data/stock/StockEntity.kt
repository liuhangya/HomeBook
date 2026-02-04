package com.fanda.homebook.data.stock

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fanda.homebook.data.period.PeriodEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.tools.DATE_FORMAT_YMD
import com.fanda.homebook.tools.calculateExpireDateFromOpenDate
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.tools.formatExpireTimeDetailed

/*
* 不是数据库中的实体对象，是关联查询后组装的数据对象
* */
data class AddStockEntity(
    @Embedded val stock: StockEntity,

    @Relation(
        parentColumn = "rackId", entityColumn = "id",
    ) val rackEntity: RackEntity?,

    @Relation(
        parentColumn = "subCategoryId", entityColumn = "id"
    ) val subCategoryEntity: RackSubCategoryEntity?,

    @Relation(
        parentColumn = "productId", entityColumn = "id"
    ) val product: ProductEntity?,

    @Relation(
        parentColumn = "periodId", entityColumn = "id"
    ) val period: PeriodEntity?,
)

fun StockEntity.visibleExpireTime() = expireDate > 0 || usedDate > 0 || (openDate > 0 && shelfMonth > 0)

fun StockEntity.getStockDes(): String = if (useStatus == StockUseStatus.USED.code) {
    when {
        usedDate == -1L -> ""
        else -> "${convertMillisToDate(usedDate, DATE_FORMAT_YMD)}用完"
    }
} else {
    if (openDate > 0 && shelfMonth > 0) {
        // 优先根据开封日期+ shelfMonth计算到期时间
        formatExpireTimeDetailed(calculateExpireDateFromOpenDate(openDate, shelfMonth))
    } else {
        formatExpireTimeDetailed(expireDate)
    }
}

@Entity(
    tableName = "stock", foreignKeys = [ForeignKey(
        entity = RackEntity::class, parentColumns = ["id"], childColumns = ["rackId"], onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = RackSubCategoryEntity::class, parentColumns = ["id"], childColumns = ["subCategoryId"], onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = PeriodEntity::class, parentColumns = ["id"], childColumns = ["periodId"], onDelete = ForeignKey.SET_NULL
    )], indices = [Index(value = ["rackId"]), Index(value = ["subCategoryId"]), Index(value = ["productId"]), Index(value = ["periodId"])]
) data class StockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val rackId: Int = 0,
    val subCategoryId: Int = 0,
    val productId: Int? = null,
    val periodId: Int? = null,
    val syncBook: Boolean = false,
    val comment: String = "",
    val remain: String = "",
    val feel: String = "",
    val price: String = "",
    val imageLocalPath: String = "",
    val buyDate: Long = -1,
    val openDate: Long = -1,
    val expireDate: Long = -1,
    val usedDate: Long = -1,
    val createDate: Long = System.currentTimeMillis(),
    val shelfMonth: Int = 0,
    val useStatus: Int = StockUseStatus.NO_USE.code,
)

enum class StockUseStatus(val code: Int) {
    ALL(-1), NO_USE(0), USING(1), USED(2),
}

data class StockStatusCounts(
    val allCount: Int, val noUseCount: Int, val usingCount: Int, val usedCount: Int
)

data class StockStatusEntity(
    val useStatus: StockUseStatus, val name: String, val count: Int
)

data class StockMenuEntity(val name: String, val type: ShowBottomSheetType)