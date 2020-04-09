package com.lenta.bp12.features.good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.ITaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.TaskContentNetRequest
import com.lenta.bp12.request.TaskContentParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var taskContentNetRequest: TaskContentNetRequest

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    lateinit var manager: ITaskManager

    @Inject
    lateinit var appSettings: IAppSettings


    val notProcessedSelectionsHelper = SelectionItemsHelper()

    val processedSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { task ->
            "${task?.type?.type}-${task?.number} // ${task?.name}"
        }
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val notProcessed by lazy {
        task.map { task ->
            val itemList = mutableListOf<SimpleItemGood>()

            task?.goods?.map { good ->
                good.positions.filter { !it.isCounted }.map { position ->
                    itemList.add(SimpleItemGood(
                            name = good.getNameWithMaterial(),
                            quantity = position.quantity
                    ))
                }
            }

            itemList.mapIndexed { index, simpleItemGood ->
                ItemGoodNotProcessedUi(
                        position = "${index + 1}",
                        name = simpleItemGood.name
                )
            }
        }
    }

    val processed by lazy {
        task.map { task ->
            val itemList = mutableListOf<SimpleItemGood>()

            task?.goods?.map { good ->
                good.positions.filter { it.isCounted }.map { position ->
                    itemList.add(SimpleItemGood(
                            name = good.getNameWithMaterial(),
                            quantity = position.quantity
                    ))
                }
            }

            itemList.mapIndexed { index, simpleItemGood ->
                ItemGoodProcessedUi(
                        position = "${index + 1}",
                        name = simpleItemGood.name,
                        quantity = simpleItemGood.quantity.dropZeros()
                )
            }
        }
    }

    val deleteEnabled = MutableLiveData(false)

    val saveEnabled = MutableLiveData(false)

    // -----------------------------

    init {
        viewModelScope.launch {
            loadGoodList()
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun loadGoodList() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            taskContentNetRequest(TaskContentParams(
                    deviceIp = deviceInfo.getDeviceIp(),
                    taskNumber = task.value!!.number,
                    mode = 1,
                    userNumber = appSettings.lastPersonnelNumber ?: ""
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { taskContentResult ->
                viewModelScope.launch {
                    manager.addGoodsInCurrentTask(taskContentResult)
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.goBack()
        navigator.openAlertScreen(failure)
    }

    fun onClickDelete() {

    }

    fun onClickSave() {

    }

    fun onClickItemPosition(position: Int) {

    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }

}

data class SimpleItemGood(
        val name: String,
        val quantity: Double
)

data class ItemGoodNotProcessedUi(
        val position: String,
        val name: String
)

data class ItemGoodProcessedUi(
        val position: String,
        val name: String,
        val quantity: String
)
