package com.lenta.movement.features.goods_without_manufacturer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsWithoutManufacturerViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    val deviceIp = MutableLiveData("")

    val weightField = MutableLiveData("0")

    private val entered = weightField.map{
        it?.toDoubleOrNull() ?: 0.0
    }

    val completeEnabled = entered.map{
        it ?: 0.0 != 0.0
    }

    fun onClickComplete(){
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            //TODO Обработать клик
        }
    }
}