package com.lenta.movement.features.task_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.lenta.movement.R
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.*
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.ObtainingTaskListNetRequest
import com.lenta.movement.requests.network.models.task_list.SearchTaskFilter
import com.lenta.movement.requests.network.models.task_list.TaskListParams
import com.lenta.movement.requests.network.models.task_list.TaskListResult
import com.lenta.movement.requests.network.models.toTaskList
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var formatter: IFormatter

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    @Inject
    lateinit var taskListNetRequest: ObtainingTaskListNetRequest

    val selectedPagePosition = MutableLiveData(TO_PROCESS_TAB)
    val currentPage = selectedPagePosition.mapSkipNulls { TaskListPage.values()[it] }

    val searchFilter: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val taskItemList2 = MutableLiveData<List<TaskListItem>>()

    val taskItemList by unsafeLazy {
        taskList.switchMap { taskList ->
            liveData {
                val taskListMapped = taskList.mapIndexed { index, task ->
                    TaskListItem(
                            number = index + 1,
                            title = formatter.getTaskTitle(task),
                            subtitle = taskManager.getMovementTypeShort(task.movementType),
                            isClickable = true,
                            blockTypeResId1 = chooseBlockTypeResId(task.blockType),
                            quantity = task.quantity,
                            isCons = task.isCons,
                            isNotFinish = task.isNotFinish
                    )
                }
                emit(taskListMapped)
            }
        }
    }
    private val taskList by unsafeLazy {
        MutableLiveData<List<Task>>()
    }

    fun onResume() {
        onUpdateBtnClick()
    }

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
    }

    fun getTitle(): String {
        return "${sessionInfo.market}"
    }

    fun onUpdateBtnClick() {
        netRequest(
                mode = ObtainingTaskListNetRequest.UPDATE_MODE,
                filter = null,
                user = NO_USER_SEARCH
        )
    }

    fun onExtendedSearch(
            taskType: String?,
            matNr: String?,
            sectionId: String?,
            group: String?,
            dateOfPublic: String?,
            user: String) {
        val filter = SearchTaskFilter(
                taskType = taskType.orEmpty(),
                matNr = matNr.orEmpty(),
                sectionId = sectionId.orEmpty(),
                group = group.orEmpty(),
                dateOfPublic = dateOfPublic.orEmpty()
        )
        netRequest(
                mode = ObtainingTaskListNetRequest.SEARCH_MODE,
                filter = filter,
                user = user
        )
    }

    private fun netRequest(mode: String, filter: SearchTaskFilter?, user: String) {
        viewModelScope.launch {
            screenNavigator.showProgress(taskListNetRequest)
            sessionInfo.apply {
                market?.let { marketNumber ->
                    personnelNumber?.let { personnelNumber ->
                        val params = TaskListParams(
                                tkNumber = marketNumber,
                                user = user,
                                mode = mode,
                                personellNumber = personnelNumber,
                                filter = filter
                        )
                        val either = taskListNetRequest(params)
                        either.either(
                                fnL = ::onFailResultHandler,
                                fnR = ::onSuccessResultHandler
                        )
                    }
                }
            }
        }
    }

    private fun onFailResultHandler(failure: Failure) {
        with(screenNavigator) {
            hideProgress()
            openAlertScreen(failure)
        }
    }

    private fun onSuccessResultHandler(result: TaskListResult) {
        val resultList = result.taskList
        val taskListMappedFromRestModel = resultList.toTaskList()
        taskList.value = taskListMappedFromRestModel
        screenNavigator.hideProgress()
    }

    private fun chooseBlockTypeResId(blockType: String): Int {
        return when (blockType) {
            Task.SELF_BLOCK_TYPE -> R.drawable.ic_self_lock_status_gray_24dp
            Task.ALIEN_BLOCK_TYPE -> R.drawable.ic_lock_white_24dp
            else -> {
                Logg.e {
                    "wrong or empty task blocktype"
                }
                EMPTY_IMAGE_VIEW_RES_ID
            }
        }
    }

    fun onClickTaskListItem(position: Int) {
        val taskListValue = taskList.value
        taskListValue?.getOrNull(position)?.let { task ->
            screenNavigator.openTaskScreen(task)
        }.orIfNull {
            Logg.e { "wrong position for task list" }
            screenNavigator.openAlertScreen(Failure.ServerError)
        }
    }

    fun onBackPressed() {
        screenNavigator.goBack()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        onSearch()
        return true
    }

    private fun onSearch() {
        val searchText = searchFilter.value
        searchText?.let {
            netRequest(ObtainingTaskListNetRequest.UPDATE_MODE,
                    filter = null,
                    user = it)
        }
    }

    fun onDigitPressed(digit: Int) = Unit // TODO

    companion object {
        private const val TO_PROCESS_TAB = 0
        private const val NO_USER_SEARCH = ""
        private const val EMPTY_IMAGE_VIEW_RES_ID = 0
    }
}