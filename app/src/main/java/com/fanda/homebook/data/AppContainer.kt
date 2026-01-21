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
import com.fanda.homebook.data.product.LocalProductRepository
import com.fanda.homebook.data.product.ProductRepository
import com.fanda.homebook.data.season.LocalSeasonRepository
import com.fanda.homebook.data.season.SeasonRepository
import com.fanda.homebook.data.size.LocalSizeRepository
import com.fanda.homebook.data.size.SizeRepository

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

}