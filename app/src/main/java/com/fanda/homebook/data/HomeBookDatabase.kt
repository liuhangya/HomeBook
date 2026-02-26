package com.fanda.homebook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fanda.homebook.data.book.BookDao
import com.fanda.homebook.data.book.BookEntity
import com.fanda.homebook.data.category.CategoryDao
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.data.closet.ClosetDao
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.data.color.ColorTypeDao
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.owner.OwnerDao
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.pay.PayWayDao
import com.fanda.homebook.data.pay.PayWayEntity
import com.fanda.homebook.data.period.PeriodDao
import com.fanda.homebook.data.period.PeriodEntity
import com.fanda.homebook.data.plan.PlanDao
import com.fanda.homebook.data.plan.PlanEntity
import com.fanda.homebook.data.product.ProductDao
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.quick.QuickDao
import com.fanda.homebook.data.quick.QuickEntity
import com.fanda.homebook.data.rack.RackDao
import com.fanda.homebook.data.rack.RackEntity
import com.fanda.homebook.data.rack.RackSubCategoryEntity
import com.fanda.homebook.data.season.ClosetSeasonRelation
import com.fanda.homebook.data.season.SeasonDao
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeDao
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.data.stock.StockDao
import com.fanda.homebook.data.stock.StockEntity
import com.fanda.homebook.data.transaction.TransactionDao
import com.fanda.homebook.data.transaction.TransactionEntity
import com.fanda.homebook.data.transaction.TransactionSubEntity

/**
 * HomeBook应用数据库
 * 使用Room持久化框架管理应用数据
 *
 * @param entities 包含所有数据实体类
 * @param version 数据库版本号，升级时递增
 * @param exportSchema 是否导出数据库模式信息
 */
@Database(
    entities = [ColorTypeEntity::class,         // 颜色类型实体
        ClosetEntity::class,           // 衣橱实体
        SeasonEntity::class,           // 季节实体
        ProductEntity::class,          // 产品实体
        SizeEntity::class,             // 尺码实体
        OwnerEntity::class,            // 所有者实体
        CategoryEntity::class,         // 分类实体
        SubCategoryEntity::class,      // 子分类实体
        RackEntity::class,             // 货架实体
        RackSubCategoryEntity::class,  // 货架子分类实体
        PeriodEntity::class,           // 期间实体
        StockEntity::class,            // 库存实体
        ClosetSeasonRelation::class,   // 衣橱-季节关联实体
        TransactionEntity::class,      // 交易实体
        TransactionSubEntity::class,   // 交易子项实体
        PayWayEntity::class,           // 支付方式实体
        QuickEntity::class,            // 快捷操作实体
        BookEntity::class,              // 账本实体
        PlanEntity::class               // 计划金额实体
    ], version = 18,                     // 当前数据库版本号
    exportSchema = false              // 不导出数据库模式信息
) abstract class HomeBookDatabase : RoomDatabase() {

    // 颜色类型数据访问对象
    abstract fun colorTypeDao(): ColorTypeDao

    // 衣橱数据访问对象
    abstract fun closetDao(): ClosetDao

    // 季节数据访问对象
    abstract fun seasonDao(): SeasonDao

    // 产品数据访问对象
    abstract fun productDao(): ProductDao

    // 尺码数据访问对象
    abstract fun sizeDao(): SizeDao

    // 所有者数据访问对象
    abstract fun ownerDao(): OwnerDao

    // 分类数据访问对象
    abstract fun categoryDao(): CategoryDao

    // 货架数据访问对象
    abstract fun rackDao(): RackDao

    // 期间数据访问对象
    abstract fun periodDao(): PeriodDao

    // 库存数据访问对象
    abstract fun stockDao(): StockDao

    // 交易数据访问对象
    abstract fun transactionDao(): TransactionDao

    // 支付方式数据访问对象
    abstract fun payWayDao(): PayWayDao

    // 快捷操作数据访问对象
    abstract fun quickDao(): QuickDao

    // 账本数据访问对象
    abstract fun bookDao(): BookDao

    // 计划金额数据访问对象
    abstract fun planDao(): PlanDao

    companion object {
        /**
         * 数据库实例，使用volatile确保多线程可见性
         */
        @Volatile private var Instance: HomeBookDatabase? = null

        /**
         * 获取数据库实例（单例模式）
         *
         * @param context 应用上下文
         * @return HomeBookDatabase实例
         */
        fun getDatabase(context: Context): HomeBookDatabase {
            return Instance ?: synchronized(this) {
                // 构建数据库实例
                Room.databaseBuilder(
                    context.applicationContext, HomeBookDatabase::class.java, "HomeBookDatabase"  // 数据库文件名
                ).addMigrations(MIGRATION_17_18).build().also { Instance = it }  // 设置单例实例
            }
        }

        // 增加预算表功能
         private val MIGRATION_17_18 = object : Migration(17, 18) {
             override fun migrate(db: SupportSQLiteDatabase) {
                 // 创建新表
                 db.execSQL(
                     """
                     CREATE TABLE IF NOT EXISTS `plan_amount` (
                         `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                         `bookId` INTEGER NOT NULL,
                         `amount` REAL NOT NULL,
                         `year` INTEGER NOT NULL,
                         `month` INTEGER NOT NULL
                     )
                     """.trimIndent()
                 )
             }
         }
    }
}