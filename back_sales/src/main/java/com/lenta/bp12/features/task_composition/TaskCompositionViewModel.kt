package com.lenta.bp12.features.task_composition

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.GoodInfoParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
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
        number.length.let { length ->
            if (length < Constants.SAP_6) {
                return
            }

            if (length >= Constants.SAP_6) {
                when (length) {
                    Constants.SAP_6 -> openGoodByMaterial("000000000000$number")
                    Constants.SAP_18 -> openGoodByMaterial(number)
                    Constants.SAP_OR_BAR_12 -> {
                        navigator.showTwelveCharactersEntered(
                                sapCallback = { openGoodByMaterial(number) },
                                barCallback = { openGoodByEan(number) }
                        )
                    }
                    else -> openGoodByEan(number)
                }
            }
        }
    }

    private fun openGoodByEan(ean: String) {
        if (manager.isExistEan(ean)) {
            navigator.openGoodInfoScreen()
        } else {
            viewModelScope.launch {
                navigator.showProgressLoadingData()

                goodInfoNetRequest(GoodInfoParams(
                        tkNumber = sessionInfo.market ?: "Not found!",
                        ean = ean,
                        taskType = task.value!!.type.type
                )).also {
                    navigator.hideProgress()
                }.either(::handleFailure) { goodInfo ->
                    viewModelScope.launch {
                        manager.addGood(goodInfo)
                        navigator.openGoodInfoScreen()
                    }
                }
            }
        }
    }

    private fun openGoodByMaterial(material: String) {
        if (manager.isExistMaterial(material)) {
            navigator.openGoodInfoScreen()
        } else {
            viewModelScope.launch {
                navigator.showProgressLoadingData()

                goodInfoNetRequest(GoodInfoParams(
                        tkNumber = sessionInfo.market ?: "Not found!",
                        material = material,
                        taskType = task.value!!.type.type
                )).also {
                    navigator.hideProgress()
                }.either(::handleFailure) { goodInfo ->
                    viewModelScope.launch {
                        manager.addGood(goodInfo)
                        navigator.openGoodInfoScreen()
                    }
                }
            }
        }
    }

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