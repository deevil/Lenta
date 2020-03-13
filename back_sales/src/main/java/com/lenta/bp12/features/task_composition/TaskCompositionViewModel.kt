package com.lenta.bp12.features.task_composition

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.GoodInfoParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskCompositionViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo


    val goodSelectionsHelper = SelectionItemsHelper()
    val basketSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val title by lazy {
        "ВПП // Возврат от 10.12.2018 15:20"
    }

    val task by lazy {
        manager.getTask()
    }

    val deleteEnabled = MutableLiveData(false)

    val saveEnabled = MutableLiveData(false)

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val goods by lazy {
        MutableLiveData(List(3) {
            ItemGoodUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}",
                    quantity = (1..15).random().toString()
            )
        })
    }

    val baskets by lazy {
        MutableLiveData(List(3) {
            ItemBasketUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}",
                    description = "Test description ${it + 1}",
                    quantity = (1..15).random().toString()
            )
        })
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {

    }

    fun onClickSave() {

    }

    fun onClickItemPosition(position: Int) {

    }

    fun onScanResult(data: String) {
        checkEnteredNumber(data)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(numberField.value ?: "")
        return true
    }

    private fun checkEnteredNumber(number: String) {
        analyseCode(
                code = number,
                funcForEan = { ean ->
                    getGoodByEan(ean)
                },
                funcForMatNr = { material ->
                    getGoodByMaterial(material)
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = ::notValidNumber
        )
    }

    private fun notValidNumber() {
        Logg.d { "Введенный номер не является корректным!" }
    }

    /*private fun searchCode(ean: String? = null, material: String? = null) {
        viewModelScope.launch {
            require((ean != null) xor (material != null)) {
                "Only one param allowed - ean: $ean, material: $material"
            }

            navigator.showProgressLoadingData()

            when {
                !ean.isNullOrBlank() -> task.getGoodByEan(ean)
                !material.isNullOrBlank() -> task.getGoodByMaterial(material)
                else -> null
            }.also {
                navigator.hideProgress()
            }?.let { good ->
                if (task.getDescription().isStrictList && !task.isGoodFromTask(good)) {
                    navigator.showGoodIsNotPartOfTask()
                } else {
                    task.addGoodToList(good)
                    navigator.openGoodInfoWlScreen()
                }
                return@launch
            }

            navigator.showGoodNotFound()
        }
    }*/

    private fun getGoodByEan(ean: String) {

    }

    private fun getGoodByMaterial(material: String) {
        Logg.d { "getGoodByMaterial - $material" }
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market ?: "Not found!",
                    ean = "",
                    material = material,
                    bpCode = "BKS",
                    taskType = task.value!!.type.type
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { goodInfo ->
                manager.addGood(goodInfo)
                navigator.openGoodInfoScreen()
            }
        }
    }

    /*override fun handleFailure(failure: Failure) {
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)

        if (failure is Failure.SapError) failure.message else resourceManager.serverConnectionError()
    }*/

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    fun onBackPressed() {
        if (task.value!!.goods.isNotEmpty()) {
            navigator.showUnsavedDataWillBeLost {
                navigator.goBack()
            }
        } else {
            navigator.goBack()
        }
    }

}


data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: String
)

data class ItemBasketUi(
        val position: String,
        val name: String,
        val description: String,
        val quantity: String
)