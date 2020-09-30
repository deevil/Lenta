package com.lenta.bp12.features.create_task.task_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.platform.extention.isWholesaleType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.orIfNull
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
        resource.tk(sessionInfo.market.orEmpty())
    }

    val taskName by lazy {
        selectedType.map { type ->
            type?.takeIf { !it.isWholesaleType() }?.run {
                val date = SimpleDateFormat(
                        Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm,
                        Locale.getDefault()
                ).format(Date())

                resource.backSalesFromDate(date)
            }.orEmpty()
        }
    }

    val provider by lazy {
        selectedType.map { type ->
            if (type?.isWholesaleType() == false) {
                resource.allSuppliers
            } else {
                resource.wholesaleBuyer
            }
        }
    }

    /**
    Список типов задачи
     */

    private val types = MutableLiveData(listOf<TaskType>())

    val taskTypeList = types.mapSkipNulls { list ->
        list.map { it.description }
    }

    val taskTypePosition = MutableLiveData(DEFAULT_POSITION)

    private val selectedType by lazy {
        types.combineLatest(taskTypePosition).mapSkipNulls {
            updateLists()
            val (list, position) = it
            list.getOrNull(position)
        }
    }

    /**
    Список складов
     */

    val storage = MutableLiveData(emptyList<String>())

    val storagePosition = MutableLiveData(DEFAULT_POSITION)

    /**
    Список причин возврата
     */

    private val reasons = MutableLiveData(emptyList<ReturnReason>())

    val returnReasonList = reasons.mapSkipNulls { list ->
        list.map { it.description }
    }

    val returnReasonPosition = MutableLiveData(DEFAULT_POSITION)

    /**
    Описание задачи и аттрибуты
     */
    val taskDescription = taskTypePosition.mapSkipNulls { position ->
        types.value?.let { types ->
            if (types.isNotEmpty()) types[position].description else ""
        }
    }

    private val taskAttributes = MutableLiveData<Set<String>>(emptySet())

    val isAlcohol = taskAttributes.mapSkipNulls { attributes ->
        attributes.contains(ControlType.ALCOHOL.code)
    }

    val isCommon = taskAttributes.mapSkipNulls { attributes ->
        attributes.contains(ControlType.COMMON.code)
    }

    val isWholeSaleTask by lazy {
        selectedType.switchMap {
            liveData {
                val isWholesaleType = it?.isWholesaleType()
                emit(isWholesaleType)
            }
        }
    }

    /**
    Кнопки нижнего тулбара
     */
    val nextEnabled by lazy {
        taskName.mapSkipNulls {
            it.isNotEmpty()
        }
    }

    /**
    Блок инициализации
     */

    init {
        onInitTaskCard()
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun updateLists() {
        launchUITryCatch {
            types.value?.let { types ->
                taskTypePosition.value?.let { position ->
                    types.getOrNull(position)?.let {
                        storage.value = database.getStorageList(it.code)
                        taskAttributes.value = database.getTaskAttributes(it.code)
                        reasons.value = database.getReturnReasonList(it.code)
                    }
                }
            }
        }
    }

    /**
    Обработка нажатий кнопок
     */

    fun onClickNext() {
        taskTypePosition.value?.let { taskTypePositionValue ->
            storagePosition.value?.let { storagePositionValue ->
                //Причина возврата может быть пустой если это ОПТ
                val reason = returnReasonPosition.value
                        ?.takeIf { isWholeSaleTask.value == false }
                        ?.let { returnReasonPositionValue ->
                            reasons.value?.getOrNull(returnReasonPositionValue)
                        }

                types.value?.getOrNull(taskTypePositionValue)?.let { type ->
                    val task = TaskCreate(
                            name = taskName.value.orEmpty(),
                            type = type,
                            storage = storage.value?.getOrNull(storagePositionValue).orEmpty(),
                            reason = reason
                    )
                    manager.updateCurrentTask(task)
                    manager.isWholesaleTaskType = (isWholeSaleTask.value == true)
                    navigator.openTaskCompositionScreen()

                }.orIfNull { Logg.e { "TaskType null" } }
            }.orIfNull { Logg.e { "storagePosition null" } }
        }.orIfNull { Logg.e { "taskTypePosition null" } }
    }

    private fun onInitTaskCard() {
        launchUITryCatch {
            types.value = database.getTaskTypeList()
            updateLists()
        }
    }

    companion object {
        private const val DEFAULT_POSITION = 0
    }
}