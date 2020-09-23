package com.lenta.bp12.features.open_task.good_list

import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.basket.ItemWholesaleBasketUi
import com.lenta.bp12.features.create_task.task_content.ItemCommonBasketUi
import com.lenta.bp12.features.open_task.base.BaseGoodListOpenViewModel
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.extentions.getDescription
import com.lenta.bp12.model.pojo.extentions.getQuantityFromGoodList
import com.lenta.bp12.model.pojo.extentions.isAnyNotLocked
import com.lenta.bp12.model.pojo.extentions.isAnyPrinted
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.PrintPalletListNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

/** Работа с заданиями
 * ViewModel ответственный за экран показывающий список товаров и корзин
 * Симетричный класс в Создании заданий:
 * @see com.lenta.bp12.features.create_task.task_content.TaskContent
 * */
class GoodListViewModel : BaseGoodListOpenViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    override lateinit var navigator: IScreenNavigator

    @Inject
    override lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    override lateinit var manager: IOpenTaskManager

    @Inject
    override lateinit var markManager: IMarkManager

    @Inject
    override lateinit var database: IDatabaseRepository

    @Inject
    override lateinit var resource: IResourceManager

    /** "ZMP_UTZ_BKS_07_V001"
     * Печать паллетной ведомости
     */
    @Inject
    lateinit var printPalletListNetRequest: PrintPalletListNetRequest

    /** "ZMP_UTZ_BKS_05_V001"
     * Получение данных товара по ШК / SAP-коду
     */
    @Inject
    override lateinit var goodInfoNetRequest: GoodInfoNetRequest

    val processingSelectionsHelper = SelectionItemsHelper()

    val processedSelectionsHelper = SelectionItemsHelper()

    val basketSelectionsHelper = SelectionItemsHelper()

    val isTaskStrict by unsafeLazy {
        task.value?.isStrict == true
    }

    val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        task.map { task ->
            "${task?.type?.code}-${task?.number} // ${task?.name}"
        }
    }

    val description by lazy {
        if (manager.isWholesaleTaskType) resource.taskContent() else resource.goodList()
    }

    //TODO REFACTOR TO ASYNCLIVEDATA, COMBINE TWO CYCLES IN ONE, GET RID OF LET
    val processing by lazy {
        task.map { currentTask ->
            currentTask?.let { task ->
                task.goods.filter { !it.isDeleted && !it.isCounted }.let { filtered ->
                    filtered.mapIndexed { index, good ->
                        ItemGoodProcessingUi(
                                position = "${filtered.size - index}",
                                name = good.getNameWithMaterial(),
                                material = good.material,
                                providerCode = good.provider.code.orEmpty(),
                                quantity = "${good.planQuantity.dropZeros()} ${good.commonUnits.name}",
                                good = good
                        )
                    }
                }
            }
        }
    }

    //TODO REFACTOR TO ASYNCLIVEDATA, COMBINE TWO CYCLES IN ONE, GET RID OF LET
    val processed by lazy {
        task.map { currentTask ->
            currentTask?.let { task ->
                task.goods.filter { it.isCounted }.let { filtered ->
                    filtered.mapIndexed { index, good ->
                        ItemGoodProcessedUi(
                                position = "${filtered.size - index}",
                                name = good.getNameWithMaterial(),
                                quantity = "${good.getTotalQuantity().dropZeros()} ${good.commonUnits.name}",
                                material = good.material,
                                providerCode = good.provider.code.orEmpty(),
                                good = good
                        )
                    }
                }
            }
        }
    }

    //TODO REFACTOR TO ASYNCLIVEDATA, COMBINE TWO CYCLES IN ONE, GET RID OF LET
    val commonBaskets by lazy {
        task.map {
            it?.let { task ->
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
    }

    //TODO REFACTOR TO ASYNCLIVEDATA, COMBINE TWO CYCLES IN ONE, GET RID OF LET
    val wholesaleBaskets by lazy {
        task.map {
            it?.let { task ->
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

    val saveEnabled by lazy {
        task.map {
            it?.isExistProcessedGood()
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
                PROCESSING_PAGE_INDEX -> {
                    if (isTaskStrict.not()) {
                        val materials = mutableListOf<String>()
                        processingSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                            processing.value?.get(position)?.material
                        }

                        processingSelectionsHelper.clearPositions()
                        manager.markGoodsDeleted(materials)
                    }
                }
                PROCESSED_PAGE_INDEX -> {
                    val materials = processedSelectionsHelper.selectedPositions.value?.mapNotNullTo(mutableListOf()) { position ->
                        processed.value?.get(position)?.material
                    }.orEmpty()

                    processedSelectionsHelper.clearPositions()
                    manager.markGoodsUncounted(materials)
                    manager.deleteGoodsFromBaskets(materials)
                }
                BASKETS_PAGE_INDEX -> {
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
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    fun onClickSave() {
        task.value?.let { task ->
            //Если есть не удаленные товары в задании и их плановое количество больше фактического
            if (task.isQuantityOfNotDeletedGoodsNotActual()) {
                navigator.openDiscrepancyListScreen() // откроем лист расхождений
            } else {
                showMakeTaskCountedAndClose()
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
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
