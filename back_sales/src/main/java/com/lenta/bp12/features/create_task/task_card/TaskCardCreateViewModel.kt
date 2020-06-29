package com.lenta.bp12.features.create_task.task_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
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

    @Inject
    lateinit var resource: IResourceManager


    /**
    Переменные
     */

    val title by lazy {
        resource.tkNumber(sessionInfo.market ?: "")
    }

    val selectedPage = MutableLiveData(0)

    val taskName by lazy {
        MutableLiveData(resource.backSalesFromDate(SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(Date())))
    }

    /**
    Список типов задачи
     */

    private val sourceTypes = MutableLiveData(listOf<TaskType>())

    private val types = sourceTypes.map {
        it?.let { types ->
            val list = types.toMutableList()
            if (list.size > 1) {
                list.add(0, TaskType(
                        code = "",
                        description = "",
                        isDivBySection = false,
                        isDivByPurchaseGroup = false
                ))
            }

            list.toList()
        }
    }

    val taskTypeList = types.map { list ->
        list?.map { it.description }
    }

    val taskTypePosition = MutableLiveData(0)

    val onSelectTaskType = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            taskTypePosition.value = position
            storagePosition.value = 0
            returnReasonPosition.value = 0

            updateLists()
        }
    }

    /**
    Список складов
     */

    private val sourceStorages = MutableLiveData(listOf<String>())

    val storageList = sourceStorages.map {
        it?.let { storages ->
            val list = storages.toMutableList()
            if (list.size > 1) {
                list.add(0, "")
            }

            list.toList()
        }
    }

    val storagePosition = MutableLiveData(0)

    val onSelectStorage = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            storagePosition.value = position
        }
    }

    /**
    Список причин возврата
     */

    private val sourceReasons = MutableLiveData(emptyList<ReturnReason>())

    private val reasons = sourceReasons.map {
        it?.let { reasons ->
            val list = reasons.toMutableList()
            if (list.size > 1) {
                list.add(0, ReturnReason(
                        code = "",
                        description = ""
                ))
            }

            list.toList()
        }
    }

    val returnReasonList = reasons.map { list ->
        list?.map { it.description }
    }

    val returnReasonPosition = MutableLiveData(0)

    val onSelectReturnReason = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            returnReasonPosition.value = position
        }
    }

    /**
    Описание задачи и аттрибуты
     */

    val taskDescription = taskTypePosition.map {
        it?.let { position ->
            types.value?.let { types ->
                if (types.isNotEmpty()) types[position].description else ""
            }
        }
    }

    private val taskAttributes = MutableLiveData<Set<String>>(emptySet())

    val isAlcohol = taskAttributes.map { attributes ->
        attributes?.contains("A") == true
    }

    val isCommon = taskAttributes.map { attributes ->
        attributes?.contains("N") == true
    }

    /**
    Кнопки нижнего тулбара
     */

    val nextEnabled = taskTypePosition.combineLatest(storagePosition).combineLatest(returnReasonPosition).map { positions ->
        val taskType = positions!!.first.first
        val storage = positions.first.second
        val returnReason = positions.second

        if (taskTypeList.value?.isNotEmpty() == true && storageList.value?.isNotEmpty() == true && returnReasonList.value?.isNotEmpty() == true) {
            taskTypeList.value?.get(taskType)?.isNotEmpty() == true && storageList.value?.get(storage)?.isNotEmpty() == true && returnReasonList.value?.get(returnReason)?.isNotEmpty() == true
        } else false
    }

    /**
    Блок инициализации
     */

    init {
        viewModelScope.launch {
            sourceTypes.value = database.getTaskTypeList()
            updateLists()
        }
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun updateLists() {
        viewModelScope.launch {
            types.value?.let { types ->
                taskTypePosition.value?.let { position ->
                    sourceStorages.value = database.getStorageList(types[position].code)
                    sourceReasons.value = database.getReturnReasonList(types[position].code)
                    taskAttributes.value = database.getTaskAttributes(types[position].code)
                }
            }
        }
    }

    /**
    Обработка нажатий кнопок
     */

    fun onClickNext() {
        manager.updateCurrentTask(TaskCreate(
                name = taskName.value ?: "",
                taskType = types.value!![taskTypePosition.value!!],
                storage = storageList.value!![storagePosition.value!!],
                reason = reasons.value!![returnReasonPosition.value!!]
        ))

        navigator.openTaskCompositionScreen()
    }

}