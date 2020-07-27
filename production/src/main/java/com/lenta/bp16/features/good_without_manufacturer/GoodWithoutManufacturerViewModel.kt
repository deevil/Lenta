package com.lenta.bp16.features.good_without_manufacturer

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodWithoutManufacturerViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    val deviceIp = MutableLiveData("")

    val weightField = MutableLiveData("0")

    private val entered = weightField.map{
        it?.toDoubleOrNull() ?: 0.0
    }

    val completeEnabled = entered.map{
        val enteredValue = it ?: 0.0
    }

    fun onClickComplete(){
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            //TODO Обработать клик
            navigator.goBack()
        }
    }
}