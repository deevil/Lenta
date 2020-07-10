package com.lenta.bp16.features.goods_select

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsSelectViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    val deviceIp = MutableLiveData("")

    fun onClickNext(){
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            //TODO Заполнить форму данными по запросу номера штрихкода
        }
    }

}