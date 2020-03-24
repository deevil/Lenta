package com.lenta.bp12.features.task_composition

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
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

    private val task by lazy {
        manager.task
    }

    val title = task.map { task ->
        "${task?.type?.type} // ${task?.name}"
    }

    val deleteEnabled = MutableLiveData(false)

    val saveEnabled = MutableLiveData(false)

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val goods = task.map { task ->
        task?.goods!!.reversed().mapIndexed { index, good ->
            ItemGoodUi(
                    material = good.material,
                    position = "${task.goods.size - index}",
                    name = good.getNameWithMaterial(),
                    quantity = good.quantity.dropZeros()
            )
        }
    }

    val baskets = task.map { task ->
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
                    manager.searchFromList = true
                    navigator.openGoodInfoScreen()
                }
                1 -> {
                    manager.currentBasket.value = baskets.value!![position].basket
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
                manager.searchFromList = true
                navigator.openGoodInfoScreen()
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