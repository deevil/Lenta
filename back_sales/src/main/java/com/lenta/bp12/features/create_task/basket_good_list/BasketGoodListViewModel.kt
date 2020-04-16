package com.lenta.bp12.features.create_task.basket_good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ITaskManager
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
    lateinit var manager: ITaskManager


    val selectionsHelper = SelectionItemsHelper()

    private val task by lazy {
        manager.currentTask
    }

    val basket by lazy {
        manager.currentBasket
    }

    val title by lazy {
        basket.map { basket ->
            "Корзина ${manager.getBasketPosition(basket)}: ${basket?.getDescription()}"
        }
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val goods by lazy {
        basket.map { basket ->
            task.value?.let { task ->
                task.goods.filter {
                    it.section == basket?.section && it.type == basket.type && it.control == basket.control
                }.mapIndexed { index, good ->
                    ItemGoodUi(
                            position = "${index + 1}",
                            name = good.getNameWithMaterial(),
                            quantity = good.getQuantityByProvider(basket?.provider).dropZeros(),
                            material = good.material
                    )
                }
            }
        }
    }

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val deleteEnabled = selectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    // -----------------------------

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
                manager.openExistGood = true
                navigator.goBack()
                navigator.openGoodInfoCreateScreen()
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        manager.searchNumber = goods.value!![position].material
        manager.openExistGood = true
        navigator.openGoodInfoCreateScreen()
    }

    fun onClickNext() {
        navigator.goBack()
    }

    fun onClickProperties() {
        navigator.openBasketPropertiesScreen()
    }

    fun onClickDelete() {
        val materialList = mutableListOf<String>()
        selectionsHelper.selectedPositions.value?.map { position ->
            goods.value?.get(position)?.material?.let {
                materialList.add(it)
            }
        }

        selectionsHelper.clearPositions()
        manager.deleteGoodByMaterials(materialList)
        manager.updateCurrentBasket(manager.currentBasket.value)
    }

}

data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: String,
        val material: String
)