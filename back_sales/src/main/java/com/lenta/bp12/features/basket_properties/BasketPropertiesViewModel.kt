package com.lenta.bp12.features.basket_properties

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class BasketPropertiesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val title = MutableLiveData("Корзина 02: C-02/1HAW/O/ПП-256985")

    val properties by lazy {
        MutableLiveData(BasketPropertiesUi(
                section = "02",
                type = "1HAW",
                gisControl = "Партионный",
                provider = "106453 Поставщик 4"
        ))
    }

}

data class BasketPropertiesUi(
        val section: String,
        val type: String,
        val gisControl: String,
        val provider: String
)
