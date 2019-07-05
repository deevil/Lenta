package com.lenta.bp7.features.option

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel

class OptionViewModel : CoreViewModel() {

    val withFacing: MutableLiveData<Boolean> = MutableLiveData(false)
    val checkEmptyPlaces: MutableLiveData<Boolean> = MutableLiveData(false)

    val nextButtonFocus = MutableLiveData<Boolean>()


    fun onClickNext() {
        // Куда-то сохранить состояние галок
        // ...

        // Перейти к следующему экрану
        // ...

    }
}
