package com.lenta.bp12.features.basket.basket_good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.create_task.base.BaseGoodListCreateViewModel
import com.lenta.bp12.features.create_task.task_content.TaskContentFragment
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.asyncTryCatchLiveData
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class BasketCreateGoodListViewModel : BaseGoodListCreateViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    override lateinit var manager: ICreateTaskManager

    val selectionsHelper = SelectionItemsHelper()

    val basket by unsafeLazy {
        manager.currentBasket
    }

    val title by unsafeLazy {
        basket.map { basket ->
            val position = basket.getPosition()
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

    val goods by unsafeLazy {
        basket.switchMap { basket ->
            task.switchMap { task ->
                asyncTryCatchLiveData {
                    val list = basket.getGoodList()
                    list.mapIndexed { index, good ->
                        val units = good.commonUnits.name
                        val quantity = basket.goods[good]
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

    fun onClickItemPosition(position: Int) {
        goods.value?.let { goods ->
            goods.getOrNull(position)?.let { item ->
                val good = item.good
                manager.updateCurrentGood(good)
                if (good.isMarked()) {
                    navigator.openMarkedGoodInfoCreateScreen()
                } else {
                    navigator.openGoodInfoCreateScreen()
                }
            }
        }.orIfNull {
            Logg.e { "goods null" }
            navigator.showInternalError(resource.goodsNotFoundErrorMsg)
        }
    }

    fun onClickNext() {
        navigator.goBackTo(TaskContentFragment::class.simpleName)
    }

    fun onClickClose() {
        navigator.showCloseBasketDialog(
                yesCallback = { handleYesOnOpenCloseBasket(true) }
        )
    }

    fun onClickOpen() {
        navigator.showOpenBasketDialog(
                yesCallback = { handleYesOnOpenCloseBasket(false) }
        )
    }

    private fun handleYesOnOpenCloseBasket(isNeedLock: Boolean) {
        task.value?.let { task ->
            basket.value?.let { basket ->
                lockAndUpdateBasketAndTask<TaskContentFragment, BasketCreateGoodListFragment>(
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

    override fun onClickDelete() {
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
                    //Удалим товар из корзины
                    basket.deleteGood(goodFromBasket)
                }
                removeEmptyBasketsAndGoods(task, basket)
                manager.updateCurrentBasket(basket)
                manager.updateCurrentTask(task)
            }.orIfNull {
                Logg.e { "basket null" }
                navigator.showInternalError(resource.basketNotFoundErrorMsg)
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    private fun removeEmptyBasketsAndGoods(task: TaskCreate, basket: Basket) {
        task.removeEmptyGoods()
        //Если корзина пуста удалим ее из задания и вернемся назад
        if (basket.goods.isEmpty()) {
            task.removeEmptyBaskets()
            navigator.goBackTo(TaskContentFragment::class.simpleName)
        }
    }


}