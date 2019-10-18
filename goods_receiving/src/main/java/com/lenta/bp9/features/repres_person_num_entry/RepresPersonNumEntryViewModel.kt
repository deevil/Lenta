package com.lenta.bp9.features.repres_person_num_entry

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class RepresPersonNumEntryViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val editTextFocus = MutableLiveData<Boolean>()
    val nextButtonFocus = MutableLiveData<Boolean>()

    val personnelNumber = MutableLiveData("")
    val fullName = MutableLiveData("")
    val employeesPosition = MutableLiveData("")
    val enabledApplyButton = fullName.map { !it.isNullOrBlank() }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onClickApply() {
        //todo
    }

    override fun onOkInSoftKeyboard(): Boolean {
        //todo return selectPersonnelNumberDelegate.onOkInSoftKeyboard()
        return true
    }
}
