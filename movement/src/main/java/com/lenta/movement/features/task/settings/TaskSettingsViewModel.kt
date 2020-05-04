package com.lenta.movement.features.task.settings

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.date_time.DateTimeUtil
import java.lang.Exception
import javax.inject.Inject

class TaskSettingsViewModel: CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var taskManager: ITaskManager

    val selectedPagePosition = MutableLiveData(0)

    val nextEnabled = MutableLiveData(false)

    fun getTitle(): String {
        return sessionInfo.market.orEmpty()
    }

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position

        taskManager.setOnTaskChanges { task ->
            nextEnabled.postValue(
                task.isCreated ||
                        (task.name.isNotEmpty()
                                && task.receiver.isNotEmpty()
                                && task.pikingStorage.isNotEmpty()
                                && task.shipmentStorage.isNotEmpty()
                                && task.shipmentDate.isValidDate())
            )
        }
    }

    fun onNextClick() {
        screenNavigator.openTaskCompositionScreen()
    }

    private fun String.isValidDate(): Boolean {
        return try {
            DateTimeUtil.getDateFromString(this, Constants.DATE_FORMAT_ddmmyy)
            true
        } catch (_: Exception) {
            false
        }
    }
}