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


    val title by lazy {
        taskManager.currentGood.getNameWithMaterial(" - ")
    }

    val deviceIp: MutableLiveData<String> = MutableLiveData("")

    val good by lazy {
        taskManager.currentGood
    }

    val raw by lazy {
        taskManager.currentRaw
    }

    val weight = MutableLiveData("")

    private val enteredWeight = weight.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    private val totalWeight = enteredWeight.map {
        it.sumWith(raw.totalQuantity)
    }

    val totalWeightWithUnits = totalWeight.map {
        "${it.dropZeros()} ${good.units.name}"
    }

    val planned by lazy {
        "${raw.planned} ${good.units.name}"
    }

    val completeEnabled: MutableLiveData<Boolean> = weight.map {
        it?.toDoubleOrNull() ?: 0.0 != 0.0
    }

    val addEnabled: MutableLiveData<Boolean> = weight.map {
        it?.toDoubleOrNull() ?: 0.0 != 0.0
    }

    // -----------------------------

    fun onClickGetWeight() {
        // todo Реализовать получения веса с весов
        weight.value = "2.5"
    }

    fun onClickAdd() {
        createPack()
    }

    fun onClickComplete() {
        createPack()

        if (weight.value!!.isEmpty()) {
            navigator.openPackListScreen()
        }
    }

    private fun createPack() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            packCodeNetRequest(
                    PackCodeParams(
                            marketNumber = sessionInfo.market ?: "Not found!",
                            parentType = 1,
                            parent = taskManager.currentTask.task.number,
                            deviceIp = deviceIp.value ?: "Not found!",
                            material = good.material,
                            orderNumber = raw.orderNumber,
                            quantity = raw.totalQuantity
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { packCodeResult ->
                good.packs.add(
                        Pack(
                                material = good.material,
                                materialOsn = raw.materialOsn,
                                code = packCodeResult.packCode,
                                name = raw.name,
                                quantity = enteredWeight.value ?: 0.0
                        )
                )

                prepareToNext()
                printTag()
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    private fun prepareToNext() {
        raw.totalQuantity = totalWeight.value ?: 0.0
        weight.value = ""
    }

    private fun printTag() {
        // todo Реализовать печать штрих-кода тары

    }

}