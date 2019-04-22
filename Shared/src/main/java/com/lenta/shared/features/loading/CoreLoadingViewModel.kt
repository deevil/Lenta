package com.lenta.shared.features.loading

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.coroutine.timer
import kotlinx.coroutines.launch

abstract class CoreLoadingViewModel : CoreViewModel() {
    abstract val title: MutableLiveData<String>
    abstract val progress: MutableLiveData<Boolean>
    abstract val speedKbInSec: MutableLiveData<Int>
    abstract val sizeInMb: MutableLiveData<Float>
    val timeInMsec: MutableLiveData<Long> = MutableLiveData(-1)

    init {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            timer(1000) {
                val elapsedTime = System.currentTimeMillis() - startTime
                Logg.d { "elapsedTime: $elapsedTime" }
                timeInMsec.postValue(elapsedTime)
            }
        }
    }
}