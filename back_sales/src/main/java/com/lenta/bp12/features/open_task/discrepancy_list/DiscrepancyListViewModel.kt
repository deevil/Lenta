package com.lenta.bp12.features.open_task.discrepancy_list

import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.orIfNull
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

    @Inject
    lateinit var resource: IResourceManager


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
        task.mapSkipNulls { task ->
            val list = task.goods.filter { !it.isCounted }
            list.mapIndexed { index, good ->
                ItemGoodUi(
                        position = "${list.size - index}",
                        name = good.name,
                        material = good.material,
                        providerCode = good.provider.code.orEmpty(),
                        quantity = chooseQuantity(good)
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
                task.goods.find { it.material == material }?.let {
                    navigator.openGoodInfoOpenScreen()
                }
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    private fun prepareToSaveAndOpenNextScreen() {
        manager.prepareSendTaskDataParams(
                deviceIp = deviceInfo.getDeviceIp(),
                tkNumber = sessionInfo.market.orEmpty(),
                userNumber = sessionInfo.personnelNumber.orEmpty()
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

            selectionsHelper.clearPositions()
            manager.updateCurrentTask(task)
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    fun onClickMissing() {
        task.value?.let { task ->
            selectionsHelper.selectedPositions.value?.forEach { position ->
                goods.value?.get(position)?.material?.let { material ->
                    task.goods.find { it.material == material }?.let { good ->
                        good.isCounted = true
                    }
                }
            }

            selectionsHelper.clearPositions()
            manager.updateCurrentTask(task)
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    fun onClickSkip() {
        goods.value?.let { goods ->
            if (goods.isNotEmpty()) {
                navigator.showRawGoodsRemainedInTask {
                    prepareToSaveAndOpenNextScreen()
                }
            } else {
                manager.finishCurrentTask()
                prepareToSaveAndOpenNextScreen()
            }
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private fun chooseQuantity(good: Good): String {
        return if (good.planQuantity > 0.0) {
            "${good.planQuantity} - ${good.getTotalQuantity()} ${good.commonUnits.name}"
        } else {
            "${good.getTotalQuantity()} ${good.commonUnits.name}"
        }
    }

}

data class ItemGoodUi(
        val position: String,
        val name: String,
        val material: String,
        val providerCode: String,
        val quantity: String
)