package com.fanda.homebook.data

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fanda.homebook.HomeBookApplication
import com.fanda.homebook.closet.viewmodel.AddClosetViewModel
import com.fanda.homebook.common.viewmodel.CategoryViewModel
import com.fanda.homebook.common.viewmodel.ColorTypeViewModel
import com.fanda.homebook.common.viewmodel.ProductViewModel
import com.fanda.homebook.common.viewmodel.SizeViewModel
import com.fanda.homebook.common.viewmodel.SubCategoryViewModel

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
                homeBookApplication().appContainer.categoryRepository
            )
        }
        initializer {
            ProductViewModel(homeBookApplication().appContainer.productRepository)
        }
        initializer {
            SizeViewModel(homeBookApplication().appContainer.sizeRepository)
        }
        initializer {
            CategoryViewModel(homeBookApplication().appContainer.categoryRepository)
        }
        initializer {
            SubCategoryViewModel(this.createSavedStateHandle(),homeBookApplication().appContainer.categoryRepository)
        }
    }
}

fun CreationExtras.homeBookApplication(): HomeBookApplication = (this[AndroidViewModelFactory.APPLICATION_KEY] as HomeBookApplication)
