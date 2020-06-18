package com.lenta.bp12.features.create_task.task_composition

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class TaskCompositionViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var deviceInfo: DeviceInfo


    val goodSelectionsHelper = SelectionItemsHelper()

    val basketSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    private val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { task ->
            "${task?.properties?.type} // ${task?.name}"
        }
    }

    val numberField = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val goods by lazy {
        task.map { task ->
            task?.goods!!.reversed().mapIndexed { index, good ->
                ItemGoodUi(
                        material = good.material,
                        position = "${task.goods.size - index}",
                        name = good.getNameWithMaterial(),
                        quantity = "${good.getPositionQuantity().dropZeros()} ${good.units.name}"
                )
            }
        }
    }

    val baskets by lazy {
        task.map { task ->
            task?.baskets!!.reversed().mapIndexed { index, basket ->
                val position = task.baskets.size - index
                ItemBasketUi(
                        basket = basket,
                        position = "$position",
                        name = "Корзина $position",
                        description = basket.getDescription(),
                        quantity = task.getQuantityByBasket(basket).dropZeros()
                )
            }
        }
    }

    val deleteEnabled = selectedPage.combineLatest(goodSelectionsHelper.selectedPositions).combineLatest(basketSelectionsHelper.selectedPositions).map {
        val tab = it!!.first.first
        val isGoodSelected = it.first.second.isNotEmpty()
        val isBasketSelected = it.second.isNotEmpty()

        tab == 0 && isGoodSelected || tab == 1 && isBasketSelected
    }

    val saveEnabled by lazy {
        goods.map {
            it?.isNotEmpty()
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onScanResult(data: String) {
        checkEnteredNumber(data)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(numberField.value ?: "")
        return true
    }

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    manager.searchNumber = goods.value!![position].material
                    manager.openGoodFromList = true
                    navigator.openGoodInfoCreateScreen()
                }
                1 -> {
                    manager.updateCurrentBasket(baskets.value!![position].basket)
                    navigator.openBasketPropertiesScreen()
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    private fun checkEnteredNumber(number: String) {
        number.length.let { length ->
            if (length >= Constants.SAP_6) {
                manager.searchNumber = number
                manager.openGoodFromList = true
                numberField.value = ""
                navigator.openGoodInfoCreateScreen()
            }
        }
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    val materialList = mutableListOf<String>()
                    goodSelectionsHelper.selectedPositions.value?.map { position ->
                        goods.value?.get(position)?.material?.let {
                            materialList.add(it)
                        }
                    }

                    goodSelectionsHelper.clearPositions()
                    manager.deleteGoodByMaterials(materialList)
                }
                1 -> {
                    val basketList = mutableListOf<Basket>()
                    basketSelectionsHelper.selectedPositions.value?.map { position ->
                        baskets.value?.get(position)?.basket?.let {
                            basketList.add(it)
                        }
                    }

                    basketSelectionsHelper.clearPositions()
                    manager.deleteBaskets(basketList)
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    fun onClickSave() {
        navigator.showMakeTaskCountedAndClose {
            manager.prepareSendTaskDataParams(
                    deviceIp = deviceInfo.getDeviceIp(),
                    tkNumber = sessionInfo.market ?: "",
                    userNumber = sessionInfo.personnelNumber ?: ""
            )

            manager.finishCurrentTask()
            navigator.openSaveDataScreen()
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
        val material: String,
        val position: String,
        val name: String,
        val quantity: String
)

data class ItemBasketUi(
        val basket: Basket,
        val position: String,
        val name: String,
        val description: String,
        val quantity: String
)