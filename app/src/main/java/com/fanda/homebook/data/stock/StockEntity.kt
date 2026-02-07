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
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.tools.DATE_FORMAT_YMD
import com.fanda.homebook.tools.calculateExpireDateFromOpenDate
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.tools.formatExpireTimeDetailed

/*
* 关联查询组合对象 - 不是数据库中的实体对象，是关联查询后组装的数据对象
* 包含库存物品的所有关联信息：货架、子分类、产品、使用期限等
*/
data class AddStockEntity(
    // 基础库存实体
    @Embedded val stock: StockEntity,

    // 货架信息（可空：当货架被删除时为空）
    @Relation(
        parentColumn = "rackId", entityColumn = "id"
    ) val rackEntity: RackEntity?,

    // 货架子分类信息（可空：当子分类被删除时为空）
    @Relation(
        parentColumn = "subCategoryId", entityColumn = "id"
    ) val subCategoryEntity: RackSubCategoryEntity?,

    // 产品信息（可空：当产品被删除或未设置时为空）
    @Relation(
        parentColumn = "productId", entityColumn = "id"
    ) val product: ProductEntity?,

    // 使用期限信息（可空：当使用期限被删除或未设置时为空）
    @Relation(
        parentColumn = "periodId", entityColumn = "id"
    ) val period: PeriodEntity?,
)

/**
 * 扩展函数：检查库存是否显示过期时间
 *
 * @return 布尔值，true表示需要显示过期时间
 */
fun StockEntity.visibleExpireTime() = expireDate > 0 || usedDate > 0 || (openDate > 0 && shelfMonth > 0)

/**
 * 扩展函数：获取库存描述信息
 * 根据库存状态返回不同的描述文本
 *
 * @return 描述字符串
 */
fun StockEntity.getStockDes(): String = if (useStatus == StockUseStatus.USED.code) {
    // 已用完状态：显示用完日期
    when {
        usedDate == -1L -> ""  // 无使用日期，返回空字符串
        else -> "${convertMillisToDate(usedDate, DATE_FORMAT_YMD)}用完"
    }
} else {
    // 未用完状态：显示过期时间
    if (openDate > 0 && shelfMonth > 0) {
        // 优先根据开封日期+ shelfMonth计算到期时间
        formatExpireTimeDetailed(calculateExpireDateFromOpenDate(openDate, shelfMonth))
    } else {
        formatExpireTimeDetailed(expireDate)
    }
}

/**
 * 库存实体类
 * 对应数据库中的stock表，用于存储库存物品的详细信息
 * 包含多个外键关联到其他表（货架、子分类、产品、使用期限等）
 */
@Entity(
    tableName = "stock", foreignKeys = [
        // 关联货架表：当货架被删除时，外键设为NULL
        ForeignKey(
            entity = RackEntity::class, parentColumns = ["id"], childColumns = ["rackId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联货架子分类表：当子分类被删除时，外键设为NULL
        ForeignKey(
            entity = RackSubCategoryEntity::class, parentColumns = ["id"], childColumns = ["subCategoryId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联产品表：当产品被删除时，外键设为NULL
        ForeignKey(
            entity = ProductEntity::class, parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.SET_NULL
        ),
        // 关联使用期限表：当使用期限被删除时，外键设为NULL
        ForeignKey(
            entity = PeriodEntity::class, parentColumns = ["id"], childColumns = ["periodId"], onDelete = ForeignKey.SET_NULL
        )], indices = [
        // 为所有外键字段创建索引，提高查询性能
        Index(value = ["rackId"]), Index(value = ["subCategoryId"]), Index(value = ["productId"]), Index(value = ["periodId"])]
) data class StockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 基本字段
    val name: String = "",                         // 物品名称
    val rackId: Int = 0,                           // 货架ID
    val subCategoryId: Int = 0,                    // 货架子分类ID

    // 关联字段
    val productId: Int? = null,                    // 产品ID（可选）
    val periodId: Int? = null,                     // 使用期限ID（可选）

    // 状态和同步字段
    val syncBook: Boolean = false,                 // 是否同步到账本

    // 描述字段
    val comment: String = "",                      // 备注
    val remain: String = "",                       // 剩余量描述
    val feel: String = "",                         // 使用感受
    val price: String = "",                        // 价格
    val imageLocalPath: String = "",               // 图片本地路径

    // 日期字段
    val buyDate: Long = -1,                        // 购买日期（-1表示未设置）
    val openDate: Long = -1,                       // 开封日期（-1表示未设置）
    val expireDate: Long = -1,                     // 过期日期（-1表示未设置）
    val usedDate: Long = -1,                       // 用完日期（-1表示未设置）
    val createDate: Long = System.currentTimeMillis(), // 创建日期

    // 有效期字段
    val shelfMonth: Int = 0,                       // 开封后可使用月数

    // 状态字段
    val useStatus: Int = StockUseStatus.NO_USE.code // 使用状态
)

/**
 * 库存使用状态枚举
 * 定义库存物品的不同使用状态
 */
enum class StockUseStatus(val code: Int) {
    ALL(-1),       // 所有状态（查询用）
    NO_USE(0),     // 未使用
    USING(1),      // 使用中
    USED(2),       // 已用完
}

/**
 * 库存状态统计信息
 * 用于统计各种使用状态的数量
 *
 * @property allCount 总数量
 * @property noUseCount 未使用数量
 * @property usingCount 使用中数量
 * @property usedCount 已用完数量
 */
data class StockStatusCounts(
    val allCount: Int, val noUseCount: Int, val usingCount: Int, val usedCount: Int
)

/**
 * 库存状态实体
 * 用于显示库存状态的信息
 *
 * @property useStatus 使用状态
 * @property name 状态名称
 * @property count 该状态下的物品数量
 */
data class StockStatusEntity(
    val useStatus: StockUseStatus, val name: String, val count: Int
)

/**
 * 库存菜单项实体
 * 用于库存管理页面中的操作菜单
 *
 * @property name 菜单项名称
 * @property type 对应的底部弹窗类型
 */
data class StockMenuEntity(
    val name: String, val type: ShowBottomSheetType
)