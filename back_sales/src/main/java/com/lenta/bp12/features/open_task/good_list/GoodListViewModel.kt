package com.lenta.bp12.features.open_task.good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.basket.ItemWholesaleBasketUi
import com.lenta.bp12.features.create_task.task_content.ItemCommonBasketUi
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.platform.extention.getDescription
import com.lenta.bp12.platform.extention.getGoodList
import com.lenta.bp12.platform.extention.getQuantityFromGoodList
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.request.PrintPalletListNetRequest
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParams
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParamsBasket
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParamsGood
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.isCommonFormatNumber
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
    lateinit var resource: IResourceManager

    @Inject
    lateinit var printPalletListNetRequest: PrintPalletListNetRequest

    val processingSelectionsHelper = SelectionItemsHelper()

    val processedSelectionsHelper = SelectionItemsHelper()

    val basketSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val task by lazy {
        manager.currentTask
    }

    val isTaskStrict by unsafeLazy {
        task.value?.isStrict ?: false
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
                emptyList<ItemWholesaleBasketUi>()

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
                emptyList<ItemWholesaleBasketUi>()

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

                    (page == 0 && isSelectedProcessing) || (page == 1 && isSelectedProcessed)
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
                0 -> {
                    processing.value?.let { processingListValue ->
                        val material = processingListValue.getOrNull(position)?.material
                        material?.let(::openGoodByMaterial) ?: navigator.showGoodIsMissingInTask()
                    }
                }
                1 -> {
                    processed.value?.let { processedListValue ->
                        val material = processedListValue.getOrNull(position)?.material
                        material?.let(::openGoodByMaterial) ?: navigator.showGoodIsMissingInTask()
                    }
                }
                2 -> {
                    val basket = if (manager.isWholesaleTaskType) {
                        wholesaleBaskets.value?.let { wholesaleBasketsValue ->
                            wholesaleBasketsValue.getOrNull(position)?.basket
                        }
                    } else {
                        commonBaskets.value?.let { commonBasketsValue ->
                            commonBasketsValue.getOrNull(position)?.basket
                        }
                    }
                    manager.updateCurrentBasket(basket)
                    navigator.openBasketOpenGoodListScreen()
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    fun onScanResult(data: String) {
        openGoodInfoByNumber(data)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        openGoodInfoByNumber(numberField.value.orEmpty())
        return true
    }

    private fun openGoodByMaterial(material: String) {
        task.value?.let { task ->
            task.goods.find { it.material == material }?.let {
                manager.searchNumber = material
                manager.isSearchFromList = true
                navigator.openGoodInfoOpenScreen()
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    private fun openGoodInfoByNumber(number: String) {
        numberField.value = ""

        if (isCommonFormatNumber(number)) {
            manager.searchNumber = number
            manager.isSearchFromList = true
            navigator.openGoodInfoOpenScreen()
        } else {
            navigator.showIncorrectEanFormat()
        }
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    val materials = mutableListOf<String>()
                    processingSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                        processing.value?.get(position)?.material
                    }

                    processingSelectionsHelper.clearPositions()
                    manager.markGoodsDeleted(materials)
                }
                1 -> {
                    val materials = mutableListOf<String>()
                    processedSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                        processed.value?.get(position)?.material
                    }

                    processedSelectionsHelper.clearPositions()
                    manager.markGoodsUncounted(materials)
                    manager.deleteGoodsFromBaskets(materials)
                }
                2 -> {
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
                var baskets = taskValue.baskets
                //Если корзины выделены то берем их
                if (positions.isNotEmpty()) {
                    baskets = positions.mapNotNull {
                        baskets.getOrNull(it)
                    }.toMutableList()
                }
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
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    // TODO Функция не проверена (13.08.2020 САП еще не создан)
    private fun printPalletList(baskets: List<Basket>) {
        launchUITryCatch {
            // собираем в один список все товары
            navigator.showProgressLoadingData()
            val goodListRest = baskets.flatMap { basket ->
                val distinctGoods = basket.getGoodList()
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

            val request = printPalletListNetRequest(
                    PrintPalletListParams(
                            userNumber = sessionInfo.personnelNumber.orEmpty(),
                            deviceIp = resource.deviceIp,
                            baskets = basketListRest,
                            goods = goodListRest
                    )
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
        navigator.showPalletListPrintedScreen(
                nextCallback = {
                    navigator.goBack()
                }
        )
    }

    companion object {
        private const val COUNT_TAB = 3
    }

}
