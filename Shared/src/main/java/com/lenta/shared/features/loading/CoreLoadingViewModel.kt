package com.lenta.shared.features.loading

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch

abstract class CoreLoadingViewModel : CoreViewModel(), ICoreLoadingViewModel {
    override val elapsedTime: MutableLiveData<Long> = MutableLiveData(0)
    override val remainingTime: MutableLiveData<Long> = MutableLiveData()

    init {
        viewModelScope.launch {
            startProgressTimer(
                    coroutineScope = this,
                    elapsedTime = elapsedTime
            )
        }

    }
}

class TimerLoadingViewModel : ICoreLoadingViewModel {
    override val title: MutableLiveData<String> = MutableLiveData("")
    override val progress: MutableLiveData<Boolean> = MutableLiveData(false)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()
    override val elapsedTime: MutableLiveData<Long> = MutableLiveData(-1)
    override val remainingTime: MutableLiveData<Long> = MutableLiveData(-1)

    override fun clean() {
        title.postValue("")
        progress.postValue(false)
        speedKbInSec.postValue(-1)
        sizeInMb.postValue(-1F)
        elapsedTime.postValue(-1)
        remainingTime.postValue(-1)
    }
}


interface ICoreLoadingViewModel {
    val title: MutableLiveData<String>
    val progress: MutableLiveData<Boolean>
    val speedKbInSec: MutableLiveData<Int>
    val sizeInMb: MutableLiveData<Float>
    val elapsedTime: MutableLiveData<Long>
    val remainingTime: MutableLiveData<Long>
    fun clean()
}