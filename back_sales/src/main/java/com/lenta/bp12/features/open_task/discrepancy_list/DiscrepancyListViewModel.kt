package com.lenta.bp12.features.open_task.discrepancy_list

import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class DiscrepancyListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: IOpenTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var deviceInfo: DeviceInfo


    /**
    Переменные
     */

    val selectionsHelper = SelectionItemsHelper()

    val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { task ->
            "${task?.getFormattedName(true)}"
        }
    }

    val goods by lazy {
        task.map { task ->
            task?.goods?.filter { !it.isDeleted && !it.isCounted && !it.isMissing }?.mapIndexed { index, good ->
                ItemGoodUi(
                        position = "${task.goods.size - index}",
                        name = good.name,
                        material = good.material,
                        providerCode = good.provider.code
                )
            }
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val deleteEnabled = selectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    val missingEnabled = selectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    /**
    Методы
     */

    fun onClickItemPosition(position: Int) {
        task.value?.let { task ->
            goods.value?.get(position)?.material?.let { material ->
                task.goods.find { it.material == material }?.let { good ->
                    manager.updateCurrentGood(good)
                    navigator.openGoodInfoOpenScreen()
                }
            }
        }
    }

    private fun prepareToSaveAndOpenNextScreen() {
        manager.prepareSendTaskDataParams(
                deviceIp = deviceInfo.getDeviceIp(),
                tkNumber = sessionInfo.market ?: "",
                userNumber = sessionInfo.personnelNumber ?: ""
        )
        navigator.openSaveDataScreen()
    }

    /**
    Обработка нажатий кнопок
     */

    fun onClickDelete() {
        task.value?.let { task ->
            selectionsHelper.selectedPositions.value?.forEach { position ->
                goods.value?.get(position)?.material?.let { material ->
                    task.goods.find { it.material == material }?.let { good ->
                        good.isDeleted = true
                    }
                }
            }

            manager.updateCurrentTask(task)
        }
    }

    fun onClickMissing() {
        task.value?.let { task ->
            selectionsHelper.selectedPositions.value?.forEach { position ->
                goods.value?.get(position)?.material?.let { material ->
                    task.goods.find { it.material == material }?.let { good ->
                        good.isMissing = true
                    }
                }
            }

            manager.updateCurrentTask(task)
        }
    }

    fun onClickSkip() {
        if (goods.value?.isNotEmpty() == true) {
            navigator.showRawGoodsRemainedInTask {
                prepareToSaveAndOpenNextScreen()
            }
        } else {
            manager.finishCurrentTask()
            prepareToSaveAndOpenNextScreen()
        }
    }

}

data class ItemGoodUi(
        val position: String,
        val name: String,
        val material: String,
        val providerCode: String
)