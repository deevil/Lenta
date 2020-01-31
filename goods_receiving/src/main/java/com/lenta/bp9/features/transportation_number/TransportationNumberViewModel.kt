package com.lenta.bp9.features.transportation_number

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class TransportationNumberViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    var mode: String = ""

    val transportationNumber: MutableLiveData<String> = MutableLiveData("")

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val enabledBtnNext: MutableLiveData<Boolean> = transportationNumber.map {
        it?.isNotEmpty()
    }

    fun onClickNext() {
        screenNavigator.openShipmentPurposeTransportLoadingScreen("1", transportationNumber.value!!)

    }
}
