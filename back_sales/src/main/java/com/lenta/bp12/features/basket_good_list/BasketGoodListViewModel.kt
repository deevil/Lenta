package com.lenta.bp12.features.basket_good_list

import com.lenta.shared.platform.viewmodel.CoreViewModel

class BasketGoodListViewModel : CoreViewModel() {

    // TODO: Implement the ViewModel
}

data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: String
)