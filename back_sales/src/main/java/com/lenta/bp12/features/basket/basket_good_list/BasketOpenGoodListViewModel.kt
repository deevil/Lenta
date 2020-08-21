package com.lenta.bp12.features.basket.basket_good_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.platform.extention.deleteGood
import com.lenta.bp12.platform.extention.getDescription
import com.lenta.bp12.platform.extention.getGoodList
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

class BasketOpenGoodListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var openTaskManager: IOpenTaskManager

    @Inject
    lateinit var resource: IResourceManager


    val selectionsHelper = SelectionItemsHelper()

    private val task by lazy {
        openTaskManager.currentTask
    }

    val basket: LiveData<Basket> by lazy {
        openTaskManager.currentBasket
    }

    val title by lazy {
        basket.map { basket ->
            val position = basket?.index ?: 1

            val description = basket?.getDescription(task.value?.type?.isDivBySection ?: false)
            resource.basket("$position: $description")
        }
    }


    private val isWholesaleBasket by unsafeLazy {
        openTaskManager.isWholesaleTaskType
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
                openTaskManager.searchNumber = number
                openTaskManager.isSearchFromList = true
                navigator.goBack()
                navigator.openGoodInfoCreateScreen()
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        goods.value?.get(position)?.material?.let { material ->
            openTaskManager.searchNumber = material
            openTaskManager.isSearchFromList = true
            navigator.goBack()
            navigator.openGoodInfoOpenScreen()
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
                    goodToDeleteFromBasket?.let { good ->
                        //Найдем этот товар в общем списке задания
                        task.goods.firstOrNull { it.material == goodMaterial }?.let { goodFromTask ->
                            //Удалим у этого товара марки и партии с номером корзины
                            goodFromTask.removeMarksByBasketIndex(basketIndex)
                            goodFromTask.removePartsByBasketNumber(basketIndex)
                            goodFromTask.removePositionsByBasketIndex(basketIndex)
                            //Найдем у этого товара позиции с подходящим количеством
                            val positionThatFits = goodFromTask.positions.firstOrNull { positionFromTask ->
                                good.positions.any { it.quantity >= positionFromTask.quantity }
                            }

                            positionThatFits?.let {
                                //Получим количество позиций этого товара
                                val quantityOfPositionFromTask = it.quantity
                                //Получим количество удаляемого товара из корзины
                                val quantityToMinus = basket.goods[good] ?: 0.0
                                //Отнимем первое от второго и вернем в товар
                                val newQuantity = quantityOfPositionFromTask.minus(quantityToMinus)
                                it.quantity = newQuantity
                                val index = goodFromTask.positions.indexOf(it)
                                goodFromTask.positions.set(index, it)
                            }
                        }
                        //Удалим товар из корзины
                        basket.deleteGood(good)
                    }
                }
                //Если корзина пуста удалим ее из задания и вернемся назад
                if (basket.goods.isEmpty()) {
                    task.removeEmptyBaskets()
                    navigator.goBack()
                }
                task.removeEmptyGoods()
                openTaskManager.updateCurrentBasket(basket)
                openTaskManager.updateCurrentTask(task)
            }
        }

    }

    fun onClickClose() {
        navigator.showCloseBasketDialog(yesCallback = {
            task.value?.let { task ->
                basket.value?.let { basket ->
                    basket.isLocked = true
                    task.updateBasket(basket)
                    openTaskManager.updateCurrentBasket(basket)
                    openTaskManager.updateCurrentTask(task)
                }

            }
            navigator.goBack()
        })
    }

    fun onClickOpen() {
        navigator.showOpenBasketDialog(yesCallback = {
            task.value?.let { task ->
                basket.value?.let { basket ->
                    basket.isLocked = false
                    task.updateBasket(basket)
                    openTaskManager.updateCurrentBasket(basket)
                    openTaskManager.updateCurrentTask(task)
                }
            }
            navigator.goBack()
        })
    }
}