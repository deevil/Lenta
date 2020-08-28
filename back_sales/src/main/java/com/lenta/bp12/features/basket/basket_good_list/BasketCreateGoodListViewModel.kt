package com.lenta.bp12.features.basket.basket_good_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class BasketCreateGoodListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var createTaskManager: ICreateTaskManager

    @Inject
    lateinit var resource: IResourceManager

    val selectionsHelper = SelectionItemsHelper()

    private val task by lazy {
        createTaskManager.currentTask
    }

    val basket: LiveData<Basket> by lazy {
        createTaskManager.currentBasket
    }

    val title by lazy {
        basket.map { basket ->
            val position = createTaskManager.getBasketPosition(basket)
            val description = basket?.getDescription(task.value?.type?.isDivBySection ?: false)
            resource.basket("$position: $description")
        }
    }

    private val isWholesaleBasket by unsafeLazy {
        createTaskManager.isWholesaleTaskType
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val goods by lazy {
        Logg.e { basket.value.toString() }
        basket.map {
            it?.let { basket ->
                val list = basket.getGoodList()
                list.mapIndexed { index, good ->
                    val units = good.commonUnits.name
                    val quantity = basket.goods[good]

                    Logg.e { "freeVolume: ${basket.freeVolume}, isPrinted: ${basket.isPrinted}, isLocked: ${basket.isLocked} ${basket.goods}" }

                    ItemGoodUi(
                            position = "${index + 1}",
                            name = good.getNameWithMaterial(),
                            quantity = "${quantity.dropZeros()} $units",
                            material = good.material,
                            good = good
                    )
                }
            }.orEmpty()
        }
    }

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val deleteEnabled = selectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    val isCloseBtnEnabled by unsafeLazy {
        basket.switchMap {
            liveData {
                val isCloseBtnEnabled = it.isLocked.not()
                emit(isCloseBtnEnabled)
            }
        }
    }

    val isOpenBtnEnabled by unsafeLazy {
        basket.switchMap {
            liveData {
                val isCloseBtnEnabled = it.isLocked
                emit(isCloseBtnEnabled)
            }
        }
    }

    val isCloseBtnVisible by unsafeLazy {
        MutableLiveData(isWholesaleBasket)
    }

    val isOpenBtnVisible by unsafeLazy {
        MutableLiveData(isWholesaleBasket)
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
                createTaskManager.searchNumber = number
                createTaskManager.isSearchFromList = true
                navigator.goBack()
                navigator.openGoodInfoCreateScreen()
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        goods.value?.get(position)?.let{ item ->
            item.material.let { material ->
                createTaskManager.searchNumber = material
                createTaskManager.isSearchFromList = true
                navigator.goBack()
                if (item.good.markType == MarkType.UNKNOWN)
                    navigator.openGoodInfoCreateScreen()
                else navigator.openMarkedGoodInfoCreateScreen(listOf(), listOf())
            }
        }
    }

    fun onClickNext() {
        navigator.goBack()
    }

    fun onClickProperties() {
        navigator.openBasketPropertiesScreen()
    }

    fun onClickDelete() {
        val materials = getSelectedGoodsNumbers()
        deleteGoodsFromBasketAndTask(materials)
    }

    private fun getSelectedGoodsNumbers(): List<String> {
        val materials = mutableListOf<String>()
        selectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
            goods.value?.getOrNull(position)?.material
        }
        selectionsHelper.clearPositions()
        return materials
    }

    private fun deleteGoodsFromBasketAndTask(materials: List<String>) {
        task.value?.let { task ->
            basket.value?.let { basket ->
                val basketIndex = basket.index
                // Пройдемся по всем номерам товара которые нужно удалить
                materials.forEach { goodMaterial ->
                    // Найдем товары в корзине которые нужно удалить
                    val goodToDeleteFromBasket = basket.goods.keys.firstOrNull { it.material == goodMaterial }
                    goodToDeleteFromBasket?.let { goodFromBasket ->
                        //Найдем этот товар в общем списке задания
                        task.goods.firstOrNull { it.material == goodMaterial }?.let { goodFromTask ->
                            //Удалим у этого товара марки и партии с номером корзины
                            goodFromTask.removeMarksByBasketIndex(basketIndex)
                            goodFromTask.removePartsByBasketNumber(basketIndex)
                            goodFromTask.removePositionsByBasketIndex(basketIndex)
                            //Найдем у этого товара позиции с подходящим количеством
                            goodFromTask.deletePositionsFromTask(
                                    goodFromBasket = goodFromBasket,
                                    basketToGetQuantity = basket)
                        }
                        //Удалим товар из корзины
                        basket.deleteGood(goodFromBasket)
                    }
                }
                removeEmptyBasketsAndGoods(task, basket)

                createTaskManager.updateCurrentBasket(basket)
                createTaskManager.updateCurrentTask(task)
            }
        }
    }

    private fun removeEmptyBasketsAndGoods(task: TaskCreate, basket: Basket) {
        task.removeEmptyGoods()
        //Если корзина пуста удалим ее из задания и вернемся назад
        if (basket.goods.isEmpty()) {
            task.removeEmptyBaskets()
            navigator.goBack()
        }
    }

    fun onClickClose() {
        navigator.showCloseBasketDialog(
                yesCallback = {
                    lockAndUpdateBasketAndTask(isNeedLock = true)
                })
    }

    fun onClickOpen() {
        navigator.showOpenBasketDialog(
                yesCallback = {
                    lockAndUpdateBasketAndTask(isNeedLock = false)
                })
    }

    private fun lockAndUpdateBasketAndTask(isNeedLock: Boolean) {
        val taskValue = task.value
        val basketValue = basket.value
        if (taskValue == null && basketValue != null) {
            basketValue.isLocked = isNeedLock
            taskValue?.updateBasket(basketValue)
            with(createTaskManager) {
                updateCurrentBasket(basketValue)
                updateCurrentTask(taskValue)
            }
            navigator.goBack()
        }

    }
}