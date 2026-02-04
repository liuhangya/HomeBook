package com.fanda.homebook.data

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fanda.homebook.HomeBookApplication
import com.fanda.homebook.book.viewmodel.BookViewModel
import com.fanda.homebook.book.viewmodel.DashboardDetailViewModel
import com.fanda.homebook.book.viewmodel.DashboardViewModel
import com.fanda.homebook.book.viewmodel.EditTransactionCategoryViewModel
import com.fanda.homebook.closet.viewmodel.AddClosetViewModel
import com.fanda.homebook.closet.viewmodel.CategoryClosetViewModel
import com.fanda.homebook.closet.viewmodel.CategoryDetailClosetViewModel
import com.fanda.homebook.closet.viewmodel.HomeClosetViewModel
import com.fanda.homebook.closet.viewmodel.WatchAndEditClosetViewModel
import com.fanda.homebook.common.viewmodel.CategoryViewModel
import com.fanda.homebook.common.viewmodel.ColorTypeViewModel
import com.fanda.homebook.common.viewmodel.PayWayViewModel
import com.fanda.homebook.common.viewmodel.ProductViewModel
import com.fanda.homebook.common.viewmodel.SizeViewModel
import com.fanda.homebook.common.viewmodel.SubCategoryViewModel
import com.fanda.homebook.data.category.CategoryRepository
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.color.ColorTypeRepository
import com.fanda.homebook.data.owner.OwnerRepository
import com.fanda.homebook.data.pay.PayWayRepository
import com.fanda.homebook.data.period.PeriodRepository
import com.fanda.homebook.data.product.ProductRepository
import com.fanda.homebook.data.rack.RackRepository
import com.fanda.homebook.data.season.SeasonRepository
import com.fanda.homebook.data.size.SizeRepository
import com.fanda.homebook.data.stock.StockRepository
import com.fanda.homebook.quick.viewmodel.AddQuickViewModel
import com.fanda.homebook.stock.viewmodel.AddStockViewModel
import com.fanda.homebook.stock.viewmodel.StockHomeViewModel
import com.fanda.homebook.stock.viewmodel.WatchAndEditStockViewModel

/*
* 提供所有的 ViewModel 工厂
* */
object AppViewModelProvider {
    val factory = viewModelFactory {
        initializer {
            ColorTypeViewModel(
                this.createSavedStateHandle(), homeBookApplication().appContainer.colorTypeRepository
            )
        }
        initializer {
            AddClosetViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.colorTypeRepository,
                homeBookApplication().appContainer.closetRepository,
                homeBookApplication().appContainer.seasonRepository,
                homeBookApplication().appContainer.productRepository,
                homeBookApplication().appContainer.sizeRepository,
                homeBookApplication().appContainer.ownerRepository,
                homeBookApplication().appContainer.categoryRepository,
                homeBookApplication().appContainer.quickRepository,
                homeBookApplication().appContainer.transactionRepository
            )
        }
        initializer {
            WatchAndEditClosetViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.colorTypeRepository,
                homeBookApplication().appContainer.closetRepository,
                homeBookApplication().appContainer.seasonRepository,
                homeBookApplication().appContainer.productRepository,
                homeBookApplication().appContainer.sizeRepository,
                homeBookApplication().appContainer.ownerRepository,
                homeBookApplication().appContainer.categoryRepository
            )
        }
        initializer {
            AddQuickViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.transactionRepository,
                homeBookApplication().appContainer.payWayRepository,
                homeBookApplication().appContainer.quickRepository
            )
        }
        initializer {
            HomeClosetViewModel(
                homeBookApplication().appContainer.closetRepository,
                homeBookApplication().appContainer.ownerRepository,
            )
        }
        initializer {
            DashboardDetailViewModel(
                this.createSavedStateHandle(),
            )
        }
        initializer {
            BookViewModel(
                homeBookApplication().appContainer.bookRepository, homeBookApplication().appContainer.transactionRepository, homeBookApplication().appContainer.quickRepository
            )
        }
        initializer {
            AddStockViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.stockRepository,
                homeBookApplication().appContainer.rackRepository,
                homeBookApplication().appContainer.productRepository,
                homeBookApplication().appContainer.periodRepository,
                homeBookApplication().appContainer.transactionRepository,
                homeBookApplication().appContainer.quickRepository,

                )
        }
        initializer {
            WatchAndEditStockViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.stockRepository,
                homeBookApplication().appContainer.rackRepository,
                homeBookApplication().appContainer.productRepository,
                homeBookApplication().appContainer.periodRepository
            )
        }
        initializer {
            CategoryClosetViewModel(this.createSavedStateHandle(), homeBookApplication().appContainer.closetRepository)
        }
        initializer {
            DashboardViewModel(
                this.createSavedStateHandle(), homeBookApplication().appContainer.quickRepository
            )
        }
        initializer {
            EditTransactionCategoryViewModel(homeBookApplication().appContainer.transactionRepository)
        }
        initializer {
            ProductViewModel(homeBookApplication().appContainer.productRepository)
        }
        initializer {
            PayWayViewModel(homeBookApplication().appContainer.payWayRepository)
        }
        initializer {
            SizeViewModel(homeBookApplication().appContainer.sizeRepository)
        }
        initializer {
            CategoryViewModel(homeBookApplication().appContainer.categoryRepository)
        }
        initializer {
            SubCategoryViewModel(this.createSavedStateHandle(), homeBookApplication().appContainer.categoryRepository)
        }
        initializer {
            CategoryDetailClosetViewModel(
                this.createSavedStateHandle(), homeBookApplication().appContainer.closetRepository, homeBookApplication().appContainer.categoryRepository
            )
        }

        initializer {
            StockHomeViewModel(homeBookApplication().appContainer.rackRepository, homeBookApplication().appContainer.stockRepository)
        }
    }
}

fun CreationExtras.homeBookApplication(): HomeBookApplication = (this[AndroidViewModelFactory.APPLICATION_KEY] as HomeBookApplication)
