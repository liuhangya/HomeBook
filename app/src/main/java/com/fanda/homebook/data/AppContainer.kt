package com.fanda.homebook.data

import android.content.Context
import com.fanda.homebook.data.category.CategoryRepository
import com.fanda.homebook.data.category.LocalCategoryRepository
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.closet.LocalClosetRepository
import com.fanda.homebook.data.color.ColorTypeRepository
import com.fanda.homebook.data.color.LocalColorTypeRepository
import com.fanda.homebook.data.owner.LocalOwnerRepository
import com.fanda.homebook.data.owner.OwnerRepository
import com.fanda.homebook.data.period.LocalPeriodRepository
import com.fanda.homebook.data.period.PeriodRepository
import com.fanda.homebook.data.product.LocalProductRepository
import com.fanda.homebook.data.product.ProductRepository
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

/*
* 依赖注入容器类
* */
interface AppContainer {
    val colorTypeRepository: ColorTypeRepository
    val closetRepository: ClosetRepository
    val seasonRepository: SeasonRepository
    val productRepository: ProductRepository
    val sizeRepository: SizeRepository
    val ownerRepository: OwnerRepository
    val categoryRepository: CategoryRepository
    val rackRepository: RackRepository
    val periodRepository: PeriodRepository
    val stockRepository: StockRepository
    val transactionRepository: TransactionRepository
}

class AppContainerImpl(private val context: Context) : AppContainer {
    override val colorTypeRepository: ColorTypeRepository by lazy {
        LocalColorTypeRepository(HomeBookDatabase.getDatabase(context).colorTypeDao())
    }
    override val closetRepository: ClosetRepository by lazy {
        LocalClosetRepository(HomeBookDatabase.getDatabase(context).closetDao())
    }
    override val seasonRepository: SeasonRepository by lazy {
        LocalSeasonRepository(HomeBookDatabase.getDatabase(context).seasonDao())
    }

    override val productRepository: ProductRepository by lazy {
        LocalProductRepository(HomeBookDatabase.getDatabase(context).productDao())
    }
    override val sizeRepository: SizeRepository by lazy {
        LocalSizeRepository(HomeBookDatabase.getDatabase(context).sizeDao())
    }

    override val ownerRepository: OwnerRepository by lazy {
        LocalOwnerRepository(HomeBookDatabase.getDatabase(context).ownerDao())
    }

    override val categoryRepository: CategoryRepository by lazy {
        LocalCategoryRepository(HomeBookDatabase.getDatabase(context).categoryDao())
    }

    override val rackRepository: RackRepository by lazy {
        LocalRackRepository(HomeBookDatabase.getDatabase(context).rackDao())
    }

    override val periodRepository: PeriodRepository by lazy {
        LocalPeriodRepository(HomeBookDatabase.getDatabase(context).periodDao())
    }

    override val stockRepository: StockRepository by lazy {
        LocalStockRepository(HomeBookDatabase.getDatabase(context).stockDao())

    }
    override val transactionRepository: TransactionRepository by lazy {
        LocalTransactionRepository(HomeBookDatabase.getDatabase(context).transactionDao())

    }

}