package com.lenta.movement.features.task_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import com.lenta.movement.R
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.*
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.ObtainingTaskListNetRequest
import com.lenta.movement.requests.network.models.task_list.SearchTaskFilter
import com.lenta.movement.requests.network.models.task_list.TaskListParams
import com.lenta.movement.requests.network.models.toTaskList
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.mapSkipNulls
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

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val taskItemList = MutableLiveData<List<TaskListItem>>()

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
                filter = null
        )
    }

    fun onSearch(
            taskType: String?,
            matNr: String?,
            sectionId: String?,
            group: String?,
            dateOfPublic: String?) {
        val filter = SearchTaskFilter(
                taskType = taskType.orEmpty(),
                matNr = matNr.orEmpty(),
                sectionId = sectionId.orEmpty(),
                group = group.orEmpty(),
                dateOfPublic = dateOfPublic.orEmpty()
        )
        netRequest(
                mode = ObtainingTaskListNetRequest.SEARCH_MODE,
                filter = filter
        )
    }

    private fun netRequest(mode: String, filter: SearchTaskFilter?) {
        viewModelScope.launch {
            screenNavigator.showProgress(taskListNetRequest)
            sessionInfo.apply {
                market?.let { marketNumber ->
                    personnelNumber?.let { personnelNumber ->
                        val params = TaskListParams(
                                tkNumber = marketNumber,
                                user = "",
                                mode = mode,
                                personellNumber = personnelNumber,
                                filter = filter
                        )
                        val either = taskListNetRequest(params)
                        either.either(
                                fnL = { failure ->
                                    screenNavigator.hideProgress()
                                    screenNavigator.openAlertScreen(failure)
                                },
                                fnR = { result ->
                                    val resultList = result.taskList
                                    val taskList = resultList.toTaskList()
                                    val taskListMapped = taskList.mapIndexed() { index, task ->
                                        TaskListItem(
                                                number = index + 1,
                                                title = formatter.getTaskTitle(task),
                                                subtitle = formatter.getTaskSubtitle(task),
                                                isClickable = true,
                                                blockTypeResId1 = chooseBlockTypeResId(task.blockType),
                                                quantity = task.quantity,
                                                isCons = task.isCons,
                                                isNotFinish = task.isNotFinish
                                        )
                                    }
                                    taskItemList.value = taskListMapped
                                    screenNavigator.hideProgress()
                                }
                        )
                    }
                }
            }
        }
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
        taskListValue?.let {
            val task = it[position]
            screenNavigator.openTaskScreen(task)
        }
    }

    fun onBackPressed() {
        screenNavigator.goBack()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCode(eanCode.value.orEmpty(), fromScan = false)
        return true
    }

    fun onScanResult(data: String) {
        searchCode(code = data, fromScan = true, isBarCode = true)
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        viewModelScope.launch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                screenNavigator.openTaskGoodsInfoScreen(productInfo)
            }
        }
    }

    fun onDigitPressed(digit: Int) = Unit // TODO

    companion object {
        private const val TO_PROCESS_TAB = 0
        private const val SEARCH_TAB = 1

        private const val EMPTY_IMAGE_VIEW_RES_ID = 0
    }
}