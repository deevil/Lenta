package com.lenta.bp12.features.basket.basket_good_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.platform.extention.deleteGood
import com.lenta.bp12.platform.extention.getDescription
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
import com.lenta.shared.utilities.orIfNull
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

    val basket: LiveData<Basket> by lazy { //вернуть lazy
        manager.currentBasket
    }

    val title by lazy {
        basket.map { basket ->
            val position = manager.getBasketPosition(basket)
            val description = basket?.getDescription(task.value?.type?.isDivBySection ?: false)
            resource.basket("$position: $description")
        }
    }

    private val isWholesaleBasket by unsafeLazy {
        manager.isWholesaleTaskType
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val goods by lazy {
        basket.map {
            it?.let { basket ->
                val list = basket.goods.keys.toList()
                list.mapIndexed { index, good ->
                    val units = good.commonUnits.name
                    val quantity = basket.goods[good]

                    Logg.e { "freeVolume: ${basket.freeVolume}, isPrinted: ${basket.isPrinted}, isLocked: ${basket.isLocked} ${basket.goods}" }

                    ItemGoodUi(
                            position = "${index + 1}",
                            name = good.getNameWithMaterial(),
                            quantity = "${quantity.dropZeros()} $units",
                            material = good.material
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
                manager.searchNumber = number
                manager.isSearchFromList = true
                navigator.goBack()
                navigator.openGoodInfoCreateScreen()
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        goods.value?.get(position)?.material?.let { material ->
            manager.searchNumber = material
            manager.isSearchFromList = true
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
        val materials = mutableListOf<String>()
        selectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
            goods.value?.getOrNull(position)?.material
        }

        selectionsHelper.clearPositions()

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
                            //Найдем у этого товара позиции с подходящим количеством
                            goodFromTask.deletePositionsFromTask(
                                    goodFromBasket = goodFromBasket,
                                    basketToGetQuantity = basket)
                        }
                        //Удалим товар из корзины
                        basket.deleteGood(goodFromBasket)
                    }
                }
                //Если корзина пуста удалим ее из задания и вернемся назад
                if (basket.goods.isEmpty()) {
                    task.removeEmptyBaskets()
                    navigator.goBack()
                }
                task.removeEmptyGoods()
                manager.updateCurrentBasket(basket)
                manager.updateCurrentTask(task)
            }.orIfNull {
                Logg.e { " basket null " }
                navigator.showInternalError(resource.basketNotFoundErrorMsg)
            }
        }.orIfNull {
            Logg.e { " task null " }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    private fun GoodCreate.deletePositionsFromTask(goodFromBasket: GoodCreate, basketToGetQuantity: Basket) {
        val positionThatFits = positions.firstOrNull { positionFromTask ->
            goodFromBasket.positions.any { it.quantity >= positionFromTask.quantity }
        }

        positionThatFits?.let {
            //Получим количество позиций этого товара
            val quantityOfPositionFromTask = it.quantity
            //Получим количество удаляемого товара из корзины
            val quantityToMinus = basketToGetQuantity.goods[goodFromBasket] ?: 0.0
            //Отнимем первое от второго и вернем в товар
            val newQuantity = quantityOfPositionFromTask.minus(quantityToMinus)
            it.quantity = newQuantity
            val index = positions.indexOf(it)
            positions.set(index, it)
        }
    }

    fun onClickClose() {
        navigator.showCloseBasketDialog(::handleYesToCloseBasketCallback)
    }

    private fun handleYesToCloseBasketCallback() {
        task.value?.let { task ->
            basket.value?.let { basket ->
                basket.isLocked = true
                task.updateBasket(basket)
                manager.updateCurrentBasket(basket)
                manager.updateCurrentTask(task)
            }.orIfNull {
                Logg.e { " basket null " }
                navigator.showInternalError(resource.basketNotFoundErrorMsg)
            }
        }.orIfNull {
            Logg.e { " task null " }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
        navigator.goBack()
    }

    fun onClickOpen() {
        navigator.showOpenBasketDialog(::handleYesToOpenBasketCallback)
    }

    private fun handleYesToOpenBasketCallback() {
        task.value?.let { task ->
            basket.value?.let { basket ->
                basket.isLocked = false
                task.updateBasket(basket)
                manager.updateCurrentBasket(basket)
                manager.updateCurrentTask(task)
            }
        }
        navigator.goBack()
    }

}

data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: String,
        val material: String
)