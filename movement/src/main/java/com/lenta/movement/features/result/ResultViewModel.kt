package com.lenta.movement.features.result

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ResultViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    val deviceIp = MutableLiveData("")

    fun onClickComplete(){
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            //TODO Обработать клик
        }
    }
}