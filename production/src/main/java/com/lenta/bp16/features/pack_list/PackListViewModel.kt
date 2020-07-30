package com.lenta.bp16.features.pack_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.EndDefrostingNetRequest
import com.lenta.bp16.request.EndDefrostingParams
import com.lenta.bp16.request.pojo.PackCode
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class PackListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ITaskManager

    @Inject
    lateinit var endDefrostingNetRequest: EndDefrostingNetRequest

    @Inject
    lateinit var deviceInfo: DeviceInfo


    val good by lazy {
        manager.currentGood
    }

    val raw by lazy {
        manager.currentRaw
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val packs: MutableLiveData<List<ItemPackListUi>> by lazy {
        good.map { good ->
            good?.packs?.filter {
                it.isNotDefect()
            }?.filter {
                it.materialOsn == raw.value?.materialOsn
            }?.filter {
                if (raw.value?.isWasDef == true) !it.isDefOut else !it.isDefOut || it.isDefOut
            }?.let { packs ->
                packs.mapIndexed { index, pack ->
                    ItemPackListUi(
                            position = (packs.size - index).toString(),
                            number = "Тара №${pack.getShortPackNumber()}",
                            name = raw.value!!.name,
                            weight = "${pack.quantity.dropZeros()} ${good.units.name}",
                            code = pack.code
                    )
                }
            }
        }
    }

    // -----------------------------

    fun onClickAdd() {
        navigator.goBack()
    }

    fun onClickComplete() {
        if (raw.value?.isWasDef == true) {
            launchUITryCatch {
                navigator.showProgressLoadingData(::handleFailure)
                endDefrostingNetRequest(EndDefrostingParams(
                        deviceIp = deviceInfo.getDeviceIp(),
                        packCodes = packs.value?.map {
                            PackCode(code = it.code)
                        } ?: emptyList()
                )
                ).either({ failure ->
                    navigator.openAlertScreen(failure)
                }) { result ->
                    if (result.isAutofix.isSapTrue()) {
                        navigator.showFixStartNextStageSuccessful {
                            navigator.goBack()
                            navigator.goBack()
                        }
                    } else {
                        navigator.showDefrostingPhaseIsCompleted {
                            navigator.goBack()
                            navigator.goBack()
                        }
                    }
                }
                navigator.hideProgress()
            }
        } else {
            navigator.goBack()
            navigator.goBack()
        }
    }

    fun onClickLabel() {
        navigator.openReprintLabelScreen()
    }

}

data class ItemPackListUi(
        val position: String,
        val number: String,
        val name: String,
        val weight: String,
        val code: String
)