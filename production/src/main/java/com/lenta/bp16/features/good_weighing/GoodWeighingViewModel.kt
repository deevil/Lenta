package com.lenta.bp16.features.good_weighing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.pojo.Pack
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.PackCodeNetRequest
import com.lenta.bp16.request.PackCodeParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodWeighingViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var taskManager: ITaskManager
    @Inject
    lateinit var packCodeNetRequest: PackCodeNetRequest


    val good by lazy {
        taskManager.currentGood
    }

    val raw by lazy {
        taskManager.currentRaw
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val deviceIp = MutableLiveData("")

    val weightField = MutableLiveData("0")

    private val entered = weightField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    private val weighted = MutableLiveData<Double>(0.0)

    private val total = entered.map {
        it.sumWith(weighted.value ?: 0.0)
    }

    val totalWithUnits = total.map {
        "${it.dropZeros()} ${good.value!!.units.name}"
    }

    val planned by lazy {
        "${raw.value!!.planned.dropZeros()} ${good.value!!.units.name}"
    }

    val completeEnabled: MutableLiveData<Boolean> = total.map {
        it ?: 0.0 != 0.0
    }

    val addEnabled: MutableLiveData<Boolean> = entered.map {
        it ?: 0.0 != 0.0
    }

    // -----------------------------

    fun onClickComplete() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            packCodeNetRequest(
                    PackCodeParams(
                            marketNumber = sessionInfo.market ?: "Not found!",
                            taskType = taskManager.getTaskTypeCode(),
                            parent = taskManager.currentTask.value!!.taskInfo.number,
                            deviceIp = deviceIp.value ?: "Not found!",
                            material = good.value!!.material,
                            orderNumber = raw.value!!.orderNumber,
                            quantity = total.value!!
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { packCodeResult ->
                good.value?.let {
                    it.packs.add(0,
                            Pack(
                                    material = it.material,
                                    materialOsn = raw.value!!.materialOsn,
                                    code = packCodeResult.packCode,
                                    quantity = total.value!!
                            )
                    )

                    good.value = it
                }

                printTag()

                total.value = 0.0
                weightField.value = "0"

                navigator.openPackListScreen()
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    fun onClickAdd() {
        weighted.value = total.value!!
        weightField.value = ""
    }

    fun onClickGetWeight() {
        // todo Реализовать получения веса с весов
        weightField.value = "2.5"
    }

    private fun printTag() {
        // todo Реализовать печать штрих-кода тары

    }

}