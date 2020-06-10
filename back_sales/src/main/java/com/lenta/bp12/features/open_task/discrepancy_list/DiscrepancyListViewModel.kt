package com.lenta.bp12.features.open_task.discrepancy_list

import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.model.SimplePosition
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
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


    val selectionsHelper = SelectionItemsHelper()

    val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { task ->
            "${task?.properties?.type}-${task?.number} // ${task?.name}"
        }
    }

    val goods by lazy {
        task.map { task ->
            val positions = mutableListOf<SimpleItemGood>()
            task?.goods?.forEach { good ->
                good.positions.filter { !it.isDelete && !it.isCounted }.forEach { position ->
                    Logg.d { "--> discrepancy item = $position" }
                    positions.add(SimpleItemGood(
                            name = good.getNameWithMaterial(),
                            material = good.material,
                            providerCode = position.provider.code
                    ))
                }
            }

            positions.mapIndexed { index, position ->
                ItemGoodUi(
                        position = "${positions.size - index}",
                        name = position.name,
                        material = position.material,
                        providerCode = position.providerCode
                )
            }
        }
    }

    // -----------------------------

    fun onClickItemPosition(position: Int) {
        goods.value?.get(position)?.let {
            manager.preparePositionToOpen(it.material, it.providerCode)
            navigator.openGoodInfoOpenScreen()
        }
    }

    private fun isNotEmptyList() = goods.value?.isNullOrEmpty() == false

    private fun getSimplePositions(): List<SimplePosition> {
        val items = mutableListOf<SimplePosition>()
        goods.value?.let { list ->
            selectionsHelper.selectedPositions.value?.let { positions ->
                if (positions.isEmpty()) {
                    list.forEach {
                        items.add(SimplePosition(
                                material = it.material,
                                providerCode = it.providerCode
                        ))
                    }
                } else {
                    positions.forEach { position ->
                        list[position].let {
                            items.add(SimplePosition(
                                    material = it.material,
                                    providerCode = it.providerCode
                            ))
                        }
                    }
                }
            }
        }

        return items
    }

    fun onClickDelete() {
        if (isNotEmptyList()) {
            manager.markPositionsDelete(getSimplePositions())
        }
    }

    fun onClickMissing() {
        if (isNotEmptyList()) {
            manager.markPositionsMissing(getSimplePositions())
        }
    }

    fun onClickSkip() {
        if (isNotEmptyList()) {
            navigator.showRawGoodsRemainedInTask {
                prepareToSaveAndOpenNextScreen()
            }
        } else {
            manager.finishCurrentTask()
            prepareToSaveAndOpenNextScreen()
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

}


data class SimpleItemGood(
        val name: String,
        val material: String,
        val providerCode: String
)

data class ItemGoodUi(
        val position: String,
        val name: String,
        val material: String,
        val providerCode: String
)