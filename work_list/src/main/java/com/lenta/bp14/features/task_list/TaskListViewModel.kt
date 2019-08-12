package com.lenta.bp14.features.task_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    val selectedPage = MutableLiveData(0)

    val unprocessedTasks = MutableLiveData<List<TaskInfoVM>>(getTestItems())
    val filteredTasks = MutableLiveData<List<TaskInfoVM>>(getTestItems())

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

    private fun getTestItems(): List<TaskInfoVM>? {
        return listOf(
                TaskInfoVM(
                        number = 3,
                        name = "name3",
                        type = "type",
                        status = "status",
                        quantity = "10"
                ),
                TaskInfoVM(
                        number = 2,
                        name = "name2",
                        type = "type",
                        status = "status",
                        quantity = "10"
                ),
                TaskInfoVM(
                        number = 1,
                        name = "name1",
                        type = "type",
                        status = "status",
                        quantity = "10"
                )
        )
    }

    val filter = MutableLiveData("")

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected: $position" }
        selectedPage.value = position

    }

    fun getMarket(): String {
        return sessionInfo.market!!
    }

    fun onClickUpdate() {

    }

    fun onClickFilter() {

    }

    fun onClickSave() {

    }

    fun onClickUnprocessedTask(position: Int) {
        screenNavigator.openJobCardScreen(taskNumber = "100")
    }

    fun onClickProcessedTask(position: Int) {
        screenNavigator.openJobCardScreen(taskNumber = "100")
    }
}

data class TaskInfoVM(
        val number: Int,
        val name: String,
        val type: String,
        val status: String,
        val quantity: String
)

