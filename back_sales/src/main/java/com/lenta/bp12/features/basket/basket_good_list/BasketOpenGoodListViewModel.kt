package com.lenta.bp12.features.basket.basket_good_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.DEFAULT_QUANTITY
import com.lenta.bp12.platform.ZERO_VOLUME
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BasketOpenGoodListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: IOpenTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resource: IResourceManager

    @Inject
    lateinit var markManager: IMarkManager

    @Inject
    lateinit var database: IDatabaseRepository

    /** "ZMP_UTZ_BKS_05_V001"
     * Получение данных товара по ШК / SAP-коду
     */
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    val selectionsHelper = SelectionItemsHelper()

    private val task by lazy {
        manager.currentTask
    }

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

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val goods by lazy {
        Logg.e { basket.value.toString() }
        basket.map {
            it?.let { activeBasket ->
                val list = activeBasket.getGoodList()
                list.mapIndexed { index, good ->
                    val units = good.commonUnits.name
                    val quantity = activeBasket.goods[good]

                    Logg.d { "-> freeVolume: ${activeBasket.freeVolume}, isPrinted: ${activeBasket.isPrinted}, isLocked: ${activeBasket.isLocked} ${activeBasket.goods}" }

                    ItemGoodUi(
                            position = "${index + 1}",
                            name = good.getNameWithMaterial(),
                            quantity = "${quantity.dropZeros()} $units",
                            material = good.material,
                            good = good)
                }

            }.orEmpty()
        }
    }

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
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

    fun onScanResult(data: String) {
        checkEnteredNumber(data)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(numberField.value.orEmpty())
        return true
    }

    private fun checkEnteredNumber(number: String) {
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
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByEan(ean) }
            navigator.hideProgress()
            foundGood?.let(::setFoundGood).orIfNull {
                if (task.value?.isStrict == false) {
                    loadGoodInfoByEan(ean)
                } else {
                    navigator.showGoodIsMissingInTask()
                }
            }
        }
    }

    private suspend fun loadGoodInfoByEan(ean: String) {
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

    /**
     * Метод ищет есть ли уже товар в задании по Sap коду,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    private fun getGoodByMaterial(material: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByMaterial(material) }
            navigator.hideProgress()
            foundGood?.let(::setFoundGood).orIfNull {
                if (task.value?.isStrict == false) {
                    loadGoodInfoByMaterial(material)
                } else {
                    navigator.showGoodIsMissingInTask()
                }
            }
        }
    }

    private suspend fun loadGoodInfoByMaterial(material: String) {
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

    private fun handleLoadGoodInfoResult(result: GoodInfoResult) {
        launchUITryCatch {
            val isGoodCorrespondToTask = manager.isGoodCorrespondToTask(result)
            val isGoodCanBeAdded = manager.isGoodCanBeAdded(result)
            val isWholesaleTask = manager.isWholesaleTaskType
            val goodKind = result.getGoodKind()
            val isGoodVet = goodKind == GoodKind.VET
            val isGoodExcise = goodKind == GoodKind.EXCISE
            with(navigator) {
                when {
                    isWholesaleTask && isGoodVet -> showCantAddVetToWholeSale()
                    isWholesaleTask && isGoodExcise -> showCantAddExciseGoodForWholesale()
                    isGoodCorrespondToTask && isGoodCanBeAdded -> setGood(result)
                    isGoodCorrespondToTask -> showGoodCannotBeAdded()
                    else -> showNotMatchTaskSettingsAddingNotPossible()
                }
            }
        }
    }

    private fun checkMark(number: String) {
        launchUITryCatch {
            with(navigator) {
                showProgressLoadingData()
                val screenStatus = markManager.checkMark(number, WorkType.CREATE)
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
                    innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull() ?: DEFAULT_QUANTITY,
                    provider = task.value?.provider ?: ProviderInfo.getEmptyProvider(),
                    producers = producers.orEmpty().toMutableList(),
                    volume = materialInfo?.volume?.toDoubleOrNull() ?: ZERO_VOLUME,
                    markType = markType,
                    markTypeGroup = database.getMarkTypeGroupByMarkType(markType),
                    type = materialInfo?.goodType.orEmpty(),
                    purchaseGroup = materialInfo?.purchaseGroup.orEmpty()
            )

            setFoundGood(goodOpen)
        }
    }

    private fun setFoundGood(foundGood: Good) {
        with(navigator) {
            manager.updateCurrentGood(foundGood)
            if (foundGood.isMarked()) {
                openMarkedGoodInfoCreateScreen()
                showForGoodNeedScanFirstMark()
            } else {
                openGoodInfoCreateScreen()
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        goods.value?.let {
            it.getOrNull(position)?.let { item ->
                manager.updateCurrentGood(item.good)
                navigator.goBack()
                if (item.good.markType == MarkType.UNKNOWN)
                    navigator.openGoodInfoCreateScreen()
                else navigator.openMarkedGoodInfoCreateScreen()
            }
        }.orIfNull {
            Logg.e { "goods null" }
            navigator.showInternalError(resource.goodsNotFoundErrorMsg)
        }
    }

    fun onClickNext() {
        navigator.goBack()
    }

    fun onClickProperties() {
        navigator.openBasketPropertiesScreen()
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
                    //Удалим товар из корзины
                    basket.deleteGood(goodFromBasket)
                }
                removeEmptyBasketsAndGoods(task, basket)
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

    private fun removeEmptyBasketsAndGoods(task: TaskOpen, basket: Basket) {
        task.removeEmptyGoods()
        //Если корзина пуста удалим ее из задания и вернемся назад
        if (basket.goods.isEmpty()) {
            task.removeEmptyBaskets()
            navigator.goBack()
        }
    }

    fun onClickClose() {
        navigator.showCloseBasketDialog(yesCallback = {
            lockAndUpdateBasketAndTask(isNeedLock = true)
        })
    }

    fun onClickOpen() {
        navigator.showOpenBasketDialog(yesCallback = {
            lockAndUpdateBasketAndTask(isNeedLock = false)
        })
    }

    private fun lockAndUpdateBasketAndTask(isNeedLock: Boolean) {
        val taskValue = task.value
        val basketValue = basket.value
        if (taskValue != null && basketValue != null) {
            basketValue.isLocked = isNeedLock
            taskValue.updateBasket(basketValue)
            with(manager) {
                updateCurrentBasket(basketValue)
                updateCurrentTask(taskValue)
            }
            navigator.goBack()
        }

    }
}