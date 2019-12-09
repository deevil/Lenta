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

    val weight = MutableLiveData("0")

    private val enteredWeight = weight.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    private val totalWeight = enteredWeight.map {
        it.sumWith(raw.quantity)
    }

    val totalWeightWithUnits = totalWeight.map {
        "${it.dropZeros()} ${good.units.name}"
    }

    val planned by lazy {
        "${raw.planned} ${good.units.name}"
    }

    private var isComplete = false

    val completeEnabled: MutableLiveData<Boolean> = enteredWeight.map {
        it ?: 0.0 != 0.0
    }

    val addEnabled: MutableLiveData<Boolean> = enteredWeight.map {
        it ?: 0.0 != 0.0
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
        isComplete = true
        createPack()
    }

    private fun createPack() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            packCodeNetRequest(
                    PackCodeParams(
                            marketNumber = sessionInfo.market ?: "Not found!",
                            parentType = 1,
                            parent = taskManager.currentTask.taskInfo.number,
                            deviceIp = deviceIp.value ?: "Not found!",
                            material = good.material,
                            orderNumber = raw.orderNumber,
                            quantity = enteredWeight.value ?: 0.0
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { packCodeResult ->
                good.packs.add(
                        Pack(
                                material = good.material,
                                materialOsn = raw.materialOsn,
                                code = packCodeResult.packCode,
                                quantity = enteredWeight.value ?: 0.0
                        )
                )

                prepareToNext()
                printTag()

                if (isComplete) {
                    isComplete = false
                    navigator.openPackListScreen()
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    private fun prepareToNext() {
        raw.quantity = totalWeight.value ?: 0.0
        weight.value = "0"
    }

    private fun printTag() {
        // todo Реализовать печать штрих-кода тары

    }

}