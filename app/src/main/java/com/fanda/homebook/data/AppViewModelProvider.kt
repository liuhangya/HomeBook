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
import com.fanda.homebook.quick.viewmodel.AddQuickViewModel
import com.fanda.homebook.quick.viewmodel.WatchAndEditQuickViewModel
import com.fanda.homebook.stock.viewmodel.AddStockViewModel
import com.fanda.homebook.stock.viewmodel.StockHomeViewModel
import com.fanda.homebook.stock.viewmodel.WatchAndEditStockViewModel

/**
 * 应用ViewModel提供器
 * 集中管理所有ViewModel的工厂实例
 */
object AppViewModelProvider {
    /**
     * ViewModel工厂实例
     * 使用viewModelFactory DSL配置所有ViewModel的初始化
     */
    val factory = viewModelFactory {
        // ============ 衣橱模块 ViewModels ============

        // 衣橱首页ViewModel
        initializer {
            HomeClosetViewModel(
                homeBookApplication().appContainer.closetRepository, homeBookApplication().appContainer.ownerRepository
            )
        }

        // 添加衣橱物品ViewModel
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

        // 查看和编辑衣橱物品ViewModel
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

        // 衣橱分类ViewModel
        initializer {
            CategoryClosetViewModel(
                this.createSavedStateHandle(), homeBookApplication().appContainer.closetRepository
            )
        }

        // 衣橱分类详情ViewModel
        initializer {
            CategoryDetailClosetViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.closetRepository,
                homeBookApplication().appContainer.categoryRepository,
                homeBookApplication().appContainer.colorTypeRepository,
                homeBookApplication().appContainer.seasonRepository
            )
        }

        // ============ 囤货模块 ViewModels ============

        // 库存首页ViewModel
        initializer {
            StockHomeViewModel(
                homeBookApplication().appContainer.rackRepository, homeBookApplication().appContainer.stockRepository
            )
        }

        // 添加库存物品ViewModel
        initializer {
            AddStockViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.stockRepository,
                homeBookApplication().appContainer.rackRepository,
                homeBookApplication().appContainer.productRepository,
                homeBookApplication().appContainer.periodRepository,
                homeBookApplication().appContainer.transactionRepository,
                homeBookApplication().appContainer.quickRepository
            )
        }

        // 查看和编辑库存物品ViewModel
        initializer {
            WatchAndEditStockViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.stockRepository,
                homeBookApplication().appContainer.rackRepository,
                homeBookApplication().appContainer.productRepository,
                homeBookApplication().appContainer.periodRepository
            )
        }

        // ============ 账本模块 ViewModels ============

        // 账本首页ViewModel
        initializer {
            BookViewModel(
                homeBookApplication().appContainer.bookRepository,
                homeBookApplication().appContainer.transactionRepository,
                homeBookApplication().appContainer.quickRepository,
                homeBookApplication().appContainer.planRepository
            )
        }

        // ============ 快捷操作模块 ViewModels ============

        // 添加快捷操作ViewModel
        initializer {
            AddQuickViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.transactionRepository,
                homeBookApplication().appContainer.payWayRepository,
                homeBookApplication().appContainer.quickRepository
            )
        }

        // 查看和编辑快捷操作ViewModel
        initializer {
            WatchAndEditQuickViewModel(
                this.createSavedStateHandle(),
                homeBookApplication().appContainer.transactionRepository,
                homeBookApplication().appContainer.payWayRepository,
                homeBookApplication().appContainer.quickRepository
            )
        }

        // ============ 仪表盘模块 ViewModels ============

        // 仪表盘ViewModel
        initializer {
            DashboardViewModel(
                this.createSavedStateHandle(), homeBookApplication().appContainer.quickRepository
            )
        }

        // 仪表盘详情ViewModel
        initializer {
            DashboardDetailViewModel(
                this.createSavedStateHandle(), homeBookApplication().appContainer.quickRepository
            )
        }

        // ============ 通用设置模块 ViewModels ============

        // 编辑交易分类ViewModel
        initializer {
            EditTransactionCategoryViewModel(
                homeBookApplication().appContainer.transactionRepository
            )
        }

        // 产品管理ViewModel
        initializer {
            ProductViewModel(
                homeBookApplication().appContainer.productRepository
            )
        }

        // 支付方式管理ViewModel
        initializer {
            PayWayViewModel(
                homeBookApplication().appContainer.payWayRepository
            )
        }

        // 尺码管理ViewModel
        initializer {
            SizeViewModel(
                homeBookApplication().appContainer.sizeRepository
            )
        }

        // 分类管理ViewModel
        initializer {
            CategoryViewModel(
                homeBookApplication().appContainer.categoryRepository
            )
        }

        // 子分类管理ViewModel
        initializer {
            SubCategoryViewModel(
                this.createSavedStateHandle(), homeBookApplication().appContainer.categoryRepository
            )
        }

        // 颜色类型管理ViewModel
        initializer {
            ColorTypeViewModel(
                this.createSavedStateHandle(), homeBookApplication().appContainer.colorTypeRepository
            )
        }
    }
}

/**
 * CreationExtras扩展函数
 * 获取HomeBookApplication实例
 *
 * @return HomeBookApplication 应用实例
 */
fun CreationExtras.homeBookApplication(): HomeBookApplication = (this[AndroidViewModelFactory.APPLICATION_KEY] as HomeBookApplication)