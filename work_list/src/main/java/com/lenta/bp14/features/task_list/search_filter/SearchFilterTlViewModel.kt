package com.lenta.bp14.features.task_list.search_filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.data.model.TaskType
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import javax.inject.Inject

class SearchFilterTlViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager


    val marketNumber = MutableLiveData<String>("")

    var taskType: TaskType = TaskType.WORK_LIST
    val goodField = MutableLiveData<String>("")
    val sectionField = MutableLiveData<String>("")
    val goodsGroupField = MutableLiveData<String>("")
    val publicationDateField = MutableLiveData<String>("")


    init {
        viewModelScope.launch {
            marketNumber.value = taskManager.marketNumber
        }
    }

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
