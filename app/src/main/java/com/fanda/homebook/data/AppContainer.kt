package com.fanda.homebook.data

import android.content.Context
import com.fanda.homebook.data.book.BookRepository
import com.fanda.homebook.data.book.LocalBookRepository
import com.fanda.homebook.data.category.CategoryRepository
import com.fanda.homebook.data.category.LocalCategoryRepository
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.closet.LocalClosetRepository
import com.fanda.homebook.data.color.ColorTypeRepository
import com.fanda.homebook.data.color.LocalColorTypeRepository
import com.fanda.homebook.data.owner.LocalOwnerRepository
import com.fanda.homebook.data.owner.OwnerRepository
import com.fanda.homebook.data.pay.LocalPayWayRepository
import com.fanda.homebook.data.pay.PayWayRepository
import com.fanda.homebook.data.period.LocalPeriodRepository
import com.fanda.homebook.data.period.PeriodRepository
import com.fanda.homebook.data.product.LocalProductRepository
import com.fanda.homebook.data.product.ProductRepository
import com.fanda.homebook.data.quick.LocalQuickRepository
import com.fanda.homebook.data.quick.QuickRepository
import com.fanda.homebook.data.rack.LocalRackRepository
import com.fanda.homebook.data.rack.RackRepository
import com.fanda.homebook.data.season.LocalSeasonRepository
import com.fanda.homebook.data.season.SeasonRepository
import com.fanda.homebook.data.size.LocalSizeRepository
import com.fanda.homebook.data.size.SizeRepository
import com.fanda.homebook.data.stock.LocalStockRepository
import com.fanda.homebook.data.stock.StockRepository
import com.fanda.homebook.data.transaction.LocalTransactionRepository
import com.fanda.homebook.data.transaction.TransactionRepository

/**
 * 应用依赖注入容器接口
 * 定义应用中所有仓库的依赖关系
 */
interface AppContainer {
    /** 颜色类型数据仓库 */
    val colorTypeRepository: ColorTypeRepository

    /** 衣橱数据仓库 */
    val closetRepository: ClosetRepository

    /** 季节数据仓库 */
    val seasonRepository: SeasonRepository

    /** 产品数据仓库 */
    val productRepository: ProductRepository

    /** 尺码数据仓库 */
    val sizeRepository: SizeRepository

    /** 所有者数据仓库 */
    val ownerRepository: OwnerRepository

    /** 分类数据仓库 */
    val categoryRepository: CategoryRepository

    /** 货架数据仓库 */
    val rackRepository: RackRepository

    /** 期间数据仓库 */
    val periodRepository: PeriodRepository

    /** 库存数据仓库 */
    val stockRepository: StockRepository

    /** 交易数据仓库 */
    val transactionRepository: TransactionRepository

    /** 支付方式数据仓库 */
    val payWayRepository: PayWayRepository

    /** 快捷操作数据仓库 */
    val quickRepository: QuickRepository

    /** 账本数据仓库 */
    val bookRepository: BookRepository
}

/**
 * 应用依赖注入容器实现类
 * 负责初始化和管理所有数据仓库实例
 *
 * @property context 应用上下文
 */
class AppContainerImpl(private val context: Context) : AppContainer {
    /**
     * 颜色类型数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val colorTypeRepository: ColorTypeRepository by lazy {
        LocalColorTypeRepository(HomeBookDatabase.getDatabase(context).colorTypeDao())
    }

    /**
     * 衣橱数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val closetRepository: ClosetRepository by lazy {
        LocalClosetRepository(HomeBookDatabase.getDatabase(context).closetDao())
    }

    /**
     * 季节数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val seasonRepository: SeasonRepository by lazy {
        LocalSeasonRepository(HomeBookDatabase.getDatabase(context).seasonDao())
    }

    /**
     * 产品数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val productRepository: ProductRepository by lazy {
        LocalProductRepository(HomeBookDatabase.getDatabase(context).productDao())
    }

    /**
     * 尺码数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val sizeRepository: SizeRepository by lazy {
        LocalSizeRepository(HomeBookDatabase.getDatabase(context).sizeDao())
    }

    /**
     * 所有者数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val ownerRepository: OwnerRepository by lazy {
        LocalOwnerRepository(HomeBookDatabase.getDatabase(context).ownerDao())
    }

    /**
     * 分类数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val categoryRepository: CategoryRepository by lazy {
        LocalCategoryRepository(HomeBookDatabase.getDatabase(context).categoryDao())
    }

    /**
     * 货架数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val rackRepository: RackRepository by lazy {
        LocalRackRepository(HomeBookDatabase.getDatabase(context).rackDao())
    }

    /**
     * 期间数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val periodRepository: PeriodRepository by lazy {
        LocalPeriodRepository(HomeBookDatabase.getDatabase(context).periodDao())
    }

    /**
     * 库存数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val stockRepository: StockRepository by lazy {
        LocalStockRepository(HomeBookDatabase.getDatabase(context).stockDao())
    }

    /**
     * 交易数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val transactionRepository: TransactionRepository by lazy {
        LocalTransactionRepository(HomeBookDatabase.getDatabase(context).transactionDao())
    }

    /**
     * 支付方式数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val payWayRepository: PayWayRepository by lazy {
        LocalPayWayRepository(HomeBookDatabase.getDatabase(context).payWayDao())
    }

    /**
     * 快捷操作数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val quickRepository: QuickRepository by lazy {
        LocalQuickRepository(HomeBookDatabase.getDatabase(context).quickDao())
    }

    /**
     * 账本数据仓库实例（懒加载）
     * 当首次访问时初始化
     */
    override val bookRepository: BookRepository by lazy {
        LocalBookRepository(HomeBookDatabase.getDatabase(context).bookDao())
    }
}