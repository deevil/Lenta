package com.lenta.bp9.features.driver_data

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDriverDataInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class DriverDataViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val initials: MutableLiveData<String> = MutableLiveData("")
    val passportData: MutableLiveData<String> = MutableLiveData("")
    val carMake: MutableLiveData<String> = MutableLiveData("")
    val carNumber: MutableLiveData<String> = MutableLiveData("")

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val enabledBtnNext: MutableLiveData<Boolean> = initials.combineLatest(passportData).combineLatest(carMake).combineLatest(carNumber).map {
        it!!.first.first.first!!.isNotEmpty() && it.first.first.second.isNotEmpty() && it.first.second.isNotEmpty() && it.second.isNotEmpty()
    }

    fun onClickNext() {
        screenNavigator.openShipmentArrivalLockLoadingScreen(
                TaskDriverDataInfo(
                        initials = initials.value ?: "",
                        passportData = passportData.value ?: "",
                        carMake = carMake.value ?: "",
                        carNumber = carNumber.value ?: "",
                        additionalCarNumber = "",
                        transportCompanyCode = ""
                )
        )
    }
}
