package com.lenta.bp12.features.create_task.task_content

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.basket.ItemWholesaleBasketUi
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.request.PrintPalletListNetRequest
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListBasket
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListGood
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.launchAsyncTryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.isCommonFormatNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    /**
    Переменные
     */

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
                            quantity = "${good.getTotalQuantity().dropZeros()} ${good.commonUnits.name}"
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
        openGoodInfoByNumber(data)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        openGoodInfoByNumber(numberField.value.orEmpty())
        return true
    }

    private fun openGoodInfoByNumber(number: String) {
        numberField.value = ""

        if (isCommonFormatNumber(number)) {
            manager.searchNumber = number
            manager.isSearchFromList = true
            navigator.openGoodInfoCreateScreen()
        } else {
            navigator.showIncorrectEanFormat()
        }
    }

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    manager.searchNumber = goods.value!![position].material
                    manager.isSearchFromList = true
                    navigator.openGoodInfoCreateScreen()
                }
                1 -> {
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
                    navigator.openBasketCreateGoodListScreen()
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    val materials = mutableListOf<String>()
                    goodSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                        goods.value?.get(position)?.material
                    }

                    goodSelectionsHelper.clearPositions()
                    manager.removeGoodByMaterials(materials)
                }
                1 -> {
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
        }
    }

    // TODO Функция не проверена (13.08.2020 САП еще не создан)
    private fun printPalletList(baskets: List<Basket>) {
        launchAsyncTryCatch {
            // собираем в один список все товары
            navigator.showProgressLoadingData()
            val goodListRest = baskets.flatMap { basket ->
                val distinctGoods = basket.goods.keys
                distinctGoods.map { good ->
                    val quantity = basket.goods[good]
                    PrintPalletListGood(
                            materialNumber = good.material,
                            basketNumber = basket.index.toString(),
                            quantity = quantity.toString(),
                            uom = good.commonUnits.code
                    )
                }
            }

            val basketListRest = baskets.map {
                val description = it.getDescription(task.value?.type?.isDivBySection ?: false)
                PrintPalletListBasket(
                        number = it.index.toString(),
                        description = description,
                        section = it.section.orEmpty()
                )
            }

            withContext(Dispatchers.Main) {
                printPalletListNetRequest(
                        PrintPalletListParams(
                                userNumber = sessionInfo.personnelNumber.orEmpty(),
                                deviceIp = resource.deviceIp(),
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
    }

    fun onClickSave() {
        task.value?.let { task ->
            if (manager.isWholesaleTaskType) {
                // Есть незакрытые корзины - отобразить экран сообщения «Некоторые корзины не закрыты.
                // Сохранение заданий невозможно», с кнопкой «Назад». См. «MRK_BKS_Макет экранов МП (Крупный ОПТ) 1.1 APP» экран №84
                if (task.baskets.any { it.isLocked.not() }) {
                    navigator.showSomeBasketsNotClosedCantSaveScreen()
                } else {
                    showMakeTaskCountedAndCLose()
                }
            } else {
                showMakeTaskCountedAndCLose()
            }
        }
    }

    private fun showMakeTaskCountedAndCLose() {
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
        super.handleFailure(failure)
        navigator.hideProgress()
        navigator.openAlertScreen(failure)
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

}