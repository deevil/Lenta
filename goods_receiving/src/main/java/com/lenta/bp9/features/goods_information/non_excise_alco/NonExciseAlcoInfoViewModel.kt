package com.lenta.bp9.features.goods_information.non_excise_alco

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel

class NonExciseAlcoInfoViewModel : CoreViewModel() {

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val suffix: MutableLiveData<String> = MutableLiveData()
    val count: MutableLiveData<String> = MutableLiveData("0")
    val acceptTotalCount: MutableLiveData<Double> = MutableLiveData(0.0)
    val refusalTotalCount: MutableLiveData<Double> = MutableLiveData(0.0)
}
