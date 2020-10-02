package com.lenta.bp12.features.open_task.good_list

import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.basket.ItemWholesaleBasketUi
import com.lenta.bp12.features.create_task.task_content.ItemCommonBasketUi
import com.lenta.bp12.features.open_task.base.BaseGoodListOpenViewModel
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

/** Работа с заданиями
 * ViewModel ответственный за экран показывающий список товаров и корзин
 * Симетричный класс в Создании заданий:
 * @see com.lenta.bp12.features.create_task.task_content.TaskContent
 * */
class GoodListViewModel : BaseGoodListOpenViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    override lateinit var manager: IOpenTaskManager

    val processingSelectionsHelper = SelectionItemsHelper()

    val processedSelectionsHelper = SelectionItemsHelper()

    val isTaskStrict by unsafeLazy {
        task.value?.isStrict == true
    }

    val title by lazy {
        task.map { task ->
            "${task?.type?.code}-${task?.number} // ${task?.name}"
        }
    }

    val description by lazy {
        resource.goodList
    }


    val processing by lazy {
        task.switchMap { currentTask ->
            asyncTryCatchLiveData {
                currentTask.goods.mapIndexedNotNull { index, good ->
                    ItemGoodProcessingUi(
                            position = "${index + 1}",
                            name = good.getNameWithMaterial(),
                            material = good.material,
                            providerCode = good.provider.code.orEmpty(),
                            quantity = chooseQuantityForProcessing(good),
                            good = good
                    ).takeIf { !good.isDeleted && !good.isCounted }
                }.reversed()
            }
        }
    }

    val processed by lazy {
        task.switchMap { currentTask ->
            asyncTryCatchLiveData {
                currentTask.goods.mapIndexedNotNull { index, good ->
                    ItemGoodProcessedUi(
                            position = "${index + 1}",
                            name = good.getNameWithMaterial(),
                            quantity = "${good.getTotalQuantity().dropZeros()} ${good.commonUnits.name}",
                            material = good.material,
                            providerCode = good.provider.code.orEmpty(),
                            good = good
                    ).takeIf { good.isCounted }
                }.reversed()
            }
        }
    }


    val commonBaskets by lazy {
        task.mapSkipNulls { task ->
            task.baskets.reversed().mapIndexed { index, basket ->
                val position = task.baskets.size - index
                ItemCommonBasketUi(
                        basket = basket,
                        position = "$position",
                        name = resource.basket("${basket.index}"),
                        description = basket.getDescription(
                                isDivBySection = task.type?.isDivBySection ?: false,
                                isWholeSale = false
                        ),
                        quantity = basket.getQuantityFromGoodList().toString()
                )
            }
        }
    }

    val wholesaleBaskets by lazy {
        task.mapSkipNulls { task ->
            task.baskets.reversed().mapIndexed { index, basket ->
                val position = task.baskets.size - index
                ItemWholesaleBasketUi(
                        basket = basket,
                        position = "$position",
                        name = resource.basket("${basket.index}"),
                        description = basket.getDescription(
                                isDivBySection = task.type?.isDivBySection ?: false,
                                isWholeSale = true
                        ),
                        quantity = basket.getQuantityFromGoodList().toString(),
                        isPrinted = basket.isPrinted,
                        isLocked = basket.isLocked
                )
            }
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val deleteVisible by lazy {
        task.map { task ->
            task?.isStrict == false
        }
    }

    val deleteEnabled by unsafeLazy {
        basketSelectionsHelper.selectedPositions.switchMap { selectedBaskets ->
            processedSelectionsHelper.selectedPositions.switchMap { selectedProcessed ->
                processingSelectionsHelper.selectedPositions.switchMap { selectedProcessing ->
                    selectedPage.switchMap { page ->
                        liveData {
                            val isSelectedBaskets = selectedBaskets.isNotEmpty()
                            val isSelectedProcessed = selectedProcessed.isNotEmpty()

                            val processingSize = processing.value?.size ?: 0
                            val isSelectedProcessing = selectedProcessing.isNotEmpty()
                                    && (selectedProcessing.size < processingSize)

                            val result = (page == PROCESSING_PAGE_INDEX && isSelectedProcessing) ||
                                    (page == PROCESSED_PAGE_INDEX && isSelectedProcessed) ||
                                    (page == BASKETS_PAGE_INDEX && isSelectedBaskets)

                            emit(result)
                        }
                    }
                }
            }
        }
    }

    val printVisibility by lazy {
        selectedPage.map { tab ->
            manager.isWholesaleTaskType && tab == 2
        }
    }

    val printEnabled by lazy {
        wholesaleBaskets.map {
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
                PROCESSING_PAGE_INDEX -> {
                    onProcessingItemClick(position)
                }
                PROCESSED_PAGE_INDEX -> {
                    onProcessedItemClick(position)
                }
                BASKETS_PAGE_INDEX -> {
                    onBasketsItemClick(position)
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    private fun onProcessingItemClick(position: Int) {
        processing.value?.let { processingListValue ->
            val good = processingListValue.getOrNull(position)?.good
            good?.let(::setFoundGood) ?: navigator.showGoodIsMissingInTask()
        }
    }

    private fun onProcessedItemClick(position: Int) {
        processed.value?.let { processedListValue ->
            val good = processedListValue.getOrNull(position)?.good
            good?.let(::setFoundGood) ?: navigator.showGoodIsMissingInTask()
        }
    }

    private fun onBasketsItemClick(position: Int) {
        if (manager.isWholesaleTaskType) {
            wholesaleBaskets.value?.let { wholesaleBasketsValue ->
                wholesaleBasketsValue.getOrNull(position)?.basket
            }
        } else {
            commonBaskets.value?.let { commonBasketsValue ->
                commonBasketsValue.getOrNull(position)?.basket
            }
        }?.let {
            manager.updateCurrentBasket(it)
            navigator.openBasketOpenGoodListScreen()
        }.orIfNull {
            Logg.e { "basket null" }
            navigator.showInternalError(resource.basketNotFoundErrorMsg)
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkSearchNumber(numberField.value.orEmpty())
        return true
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                PROCESSING_PAGE_INDEX -> handleDeleteProcessingItems()
                PROCESSED_PAGE_INDEX -> handleDeleteProcessedItems()
                BASKETS_PAGE_INDEX -> handleDeleteBasketItems()
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    private fun handleDeleteProcessingItems() {
        if (isTaskStrict.not()) {
            val materials = mutableListOf<String>()
            processingSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                processing.value?.get(position)?.material
            }.orEmpty()

            processingSelectionsHelper.clearPositions()
            manager.markGoodsDeleted(materials)
        }
    }

    private fun handleDeleteProcessedItems() {
        val materials = mutableListOf<String>()

        processedSelectionsHelper.selectedPositions.value?.forEach { position ->
            val item = processed.value?.getOrNull(position)
            item?.let {
                materials.add(it.material)
                it.good.clearMarksPartsPositions()
            }
        }
        processedSelectionsHelper.clearPositions()
        manager.markGoodsUncounted(materials)
        manager.deleteGoodsFromBaskets(materials)
    }

    private fun handleDeleteBasketItems() {
        val basketList = basketSelectionsHelper.selectedPositions.value?.mapNotNull { position ->
            if (manager.isWholesaleTaskType) {
                wholesaleBaskets.value?.get(position)?.basket
            } else {
                commonBaskets.value?.get(position)?.basket
            }
        }.orEmptyMutable()

        basketSelectionsHelper.clearPositions()
        manager.removeBaskets(basketList)
    }

    fun onClickSave() {
        task.value?.let { task ->
            when {
                // Есть незакрытые в опте корзины - отобразить экран сообщения «Некоторые корзины не закрыты.
                // Сохранение заданий невозможно», с кнопкой «Назад». См. «MRK_BKS_Макет экранов МП (Крупный ОПТ) 1.1 APP» экран №84
                isTaskWholesaleAndAnyOfBasketsIsNotClosed(task) ->
                    navigator.showSomeBasketsNotClosedCantSaveScreen()
                //Если есть не удаленные товары в задании и их плановое количество больше фактического
                task.isQuantityOfNotDeletedGoodsNotActual() -> {
                    task.goods.forEach {
                        it.isMissing = false
                    }
                    navigator.openDiscrepancyListScreen()
                }
                else -> showMakeTaskCountedAndClose()
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    private fun isTaskWholesaleAndAnyOfBasketsIsNotClosed(task: TaskOpen) = manager.isWholesaleTaskType && task.baskets.isAnyNotLocked()

    private fun showMakeTaskCountedAndClose() {
        navigator.showMakeTaskCountedAndClose {
            manager.finishCurrentTask()
            manager.prepareSendTaskDataParams(
                    deviceIp = deviceInfo.getDeviceIp(),
                    tkNumber = sessionInfo.market.orEmpty(),
                    userNumber = sessionInfo.personnelNumber.orEmpty()
            )
            navigator.openSaveDataScreen()
        }
    }

    fun getCountTab(): Int {
        return COUNT_TAB
    }

    fun onPrint() {
        task.value?.let { taskValue ->
            basketSelectionsHelper.selectedPositions.value?.let { positions ->
                val taskValueBaskets = taskValue.baskets
                val baskets = positions.takeIf { it.isNotEmpty() }
                        ?.mapNotNullTo(mutableListOf()) {
                            taskValueBaskets.getOrNull(it)
                        } ?: taskValueBaskets

                if (baskets.isAnyNotLocked()) {
                    // «Некоторые выбранные корзины не закрыты. Закройте корзины и повторите печать», с кнопкой «Назад»
                    navigator.showSomeOfChosenBasketsNotClosedScreen()
                } else {
                    printLockedBasket(baskets)
                }
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    private fun printLockedBasket(baskets: List<Basket>) {
        if (baskets.isAnyPrinted()) {
            // «По некоторым выделенным корзинам уже производилась печать. Продолжить?», с кнопками «Да», «Назад» (макеты, экран №81)
            navigator.showSomeBasketsAlreadyPrinted(
                    yesCallback = { printPalletList(baskets) }
            )
        } else {
            printPalletList(baskets)
        }
    }

    // TODO Функция не проверена (13.08.2020 САП еще не создан)
    private fun printPalletList(baskets: List<Basket>) {
        launchUITryCatch {
            // Собираем в один список все товары
            navigator.showProgressLoadingData()
            val isDivBySection = task.value?.type?.isDivBySection ?: false
            val request = printPalletListNetRequest(
                    Triple(baskets, isDivBySection, manager.isWholesaleTaskType)
            )
            navigator.hideProgress()
            request.either(
                    fnL = ::handleFailure,
                    fnR = {
                        handlePrintSuccess(baskets)
                    }
            )
        }
    }

    private fun chooseQuantityForProcessing(good: Good): String {
        return good.takeIf { good.planQuantity > ZERO_QUANTITY }?.run {
            "${planQuantity.dropZeros()} ${commonUnits.name}"
        }.orEmpty()
    }

    private fun handlePrintSuccess(baskets: List<Basket>) {
        baskets.forEach {
            it.isPrinted = true
        }
        //отображать сообщение «Паллетный лист был успешно распечатан», с кнопкой «Далее»
        navigator.showPalletListPrintedScreen(nextCallback = navigator::goBack)
    }

    companion object {
        private const val PROCESSING_PAGE_INDEX = 0
        private const val PROCESSED_PAGE_INDEX = 1
        private const val BASKETS_PAGE_INDEX = 2
        private const val COUNT_TAB = 3
    }

}
