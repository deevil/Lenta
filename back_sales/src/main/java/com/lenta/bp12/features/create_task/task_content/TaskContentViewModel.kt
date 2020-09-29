package com.lenta.bp12.features.create_task.task_content

import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.basket.ItemWholesaleBasketUi
import com.lenta.bp12.features.create_task.base.BaseGoodListCreateViewModel
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.model.pojo.extentions.getDescription
import com.lenta.bp12.model.pojo.extentions.isAnyNotLocked
import com.lenta.bp12.model.pojo.extentions.isAnyPrinted
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

/** Создание заданий
 * ViewModel ответственный за экран показывающий список товаров и корзин в задании
 * Симетричный класс в Работе с заданиями:
 * @see com.lenta.bp12.features.open_task.good_list.GoodListViewModel
 * */
class TaskContentViewModel : BaseGoodListCreateViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    override lateinit var manager: ICreateTaskManager

    /**
    Переменные
     */

    val goodSelectionsHelper = SelectionItemsHelper()

    val title by lazy {
        task.map { task ->
            task?.getFormattedName()
        }
    }

    val goods by lazy {
        task.switchMap { task ->
            asyncLiveData<List<ItemGoodUi>> {
                val distinctList = task.goods.distinctBy { it.ean }
                val list = distinctList.mapIndexed { index, good ->
                    ItemGoodUi(
                            material = good.material,
                            position = "${distinctList.size - index}",
                            name = good.getNameWithMaterial(),
                            quantity = "${good.getTotalQuantity().dropZeros()} ${good.commonUnits.name}",
                            markType = good.markType,
                            good = good
                    )
                }
                emit(list)
            }
        }
    }

    val commonBaskets by lazy {
        task.switchMap { task ->
            asyncLiveData<List<ItemCommonBasketUi>> {
                val list = task.baskets.reversed().mapIndexed { index, basket ->
                    val position = task.baskets.size - index
                    ItemCommonBasketUi(
                            basket = basket,
                            position = "$position",
                            name = resource.basket("${basket.index}"),
                            description = basket.getDescription(
                                    isDivBySection = task.type.isDivBySection,
                                    isWholeSale = false
                            ),
                            quantity = "${task.getCountByBasket(basket)}"
                    )
                }
                emit(list)
            }
        }
    }

    val wholesaleBaskets by lazy {
        task.switchMap { task ->
            asyncLiveData<List<ItemWholesaleBasketUi>> {
                val list = task.baskets.reversed().mapIndexed { index, basket ->
                    val position = task.baskets.size - index
                    ItemWholesaleBasketUi(
                            basket = basket,
                            position = "$position",
                            name = resource.basket("${basket.index}"),
                            description = basket.getDescription(
                                    isDivBySection = task.type.isDivBySection,
                                    isWholeSale = true
                            ),
                            quantity = "${task.getCountByBasket(basket)} ${Uom.ST.name}",
                            isPrinted = basket.isPrinted,
                            isLocked = basket.isLocked
                    )
                }
                emit(list)
            }
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val deleteEnabled by unsafeLazy {
        selectedPage.switchMap { tab ->
            goodSelectionsHelper.selectedPositions.switchMap { selectedGoods ->
                basketSelectionsHelper.selectedPositions.switchMap { selectedBaskets ->
                    liveData {
                        val isGoodSelected = selectedGoods.isNotEmpty()
                        val isBasketSelected = selectedBaskets.isNotEmpty()
                        val result = (tab == GOOD_TAB_INDEX && isGoodSelected) || (tab == BASKET_TAB_INDEX && isBasketSelected)
                        emit(result)
                    }
                }
            }
        }
    }

    val printVisibility by lazy {
        selectedPage.map { tab ->
            manager.isWholesaleTaskType && tab == BASKET_TAB_INDEX
        }
    }

    val printEnabled by lazy {
        wholesaleBaskets.map {
            it?.isNotEmpty()
        }
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

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                GOOD_TAB_INDEX -> handleItemClickGoodTab(position)
                BASKET_TAB_INDEX -> handleItemClickBasketTab(position)
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    private fun handleItemClickGoodTab(position: Int) {
        val goodItem = goods.value?.getOrNull(position)
        goodItem?.let {
            val good = goodItem.good
            manager.updateCurrentGood(good)
            if (good.isMarked()) {
                navigator.openMarkedGoodInfoCreateScreen()
            } else {
                navigator.openGoodInfoCreateScreen()
            }
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private fun handleItemClickBasketTab(position: Int) {
        val basket = if (manager.isWholesaleTaskType) {
            wholesaleBaskets.value?.getOrNull(position)?.basket
        } else {
            commonBaskets.value?.getOrNull(position)?.basket
        }
        manager.updateCurrentBasket(basket)
        navigator.openBasketCreateGoodListScreen()
    }

    override fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                GOOD_TAB_INDEX -> handleDeleteItemGoodTab()
                BASKET_TAB_INDEX -> handleDeleteItemBasketTab()
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    private fun handleDeleteItemGoodTab() {
        val materials = mutableListOf<String>()
        goodSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
            goods.value?.get(position)?.material
        }

        goodSelectionsHelper.clearPositions()
        manager.removeGoodByMaterials(materials)
    }

    private fun handleDeleteItemBasketTab() {
        val basketList = mutableListOf<Basket>()
        basketSelectionsHelper.selectedPositions.value?.mapNotNullTo(basketList) { position ->
            if (manager.isWholesaleTaskType) {
                wholesaleBaskets.value?.get(position)?.basket
            } else {
                commonBaskets.value?.get(position)?.basket
            }
        }

        basketSelectionsHelper.clearPositions()
        manager.removeBaskets(basketList)
    }

    fun onPrint() {
        task.value?.let { taskValue ->
            goodSelectionsHelper.selectedPositions.value?.let { positions ->
                val taskValueBaskets = taskValue.baskets
                val baskets = positions.takeIf { it.isNotEmpty() }
                        ?.mapNotNullTo(mutableListOf()) {
                            taskValueBaskets.getOrNull(it)
                        }.orIfNull { taskValueBaskets }
                printBaskets(baskets)
            }
        }
    }

    private fun printBaskets(baskets: List<Basket>) {
        //Если какие-то корзины не закрыты
        if (baskets.isAnyNotLocked()) {
            // Вывести экран сообщения «Некоторые выбранные корзины не закрыты. Закройте корзины и повторите печать», с кнопкой «Назад»
            navigator.showSomeOfChosenBasketsNotClosedScreen()
        } else {
            //Если какие-то корзины напечатаны
            if (baskets.isAnyPrinted()) {
                // «По некоторым выделенным корзинам уже производилась печать. Продолжить?», с кнопками «Да», «Назад» (макеты, экран №81)
                navigator.showSomeBasketsAlreadyPrinted(
                        yesCallback = { printPalletList(baskets) }
                )
            } else {
                printPalletList(baskets)
            }
        }
    }

    // TODO Функция не проверена (13.08.2020 САП еще не создан)
    private fun printPalletList(baskets: List<Basket>) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val isDivBySection = task.value?.type?.isDivBySection ?: false
            printPalletListNetRequest(
                    Triple(baskets, isDivBySection, manager.isWholesaleTaskType)
            ).either(
                    fnL = ::handleFailure,
                    fnR = {
                        handlePrintSuccess(baskets)
                    }
            )
        }
    }

    fun onClickSave() {
        task.value?.let { task ->
            if (isTaskWholesaleAndAnyOfBasketsIsNotClosed(task)) {
                // Есть незакрытые в опте корзины - отобразить экран сообщения «Некоторые корзины не закрыты.
                // Сохранение заданий невозможно», с кнопкой «Назад». См. «MRK_BKS_Макет экранов МП (Крупный ОПТ) 1.1 APP» экран №84
                navigator.showSomeBasketsNotClosedCantSaveScreen()
            } else {
                showMakeTaskCountedAndClose()
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    private fun isTaskWholesaleAndAnyOfBasketsIsNotClosed(task: TaskCreate) = manager.isWholesaleTaskType && task.baskets.isAnyNotLocked()

    private fun showMakeTaskCountedAndClose() {
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
        navigator.hideProgress()
        super.handleFailure(failure)
    }

    private fun handlePrintSuccess(baskets: List<Basket>) {

        baskets.forEach {
            it.isPrinted = true
        }
        navigator.hideProgress()
        //отображать сообщение «Паллетный лист был успешно распечатан», с кнопкой «Далее»
        navigator.showPalletListPrintedScreen(
                nextCallback = {
                    navigator.goBackTo(TaskContentFragment::class.simpleName)
                }
        )
    }

    fun onBackPressed() {
        task.value?.let { taskValue ->
            if (taskValue.goods.isNotEmpty()) {
                navigator.showUnsavedDataWillBeLost {
                    navigator.goBack()
                }
            } else {
                navigator.goBack()
            }
        }
    }

    companion object {
        private const val GOOD_TAB_INDEX = 0
        private const val BASKET_TAB_INDEX = 1
    }

}