package com.lenta.bp9.features.repres_person_num_entry

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.PersonnelNumberNetRequest
import com.lenta.shared.requests.network.TabNumberInfo
import com.lenta.shared.requests.network.TabNumberParams
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class RepresPersonNumEntryViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var personnelNumberNetRequest: PersonnelNumberNetRequest
    @Inject
    lateinit var timeMonitor: ITimeMonitor

    val sectionInfo: MutableLiveData<TaskSectionInfo> = MutableLiveData()
    val editTextFocus = MutableLiveData<Boolean>()
    private val nextButtonFocus = MutableLiveData<Boolean>()

    val personnelNumber = MutableLiveData("")
    val fullName = MutableLiveData("")
    val employeesPosition = MutableLiveData("")
    val enabledBtnApply = fullName.map { !it.isNullOrBlank() }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun getDescription(): String {
        return "${sectionInfo.value!!.sectionNumber}-${sectionInfo.value!!.sectionName}"
    }

    private fun searchPersonnelNumber() {
        viewModelScope.launch {
            screenNavigator.showProgress(personnelNumberNetRequest)
            personnelNumberNetRequest(TabNumberParams(tabNumber = personnelNumber.value
                    ?: "")).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(personnelNumberInfo: TabNumberInfo) {
        Logg.d { "handleSuccess $personnelNumberInfo" }
        fullName.value = personnelNumberInfo.name
        employeesPosition.value = personnelNumberInfo.jobName
        viewModelScope.launch {
            nextButtonFocus.postValue(true)
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    fun onClickApply() {
        val milliseconds = timeMonitor.getUnixTime()
        val currentDate = DateTimeUtil.formatDate(milliseconds, Constants.DATE_FORMAT_yyyy_mm_dd)
        val currentTime = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_hhmmss)
        taskManager.getReceivingTask()?.taskRepository?.getSections()?.changeSection(sectionInfo.value!!.copy(
                personnelNumber = personnelNumber.value!!,
                employeeName = fullName.value!!,
                dateTransferSection = currentDate,
                timeTransferSection = currentTime))
        screenNavigator.openTransferGoodsSectionScreen()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchPersonnelNumber()
        return true
    }
}
