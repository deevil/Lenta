package com.lenta.bp16.features.pack_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.ContainerInfo
import com.lenta.bp16.request.DefrostingFinishParams
import com.lenta.bp16.request.DefrostingFinishRequest
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class PackListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager
    @Inject
    lateinit var defrostingFinishRequest: DefrostingFinishRequest
    @Inject
    lateinit var deviceInfo: DeviceInfo


    val good by lazy {
        taskManager.currentGood
    }

    val raw by lazy {
        taskManager.currentRaw
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val packs: MutableLiveData<List<ItemPackListUi>> by lazy {
        good.map { good ->
            good?.packs?.let { packs ->
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
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            defrostingFinishRequest(getParams()).either({ failure ->
                navigator.openAlertScreen(failure)
            }) {
                if (raw.value?.isDefrost == true) {
                    navigator.showDefrostingPhaseIsCompleted {
                        navigator.goBack()
                        navigator.goBack()
                    }
                } else {
                    navigator.goBack()
                    navigator.goBack()
                }
            }
            navigator.hideProgress()
        }
    }

    private fun getParams(): DefrostingFinishParams {
        return DefrostingFinishParams(
                ip = deviceInfo.getDeviceIp(),
                containers = packs.value?.map {
                    ContainerInfo(
                            it.code
                    )
                } ?: emptyList()
        )
    }

}

data class ItemPackListUi(
        val position: String,
        val number: String,
        val name: String,
        val weight: String,
        val code: String

)