package com.lenta.bp12.features.create_task.task_content

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.basket.ItemWholesaleBasketUi
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.actionByNumber
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.model.pojo.extentions.getDescription
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.MarkCartonBoxGoodInfoNetRequest
import com.lenta.bp12.request.PrintPalletListNetRequest
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParams
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParamsBasket
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParamsGood
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants.MARK_TOBACCO_PACK_29
import com.lenta.shared.platform.constants.Constants.TOBACCO_MARK_BLOCK_OR_BOX_RANGE_30_44
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.*
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class TaskContentViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    lateinit var resource: IResourceManager

    @Inject
    lateinit var printPalletListNetRequest: PrintPalletListNetRequest

    /** Получение данных товара по ШК / SAP-коду
     * "ZMP_UTZ_BKS_05_V001" */
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var markCartonBoxGoodInfoNetRequest: MarkCartonBoxGoodInfoNetRequest

    @Inject
    lateinit var database: IDatabaseRepository


    /**
    Переменные
     */

    private var isEanLastScanned = false

    private var lastSuccessSearchNumber = ""

    val goodSelectionsHelper = SelectionItemsHelper()

    val basketSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    private val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { task ->
            task?.getFormattedName()
        }
    }

    val numberField = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val goods by lazy {
        task.map {
            it?.let { task ->
                task.goods.mapIndexed { index, good ->
                    ItemGoodUi(
                            material = good.material,
                            position = "${task.goods.size - index}",
                            name = good.getNameWithMaterial(),
                            quantity = "${good.getTotalQuantity().dropZeros()} ${good.commonUnits.name}",
                            markType = good.markType
                    )
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
                            description = basket.getDescription(task.type.isDivBySection),
                            quantity = "${task.getCountByBasket(basket)}"
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
                            description = basket.getDescription(task.type.isDivBySection),
                            quantity = "${task.getCountByBasket(basket)} ${Uom.ST.name}",
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

    val deleteEnabled = selectedPage.combineLatest(goodSelectionsHelper.selectedPositions)
            .combineLatest(basketSelectionsHelper.selectedPositions).map {
                val tab = it!!.first.first
                val isGoodSelected = it.first.second.isNotEmpty()
                val isBasketSelected = it.second.isNotEmpty()

                tab == 0 && isGoodSelected || tab == 1 && isBasketSelected
            }

    val printVisibility by lazy {
        selectedPage.map { tab ->
            manager.isWholesaleTaskType && tab == 1
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
    }

    private fun checkMark(number: String) {
        when (number.length) {
            MARK_TOBACCO_PACK_29 -> {
                if (isTobaccoPackMark(number))
                    navigator.showCantScanPackAlert()
            }
            in TOBACCO_MARK_BLOCK_OR_BOX_RANGE_30_44 -> {
                if (isTobaccoCartonMark(number)) {
                    openMarkedGoodWithCarton(number)
                }
            }
            else -> {
                if (isTobaccoCartonMark(number)) {
                    openMarkedGoodWithCarton(number)
                } else {
                    navigator.showIncorrectEanFormat()
                }
            }
        }
    }

    private fun openMarkedGoodWithCarton(number: String) {
        val gtin = number.cutOutGTIN()
        val container = Mark.Container.Pack(number, gtin)
        getGoodByEan(gtin, container)
    }

    /**
     * Метод ищет есть ли уже товар в задании по EAN,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    private fun getGoodByEan(ean: String, container: Mark.Container? = null) {
        manager.findGoodByEan(ean)?.let { foundGood ->
            lastSuccessSearchNumber = ean
            isEanLastScanned = true
            setFoundGood(foundGood)
        } ?: loadGoodInfoByEan(ean, container)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { foundGood ->
            setFoundGood(foundGood)
        } ?: loadGoodInfoByMaterial(material)
    }

    private fun setFoundGood(foundGood: GoodCreate, container: Mark.Container? = null) {

        if (foundGood.markType != MarkType.UNKNOWN) {
            if (container != null) {


                navigator.showForGoodNeedScanFirstMark()
            } else {
                navigator.openMarkedGoodInfoCreateScreen()
            }
        } else {
            manager.updateCurrentGood(foundGood)
            navigator.openGoodInfoCreateScreen()
        }
        Logg.d { "--> found good: $foundGood" }
    }

    private fun loadGoodInfoByEan(ean: String, container: Mark.Container? = null) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    ean = ean,
                    taskType = task.value?.type?.code.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) {
                handleLoadGoodInfoResult(it, container)
            }
        }
    }

    private fun loadGoodInfoByMaterial(material: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = material,
                    taskType = task.value?.type?.code.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleLoadGoodInfoResult(
                        result = result)
            }
        }
    }

    private fun handleLoadGoodInfoResult(result: GoodInfoResult, container: Mark.Container? = null) {
        launchUITryCatch {
            if (manager.isGoodCanBeAdded(result)) {
                setGood(result, container)
            } else {
                manager.clearSearchFromListParams()
                navigator.showGoodCannotBeAdded()
            }
        }
    }

    /**
     * Метод проверяет маркированный товар пришел или нет.
     * если маркированный, то показываем сообщение о том что нужно сканировать марку,
     * если нет, то создаём его и показываем карточку
     */
    private fun setGood(result: GoodInfoResult, container: Mark.Container? = null) {
        launchUITryCatch {
            with(result) {
                task.value?.let { task ->
                    val taskType = task.type
                    val good = GoodCreate(
                            ean = eanInfo?.ean.orEmpty(),
                            eans = database.getEanListByMaterialUnits(
                                    material = materialInfo?.material.orEmpty(),
                                    unitsCode = materialInfo?.commonUnitsCode.orEmpty()
                            ),
                            material = materialInfo?.material.orEmpty(),
                            name = materialInfo?.name.orEmpty(),
                            kind = getGoodKind(),
                            type = materialInfo?.goodType.takeIf { taskType.isDivByGoodType }.orEmpty(),
                            control = getControlType(),
                            section = materialInfo?.section.takeIf { taskType.isDivBySection }.orEmpty(),
                            matrix = getMatrixType(materialInfo?.matrix.orEmpty()),
                            commonUnits = database.getUnitsByCode(materialInfo?.commonUnitsCode.orEmpty()),
                            innerUnits = database.getUnitsByCode(materialInfo?.innerUnitsCode.orEmpty()),
                            innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull()
                                    ?: 1.0,
                            providers = providers?.toMutableList().takeIf { taskType.isDivByProvider }.orEmpty().toMutableList(),
                            producers = producers?.toMutableList().orEmpty().toMutableList(),
                            volume = materialInfo?.volume?.toDoubleOrNull() ?: 0.0,
                            markType = getMarkType(),
                            maxRetailPrice = "")

                    setFoundGood(good, container)
                }.orIfNull {
                    Logg.e { "task null" }
                    navigator.showInternalError(resource.taskNotFoundErrorMsg)
                }
            }
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkSearchNumber(numberField.value.orEmpty())
        return true
    }

    private fun String.cutOutGTIN(): String {
        return this.substringAfter("01").substringBefore("21")
    }

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                GOOD_TAB_INDEX -> {
                    val good = goods.value?.getOrNull(position)
                    good?.let {
                        manager.searchNumber = good.material
                        manager.isSearchFromList = true
                        if (good.markType != MarkType.UNKNOWN) {
                            navigator.openMarkedGoodInfoCreateScreen()
                        } else {
                            navigator.openGoodInfoCreateScreen()
                        }
                    }.orIfNull {
                        Logg.e { "good null" }
                        navigator.showInternalError(resource.goodNotFoundErrorMsg)
                    }
                }
                BASKET_TAB_INDEX -> {
                    val basket = if (manager.isWholesaleTaskType) {
                        wholesaleBaskets.value?.getOrNull(position)?.basket
                    } else {
                        commonBaskets.value?.getOrNull(position)?.basket

                    }
                    manager.updateCurrentBasket(basket)
                    navigator.openBasketCreateGoodListScreen()
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                GOOD_TAB_INDEX -> {
                    val materials = mutableListOf<String>()
                    goodSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                        goods.value?.get(position)?.material
                    }

                    goodSelectionsHelper.clearPositions()
                    manager.removeGoodByMaterials(materials)
                }
                BASKET_TAB_INDEX -> {
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

    fun onPrint() {
        task.value?.let { taskValue ->
            goodSelectionsHelper.selectedPositions.value?.let { positions ->

                val taskValueBaskets = taskValue.baskets
                val baskets = positions.takeIf { it.isNotEmpty() }
                        ?.mapNotNullTo(mutableListOf()) {
                            taskValueBaskets.getOrNull(it)
                        }.orIfNull { taskValueBaskets }

                //Если какие-то корзины не закрыты
                if (baskets.any { it.isLocked.not() }) {
                    // Вывести экран сообщения «Некоторые выбранные корзины не закрыты. Закройте корзины и повторите печать», с кнопкой «Назад»
                    navigator.showSomeOfChosenBasketsNotClosedScreen()
                } else {
                    //Если какие-то корзины напечатаны
                    if (baskets.any { it.isPrinted }) {
                        // «По некоторым выделенным корзинам уже производилась печать. Продолжить?», с кнопками «Да», «Назад» (макеты, экран №81)
                        navigator.showSomeBasketsAlreadyPrinted(
                                yesCallback = { printPalletList(baskets) }
                        )
                    } else {
                        printPalletList(baskets)
                    }
                }
            }
        }
    }

    // TODO Функция не проверена (13.08.2020 САП еще не создан)
    private fun printPalletList(baskets: List<Basket>) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            // собираем в один список все товары
            val goodListRest = baskets.flatMap { basket ->
                val distinctGoods = basket.goods.keys
                distinctGoods.map { good ->
                    val quantity = basket.goods[good]
                    PrintPalletListParamsGood(
                            materialNumber = good.material,
                            basketNumber = basket.index.toString(),
                            quantity = quantity.toString(),
                            uom = good.commonUnits.code
                    )
                }
            }

            val basketListRest = baskets.map {
                val description = it.getDescription(task.value?.type?.isDivBySection ?: false)
                PrintPalletListParamsBasket(
                        number = it.index.toString(),
                        description = description,
                        section = it.section.orEmpty()
                )
            }

            printPalletListNetRequest(
                    PrintPalletListParams(
                            userNumber = sessionInfo.personnelNumber.orEmpty(),
                            deviceIp = resource.deviceIp,
                            baskets = basketListRest,
                            goods = goodListRest
                    )
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
            if (manager.isWholesaleTaskType) {
                // Есть незакрытые корзины - отобразить экран сообщения «Некоторые корзины не закрыты.
                // Сохранение заданий невозможно», с кнопкой «Назад». См. «MRK_BKS_Макет экранов МП (Крупный ОПТ) 1.1 APP» экран №84
                if (task.baskets.any { it.isLocked.not() }) {
                    navigator.showSomeBasketsNotClosedCantSaveScreen()
                } else {
                    showMakeTaskCountedAndClose()
                }
            } else {
                showMakeTaskCountedAndClose()
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

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
                    navigator.goBack()
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