package com.lenta.bp12.features.basket_good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class BasketGoodListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager


    val selectionsHelper = SelectionItemsHelper()

    val task by lazy {
        manager.task
    }

    val basket by lazy {
        manager.currentBasket
    }

    val title = basket.map { basket ->
        val position = task.value?.baskets?.indexOf(basket) ?: 0 + 1
        val number = if (position.toString().length == 1) "0$position" else "$position"
        val type = basket?.type
        val control = basket?.control?.code
        val provider = basket?.provider?.code

        "Корзина ${position}: C-${number}/${type}/${control}/ПП-${provider}"
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val goods = basket.map { basket ->
        task.value?.let { task ->
            task.goods.filter {
                basket?.section == it.section && basket.type == it.type && basket.control == it.control && basket.provider == it.provider
            }.mapIndexed { index, good ->
                ItemGoodUi(
                        position = "${index + 1}",
                        name = good.getNameWithMaterial(),
                        quantity = good.quantity.dropZeros(),
                        material = good.material
                )
            }
        }
    }

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val deleteEnabled = MutableLiveData(false)

    // -----------------------------

    fun onClickDelete() {

    }

    fun onClickProperties() {
        navigator.openBasketPropertiesScreen()
    }

    fun onClickNext() {
        navigator.goBackTo("TaskCompositionFragment")
    }

    fun onClickItemPosition(position: Int) {
        manager.searchNumber = goods.value!![position].material
        manager.searchFromList = true
        navigator.openGoodInfoScreen()
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
            if (length >= Constants.SAP_6) {
                manager.searchNumber = number
                manager.searchFromList = true
                navigator.openGoodInfoScreen()
            }
        }
    }

}

data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: String,
        val material: String
)