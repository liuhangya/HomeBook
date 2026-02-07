package com.fanda.homebook.data.transaction

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.fanda.homebook.data.category.CategoryEntity

/**
 * 交易主分类实体类
 * 对应数据库中的transaction_category表，用于存储交易的一级分类信息
 *
 * @property id 主键ID，自动生成
 * @property name 分类名称，如"餐饮"、"交通"、"购物"等
 * @property type 分类类型，对应TransactionType枚举值
 */
@Entity(tableName = "transaction_category") data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 分类名称
    val name: String,

    // 分类类型
    val type: Int
)

/**
 * 交易子分类实体类
 * 对应数据库中的transaction_sub_category表，用于存储交易的二级分类信息
 * 包含子分类的ID、名称、排序序号、所属分类的ID和分类类型
 * 删除规则是父分类被删除时，所有子类也被删除
 *
 * @property id 主键ID，自动生成
 * @property name 子分类名称，如"早餐"、"午餐"、"晚餐"（属于餐饮分类）
 * @property sortOrder 排序序号，默认值为0
 * @property categoryId 所属主分类的ID，外键关联到TransactionEntity表的id字段
 * @property type 分类类型，对应TransactionType枚举值，默认值为0
 */
@Entity(
    tableName = "transaction_sub_category", foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,  // 注意：这里可能应该是TransactionEntity，根据实际业务逻辑确认
        parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE // 级联删除：当主分类被删除时，相关子分类自动删除
    )], indices = [
        // 为categoryId字段创建索引，提高查询效率
        Index(value = ["categoryId"]),
    ]
) data class TransactionSubEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 子分类名称
    val name: String,

    // 排序序号，默认为0，数值越小排序越靠前
    val sortOrder: Int = 0,

    // 所属主分类的ID
    val categoryId: Int,

    // 分类类型
    val type: Int = 0
)

/**
 * 交易分类和子分类的组合实体类
 * 用于数据库查询交易分类和子分类信息
 * 包含交易分类实体和交易子分类列表
 *
 * @property category 交易主分类实体
 * @property unsortedSubCategories 未排序的交易子分类列表（直接从数据库查询的结果）
 * @property subCategories 计算属性，返回按sortOrder排序后的交易子分类列表
 */
class TransactionWithSubCategories {
    @Embedded lateinit var category: TransactionEntity

    @Relation(
        parentColumn = "id",                 // 交易主分类的ID字段
        entityColumn = "categoryId",         // 交易子分类的外键字段
        entity = TransactionSubEntity::class // 关联的实体类
    ) var unsortedSubCategories: List<TransactionSubEntity> = emptyList()

    /**
     * 计算属性，返回排序后的交易子分类列表
     * 通过sortOrder字段升序排列
     */
    val subCategories: List<TransactionSubEntity>
        get() = unsortedSubCategories.sortedBy { it.sortOrder }
}

/**
 * 交易类型枚举
 * 定义不同类型的交易分类，用于区分不同的消费或收入类别
 * 每个枚举值对应一个特定的交易类型代码
 */
enum class TransactionType(val type: Int) {
    CUSTOM(-1),       // 自定义分类
    DINING(0),        // 餐饮
    TRAFFIC(1),       // 交通
    CLOTHING(2),      // 服饰
    SKINCARE(3),      // 护肤/化妆品
    SHOPPING(4),      // 购物
    SERVICES(5),      // 服务
    HEALTH(6),        // 健康/医疗
    PLAY(7),          // 娱乐
    DAILY(8),         // 日常用品
    TRAVEL(9),        // 旅行
    INSURANCE(10),    // 保险
    RED_ENVELOPE(11), // 发红包
    SOCIAL(12),       // 社交
    SALARY(13),       // 工资
    GET_ENVELOPE(14), // 收红包
    BONUS(15),        // 奖金
    FINANCE(16),      // 理财
    DEBTS(17),        // 借贷
    OTHERS(18),       // 其他
    ADD(1000)         // 添加（特殊用途）
}