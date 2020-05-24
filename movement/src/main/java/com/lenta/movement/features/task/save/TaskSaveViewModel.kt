package com.lenta.movement.features.task.save

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class TaskSaveViewModel : CoreViewModel() {

    var tasks: List<String>? = null

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var formatter: IFormatter

    val taskList by lazy {
        MutableLiveData(
            tasks.orEmpty().mapIndexed { index, taskTitle ->
                TaskSaveListItem(
                    number = index + 1,
                    title = taskTitle
                )
            }
        )
    }

    fun getTitle(): String {
        return formatter.formatMarketName(sessionInfo.market.orEmpty())
    }

    fun onNextClick() {
        screenNavigator.openMainMenuScreen() // TODO
    }

}