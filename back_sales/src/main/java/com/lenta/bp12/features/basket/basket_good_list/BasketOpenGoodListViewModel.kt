package com.lenta.bp12.features.basket.basket_good_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.open_task.base.BaseGoodListOpenViewModel
import com.lenta.bp12.features.open_task.good_list.GoodListFragment
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.asyncTryCatchLiveData
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class BasketOpenGoodListViewModel : BaseGoodListOpenViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    override lateinit var manager: IOpenTaskManager

    val selectionsHelper = SelectionItemsHelper()

    val basket: LiveData<Basket> by lazy {
        manager.currentBasket
    }

    val title by lazy {
        basket.map { basket ->
            val position = basket?.index ?: 1
            val description = basket?.getDescription(
                    isDivBySection = task.value?.type?.isDivBySection ?: false,
                    isWholeSale = manager.isWholesaleTaskType
            )
            resource.basket("$position: $description")
        }
    }

    private val isWholesaleBasket by unsafeLazy {
        manager.isWholesaleTaskType
    }

    val goods by lazy {
        basket.switchMap { activeBasket ->
            task.switchMap { task ->
                asyncTryCatchLiveData {
                    val list = activeBasket.getGoodList()
                    list.mapIndexed { index, good ->
                        val units = good.commonUnits.name
                        val quantity = activeBasket.goods[good]

                        Logg.d { "-> freeVolume: ${activeBasket.freeVolume}, isPrinted: ${activeBasket.isPrinted}, isLocked: ${activeBasket.isLocked} ${activeBasket.goods}" }

                        ItemGoodUi(
                                position = "${index + 1}",
                                name = good.getNameWithMaterial(),
                                mrc = getMrc(good, task),
                                quantity = "${quantity.dropZeros()} $units",
                                material = good.material,
                                good = good
                        )
                    }
                }
            }
        }
    }

    val deleteEnabled = selectionsHelper.selectedPositions.map {
        it?.isNotEmpty() == true
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

    override fun onOkInSoftKeyboard(): Boolean {
        checkSearchNumber(numberField.value.orEmpty())
        return true
    }

    fun onClickItemPosition(position: Int) {
        goods.value?.let {
            it.getOrNull(position)?.let { item ->
                manager.updateCurrentGood(item.good)
                if (item.good.markType == MarkType.UNKNOWN)
                    navigator.openGoodInfoOpenScreen()
                else navigator.openMarkedGoodInfoOpenScreen()
            }
        }.orIfNull {
            Logg.e { "goods null" }
            navigator.showInternalError(resource.goodsNotFoundErrorMsg)
        }
    }

    fun onClickNext() {
        navigator.goBackTo(GoodListFragment::class.simpleName)
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
                // Найдем товары в корзине которые нужно удалить
                val goodsToDeleteFromBasket = basket.goods.keys.filter { materials.contains(it.material) }
                goodsToDeleteFromBasket.forEach { goodFromBasket ->
                    goodFromBasket.removePartsMarksPositionsByBasketIndex(basketIndex)
                    //Найдем у этого товара позиции с подходящим количеством
                    goodFromBasket.deletePositionsFromTask(
                            goodFromBasket = goodFromBasket,
                            basketToGetQuantity = basket
                    )
                    if (goodFromBasket.getTotalQuantity() == ZERO_QUANTITY) {
                        goodFromBasket.isCounted = false
                    }
                    //Удалим товар из корзины
                    basket.deleteGood(goodFromBasket)
                }
                removeEmptyBaskets(task, basket)
                manager.updateBasketAndTask(task, basket)
            }.orIfNull {
                Logg.e { "basket null" }
                navigator.showInternalError(resource.basketNotFoundErrorMsg)
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    private fun IOpenTaskManager.updateBasketAndTask(task: TaskOpen, basket: Basket) {
        updateCurrentBasket(basket)
        updateCurrentTask(task)
    }

    private fun removeEmptyBaskets(task: TaskOpen, basket: Basket) {
        //Если корзина пуста удалим ее из задания и вернемся назад
        if (basket.goods.isEmpty()) {
            task.removeEmptyBaskets()
            navigator.goBackTo(GoodListFragment::class.simpleName)
        }
    }

    fun onClickClose() {
        navigator.showCloseBasketDialog(
                yesCallback = {
                    handleYesOnOpenCloseBasket(true)
                }
        )
    }

    fun onClickOpen() {
        navigator.showOpenBasketDialog(
                yesCallback = {
                    handleYesOnOpenCloseBasket(false)
                }
        )
    }

    private fun handleYesOnOpenCloseBasket(isNeedLock: Boolean) {
        task.value?.let { task ->
            basket.value?.let { basket ->
                lockAndUpdateBasketAndTask<GoodListFragment, BasketOpenGoodListFragment>(
                        isNeedLock = isNeedLock,
                        task = task,
                        basket = basket
                )
            }.orIfNull {
                Logg.e { "basket null" }
                navigator.showInternalError(resource.basketNotFoundErrorMsg)
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }
}