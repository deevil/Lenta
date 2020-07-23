package com.lenta.bp16.features.select_good

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodSelectViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    val deviceIp = MutableLiveData("")

    fun onClickNext(){
        viewModelScope.launch {
            navigator.showProgressLoadingData(::handleFailure)
            //TODO Заполнить форму данными по запросу номера штрихкода
            /*Допустим, пока так, потом с появлением ТП пропишу логику*/
            navigator.openGoodInfoScreen()
        }
    }

}