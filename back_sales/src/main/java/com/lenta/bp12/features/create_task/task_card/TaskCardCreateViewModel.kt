package com.lenta.bp12.features.create_task.task_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.Properties
import com.lenta.bp12.model.pojo.create_task.Task
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TaskCardCreateViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var database: IDatabaseRepository

    @Inject
    lateinit var manager: ICreateTaskManager


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val selectedPage = MutableLiveData(0)

    val taskName = MutableLiveData(
            "Возврат от ${SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(Date())}"
    )

    val taskTypePosition = MutableLiveData(0)

    val storagePosition = MutableLiveData(0)

    val returnReasonPosition = MutableLiveData(0)

    val onSelectTaskType = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            taskTypePosition.value = position
            storagePosition.value = 0
            returnReasonPosition.value = 0

            updateLists()
        }
    }

    val onSelectStorage = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            storagePosition.value = position
        }
    }

    val onSelectReturnReason = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            returnReasonPosition.value = position
        }
    }

    private val types = MutableLiveData<List<Properties>>(emptyList())

    private val reasons = MutableLiveData<List<ReturnReason>>(emptyList())

    val taskTypeList = types.map { list ->
        list?.map { it.description }
    }

    val storageList = MutableLiveData<List<String>>(emptyList())

    val returnReasonList = reasons.map { list ->
        list?.map { it.description }
    }

    val taskDescription = taskTypePosition.map { position ->
        if (types.value?.isNotEmpty() == true) {
            types.value!![position!!].description
        } else ""
    }

    val nextEnabled = taskTypePosition.combineLatest(storagePosition).combineLatest(returnReasonPosition).map { positions ->
        val taskType = positions!!.first.first
        val storage = positions.first.second
        val returnReason = positions.second

        if (taskTypeList.value?.isNotEmpty() == true && storageList.value?.isNotEmpty() == true && returnReasonList.value?.isNotEmpty() == true) {
            taskTypeList.value?.get(taskType)?.isNotEmpty() == true && storageList.value?.get(storage)?.isNotEmpty() == true && returnReasonList.value?.get(returnReason)?.isNotEmpty() == true
        } else false
    }

    private val taskAttributes = MutableLiveData<Set<String>>(emptySet())

    val isAlcohol = taskAttributes.map { attributes ->
        attributes?.contains("A") == true
    }

    val isCommon = taskAttributes.map { attributes ->
        attributes?.contains("N") == true
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            types.value = database.getTaskTypeList()
            updateLists()
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun updateLists() {
        viewModelScope.launch {
            storageList.value = database.getStorageList(types.value!![taskTypePosition.value!!].type)
            reasons.value = database.getReturnReasonList(types.value!![taskTypePosition.value!!].type)
            taskAttributes.value = database.getTaskAttributes(types.value!![taskTypePosition.value!!].type)
        }
    }

    fun onClickNext() {
        manager.updateCurrentTask(Task(
                name = taskName.value!!,
                properties = types.value!![taskTypePosition.value!!],
                storage = storageList.value!![storagePosition.value!!],
                reason = reasons.value!![returnReasonPosition.value!!]
                //isAlcoholAllowed = isAlcohol.value!!,
                //isCommonAllowed = isCommon.value!!
        ))

        navigator.openTaskCompositionScreen()
    }

}