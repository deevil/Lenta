package com.lenta.bp12.features.create_task.task_composition

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.isCommonFormatNumber
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

    @Inject
    lateinit var resource: IResourceManager


    /**
    Переменные
     */

    val goodSelectionsHelper = SelectionItemsHelper()

    val basketSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    private val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { task ->
            task?.getFormattedName()
        }
    }

    val numberField = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val goods by lazy {
        task.map {
            it?.let { task ->
                task.goods.mapIndexed { index, good ->
                    ItemGoodUi(
                            material = good.material,
                            position = "${task.goods.size - index}",
                            name = good.getNameWithMaterial(),
                            quantity = "${good.getTotalQuantity().dropZeros()} ${good.commonUnits.name}"
                    )
                }
            }
        }
    }

    val baskets by lazy {
        task.map {
            it?.let { task ->
                task.baskets.reversed().mapIndexed { index, basket ->
                    val position = task.baskets.size - index
                    ItemBasketUi(
                            basket = basket,
                            position = "$position",
                            name = resource.basket("$position"),
                            description = basket.getDescription(task.type.isDivBySection),
                            quantity = "${task.getCountByBasket(basket)}"
                    )
                }
            }
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val deleteEnabled = selectedPage.combineLatest(goodSelectionsHelper.selectedPositions).combineLatest(basketSelectionsHelper.selectedPositions).map {
        val tab = it!!.first.first
        val isGoodSelected = it.first.second.isNotEmpty()
        val isBasketSelected = it.second.isNotEmpty()

        tab == 0 && isGoodSelected || tab == 1 && isBasketSelected
    }

    val printVisibility = selectedPage.map { tab ->
        tab == 1
    }

    val saveEnabled by lazy {
        goods.map {
            it?.isNotEmpty()
        }
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onScanResult(data: String) {
        openGoodInfoByNumber(data)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        openGoodInfoByNumber(numberField.value.orEmpty())
        return true
    }

    private fun openGoodInfoByNumber(number: String) {
        numberField.value = ""

        if (isCommonFormatNumber(number)) {
            manager.searchNumber = number
            manager.isSearchFromList = true
            navigator.openGoodInfoCreateScreen()
        } else {
            navigator.showIncorrectEanFormat()
        }
    }

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    manager.searchNumber = goods.value!![position].material
                    manager.isSearchFromList = true
                    navigator.openGoodInfoCreateScreen()
                }
                1 -> {
                    manager.updateCurrentBasket(baskets.value!![position].basket)
                    navigator.openBasketGoodListScreen()
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    val materials = mutableListOf<String>()
                    goodSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                        goods.value?.get(position)?.material
                    }

                    goodSelectionsHelper.clearPositions()
                    manager.removeGoodByMaterials(materials)
                }
                1 -> {
                    val basketList = mutableListOf<Basket>()
                    basketSelectionsHelper.selectedPositions.value?.mapNotNullTo(basketList) { position ->
                        baskets.value?.get(position)?.basket
                    }

                    basketSelectionsHelper.clearPositions()
                    manager.removeBaskets(basketList)
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    fun onClickSave() {
        navigator.showMakeTaskCountedAndClose {
            manager.prepareSendTaskDataParams(
                    deviceIp = deviceInfo.getDeviceIp(),
                    tkNumber = sessionInfo.market.orEmpty(),
                    userNumber = sessionInfo.personnelNumber.orEmpty()
            )

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