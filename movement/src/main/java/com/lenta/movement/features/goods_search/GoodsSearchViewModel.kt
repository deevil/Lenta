package com.lenta.movement.features.goods_search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsSearchViewModel : CoreViewModel() {

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