package com.lenta.bp12.features.create_task.basket_good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
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

    @Inject
    lateinit var resource: IResourceManager


    val selectionsHelper = SelectionItemsHelper()

    private val task by lazy {
        manager.currentTask
    }

    val basket by lazy {
        manager.currentBasket
    }

    val title by lazy {
        basket.map { basket ->
            val position = manager.getBasketPosition(basket)
            val description = basket?.getDescription(task.value?.taskType?.isDivBySection ?: false)
            resource.basket("$position: $description")
        }
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val goods by lazy {
        basket.map {
            it?.let { basket ->
                task.value?.let { task ->
                    task.getGoodListByBasket(basket).mapIndexed { index, good ->
                        val quantity = good.getQuantityByProvider(basket.provider.code).dropZeros()
                        val units = good.commonUnits.name

                        ItemGoodUi(
                                position = "${index + 1}",
                                name = good.getNameWithMaterial(),
                                quantity = "$quantity $units",
                                material = good.material
                        )
                    }
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
        checkEnteredNumber(numberField.value.orEmpty())
        return true
    }

    private fun checkEnteredNumber(number: String) {
        number.length.let { length ->
            if (length >= Constants.SAP_6) {
                manager.searchNumber = number
                manager.searchGoodFromList = true
                navigator.goBack()
                navigator.openGoodInfoCreateScreen()
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        goods.value?.getOrNull(position)?.material?.let { material ->
            manager.searchNumber = material
            manager.searchGoodFromList = true
            navigator.goBack()
            navigator.openGoodInfoCreateScreen()
        }
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

        basket.value?.let { basket ->
            manager.removeGoodByBasketAndMaterials(basket, materialList)
            manager.updateCurrentBasket(manager.currentBasket.value)

            task.value?.let { task ->
                if (task.isExistBasket(basket)) {
                    manager.updateCurrentBasket(basket)
                } else {
                    navigator.goBack()
                }
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