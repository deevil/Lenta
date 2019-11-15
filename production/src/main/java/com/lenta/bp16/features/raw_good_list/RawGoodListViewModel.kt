package com.lenta.bp16.features.raw_good_list

import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class RawGoodListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val title by lazy {
        "ЕО - 31354361354313546543131"
    }

}

data class ItemRawGoodListUi(
        val position: String,
        val sku: String,
        val arrived: String,
        val remain: String
)