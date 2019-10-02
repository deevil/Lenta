package com.lenta.bp14.features.task_list.search_filter

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.data.TaskManager
import com.lenta.bp14.models.data.pojo.TaskType
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class SearchFilterTlViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val marketNumber by lazy { sessionInfo.market }

    val taskTypeList = MutableLiveData<List<String>>()

    private var taskType: TaskType = TaskType.WORK_LIST

    val goodField = MutableLiveData<String>("")
    val sectionField = MutableLiveData<String>("")
    val goodsGroupField = MutableLiveData<String>("")
    val publicationDateField = MutableLiveData<String>("")


    fun onClickFind() {
        taskManager.setCurrentTaskFilter(taskType, goodField.value, sectionField.value, goodsGroupField.value, publicationDateField.value)
        navigator.openTaskListScreen()
    }

    override fun onClickPosition(position: Int) {
        taskType = when (position) {
            0 -> TaskType.WORK_LIST
            1 -> TaskType.PRICE_CHECK
            2 -> TaskType.CHECK_LIST
            3 -> TaskType.NOT_EXPOSED
            else -> throw IllegalArgumentException("Wrong task type position!")
        }
    }

}
