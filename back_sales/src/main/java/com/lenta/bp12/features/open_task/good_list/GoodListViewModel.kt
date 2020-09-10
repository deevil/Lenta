package com.lenta.bp12.features.open_task.good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.basket.ItemWholesaleBasketUi
import com.lenta.bp12.features.create_task.task_content.ItemCommonBasketUi
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.extentions.getDescription
import com.lenta.bp12.model.pojo.extentions.getQuantityFromGoodList
import com.lenta.bp12.model.pojo.extentions.isAnyNotLocked
import com.lenta.bp12.model.pojo.extentions.isAnyPrinted
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.PrintPalletListNetRequest
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class GoodListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    lateinit var manager: IOpenTaskManager

    @Inject
    lateinit var markManager: IMarkManager

    @Inject
    lateinit var database: IDatabaseRepository

    @Inject
    lateinit var resource: IResourceManager

    /** "ZMP_UTZ_BKS_07_V001"
     * Печать паллетной ведомости
     */
    @Inject
    lateinit var printPalletListNetRequest: PrintPalletListNetRequest

    /** "ZMP_UTZ_BKS_05_V001"
     * Получение данных товара по ШК / SAP-коду
     */
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    val processingSelectionsHelper = SelectionItemsHelper()

    val processedSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(PROCESSING_PAGE_INDEX)

    val basketSelectionsHelper = SelectionItemsHelper()


    val task by lazy {
        manager.currentTask
    }

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

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

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
                                quantity = "${good.planQuantity.dropZeros()} ${good.commonUnits.name}"
                        )
                    }
                }
            }
        }
    }

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
                                providerCode = good.provider.code.orEmpty()
                        )
                    }
                }
            }
        }
    }

    val commonBaskets by lazy {
        task.map {
            it?.let { task ->
                task.baskets.reversed().mapIndexed { index, basket ->
                    val position = task.baskets.size - index
                    ItemCommonBasketUi(
                            basket = basket,
                            position = "$position",
                            name = resource.basket("${basket.index}"),
                            description = basket.getDescription(task.type?.isDivBySection ?: false),
                            quantity = basket.getQuantityFromGoodList().toString()
                    )
                }
            }
        }
    }

    val wholesaleBaskets by lazy {
        task.map {
            it?.let { task ->
                task.baskets.reversed().mapIndexed { index, basket ->
                    val position = task.baskets.size - index
                    ItemWholesaleBasketUi(
                            basket = basket,
                            position = "$position",
                            name = resource.basket("${basket.index}"),
                            description = basket.getDescription(task.type?.isDivBySection ?: false),
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

    val deleteEnabled = selectedPage.combineLatest(processingSelectionsHelper.selectedPositions)
            .combineLatest(processedSelectionsHelper.selectedPositions).map {
                it?.let {
                    val page = it.first.first
                    val isSelectedProcessing = it.first.second.isNotEmpty()
                    val isSelectedProcessed = it.second.isNotEmpty()

                    (page == PROCESSED_PAGE_INDEX && isSelectedProcessing) || (page == PROCESSED_PAGE_INDEX && isSelectedProcessed)
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
            val material = processingListValue.getOrNull(position)?.material
            material?.let(::openGoodByMaterial) ?: navigator.showGoodIsMissingInTask()
        }
    }

    private fun onProcessedItemClick(position: Int) {
        processed.value?.let { processedListValue ->
            val material = processedListValue.getOrNull(position)?.material
            material?.let(::openGoodByMaterial) ?: navigator.showGoodIsMissingInTask()
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

    fun onScanResult(data: String) {
        checkSearchNumber(data)
    }

    /**
     * Метод проверяет длину отсканированного/введенного кода
     * */
    private fun checkSearchNumber(number: String) {
        actionByNumber(
                number = number,
                funcForEan = {
                    getGoodByEan(number)
                },
                funcForMaterial = ::getGoodByMaterial,
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForMark = ::checkMark,
                funcForNotValidBarFormat = navigator::showIncorrectEanFormat
        )
        numberField.value = ""
    }

    /**
     * Метод ищет есть ли уже товар в задании по EAN,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    private fun getGoodByEan(ean: String) {
        manager.findGoodByEan(ean)?.let { foundGood ->
            setFoundGood(foundGood)
            return
        }

        if (task.value?.isStrict == false) {
            loadGoodInfoByEan(ean)
        } else {
            navigator.showGoodIsMissingInTask()
        }
    }

    private fun loadGoodInfoByEan(ean: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    ean = ean,
                    taskType = task.value?.type?.code.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) {
                handleLoadGoodInfoResult(it)
            }
        }
    }

    /**
     * Метод ищет есть ли уже товар в задании по Sap коду,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { foundGood ->
            setFoundGood(foundGood)
            return
        }

        if (task.value?.isStrict == false) {
            loadGoodInfoByMaterial(material)
        } else {
            navigator.showGoodIsMissingInTask()
        }
    }

    private fun loadGoodInfoByMaterial(material: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = material,
                    taskType = task.value?.type?.code.orEmpty(),
                    mode = ScanInfoMode.MARK.mode.toString()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleLoadGoodInfoResult(result)
            }
        }
    }

    private fun handleLoadGoodInfoResult(result: GoodInfoResult) {
        launchUITryCatch {
            if (manager.isGoodCorrespondToTask(result)) {
                if (manager.isGoodCanBeAdded(result)) {
                    setGood(result)
                } else {
                    navigator.showGoodCannotBeAdded()
                }
            } else {
                navigator.showNotMatchTaskSettingsAddingNotPossible()
            }
        }
    }

    private suspend fun setGood(result: GoodInfoResult) {

        with(result) {
            val markType = getMarkType()
            val goodOpen = Good(
                    ean = eanInfo?.ean.orEmpty(),
                    material = materialInfo?.material.orEmpty(),
                    name = materialInfo?.name.orEmpty(),
                    section = materialInfo?.section.orEmpty(),
                    matrix = getMatrixType(materialInfo?.matrix.orEmpty()),
                    kind = getGoodKind(),
                    control = getControlType(),
                    commonUnits = database.getUnitsByCode(materialInfo?.commonUnitsCode.orEmpty()),
                    innerUnits = database.getUnitsByCode(materialInfo?.innerUnitsCode.orEmpty()),
                    innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull() ?: 0.0,
                    provider = task.value?.provider ?: ProviderInfo(),
                    producers = producers.orEmpty().toMutableList(),
                    volume = materialInfo?.volume?.toDoubleOrNull() ?: 0.0,
                    markType = markType,
                    markTypeGroup = database.getMarkTypeGroupByMarkType(markType),
                    maxRetailPrice = "",
                    type = materialInfo?.goodType.orEmpty()
            )

            if (goodOpen.kind == GoodKind.EXCISE) {
                navigator.showForExciseGoodNeedScanFirstMark()
            }

                setFoundGood(goodOpen)
            }
        }


    private fun setFoundGood(foundGood: Good) {
        manager.updateCurrentGood(foundGood)

        with(navigator) {
            if (foundGood.markType != MarkType.UNKNOWN) {
                openMarkedGoodInfoCreateScreen()
                showForGoodNeedScanFirstMark()
            } else {
                openGoodInfoCreateScreen()
            }
        }

        Logg.d { "--> found good: $foundGood" }
    }

    private fun checkMark(number: String) {
        launchUITryCatch {
            with(navigator) {
                showProgressLoadingData()
                val screenStatus = markManager.checkMark(number, WorkType.CREATE)
                Logg.e { screenStatus.name }
                hideProgress()

                when (screenStatus) {
                    MarkScreenStatus.OK -> openMarkedGoodInfoCreateScreen()
                    MarkScreenStatus.NO_MARKTYPE_IN_SETTINGS -> showNoMarkTypeInSettings()
                    MarkScreenStatus.INCORRECT_EAN_FORMAT -> showIncorrectEanFormat()
                    else -> Unit
                }
            }
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkSearchNumber(numberField.value.orEmpty())
        return true
    }

    private fun openGoodByMaterial(material: String) {
        task.value?.let { task ->
            task.goods.find { it.material == material }?.let { good ->
                manager.currentGood.value = good
                if (good.markType != MarkType.UNKNOWN) {
                    navigator.openMarkedGoodInfoOpenScreen()
                } else {
                    navigator.openGoodInfoOpenScreen()
                }
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                PROCESSING_PAGE_INDEX -> {
                    val materials = mutableListOf<String>()
                    processingSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                        processing.value?.get(position)?.material
                    }

                    processingSelectionsHelper.clearPositions()
                    manager.markGoodsDeleted(materials)
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
                        commonBaskets.value?.get(position)?.basket
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
            if (task.isExistUncountedGood()) {
                navigator.openDiscrepancyListScreen()
            } else {
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
                    baskets to isDivBySection
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
