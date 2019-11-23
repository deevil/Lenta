package com.lenta.bp9.features.mercury_exception_integration

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class MercuryExceptionIntegrationViewModel : CoreViewModel(), OnPositionClickListener {


    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val reasonsExclusion: List<String> = listOf("Другое", "Причина 1", "Причина 2", "Причина 3")

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onClickNext() {
        return
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }
}
