package com.lenta.bp12.features.basket.basket_good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.platform.extention.getMarkType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
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

class BasketCreateGoodListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var resource: IResourceManager

    /** "ZMP_UTZ_BKS_05_V001"
     * Получение данных товара по ШК / SAP-коду
     */
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var database: IDatabaseRepository

    @Inject
    lateinit var markManager: IMarkManager

    val selectionsHelper = SelectionItemsHelper()

    private val task by unsafeLazy {
        manager.currentTask
    }

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

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val goods by unsafeLazy {
        Logg.e { basket.value.toString() }
        basket.map {
            it?.let { basket ->
                val list = basket.getGoodList()
                list.mapIndexed { index, good ->
                    val units = good.commonUnits.name
                    val quantity = basket.goods[good]

                    Logg.e { "freeVolume: ${basket.freeVolume}, isPrinted: ${basket.isPrinted}, isLocked: ${basket.isLocked} goods: ${basket.goods}" }

                    ItemGoodUi(
                            position = "${index + 1}",
                            name = good.getNameWithMaterial(),
                            quantity = "${quantity.dropZeros()} $units",
                            material = good.material,
                            good = good
                    )
                }
            }.orEmpty()
        }
    }

    val requestFocusToNumberField by unsafeLazy {
        MutableLiveData(true)
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

    // -----------------------------

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

    private fun getGoodByMaterial(material: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByMaterial(material) }
            navigator.hideProgress()
            foundGood?.let(::setFoundGood).orIfNull { loadGoodInfoByMaterial(material) }
        }
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
            foundGood?.let(::setFoundGood).orIfNull { loadGoodInfoByEan(ean) }
        }
    }

    private fun setFoundGood(foundGood: Good) {
        with(navigator) {
            if (manager.isWholesaleTaskType && foundGood.kind == GoodKind.EXCISE) {
                showCantAddExciseGoodForWholesale()
            } else {
                manager.updateCurrentGood(foundGood)
                if (foundGood.markType != MarkType.UNKNOWN) {
                    openMarkedGoodInfoCreateScreen()
                    showForGoodNeedScanFirstMark()
                } else {
                    openGoodInfoCreateScreen()
                }
                Logg.d { "--> found good: $foundGood" }
            }
        }
    }

    private suspend fun loadGoodInfoByEan(ean: String) {
        navigator.showProgressLoadingData(::handleFailure)
        goodInfoNetRequest(
                GoodInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        ean = ean,
                        taskType = task.value?.type?.code.orEmpty()
                )
        ).also {
            navigator.hideProgress()
        }.either(::handleFailure) {
            handleLoadGoodInfoResult(it)
        }
    }

    private suspend fun loadGoodInfoByMaterial(material: String) {
        navigator.showProgressLoadingData(::handleFailure)
        goodInfoNetRequest(
                GoodInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        material = material,
                        taskType = task.value?.type?.code.orEmpty()
                )
        ).also {
            navigator.hideProgress()
        }.either(::handleFailure) {
            handleLoadGoodInfoResult(it)
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

    private fun handleLoadGoodInfoResult(result: GoodInfoResult) {
        launchUITryCatch {
            if (manager.isGoodCanBeAdded(result)) {
                setGood(result)
            } else {
                navigator.showGoodCannotBeAdded()
            }
        }
    }

    /**
     * Метод проверяет маркированный товар пришел или нет.
     * если маркированный, то показываем сообщение о том что нужно сканировать марку,
     * если нет, то создаём его и показываем карточку
     */
    private fun setGood(result: GoodInfoResult) {
        launchUITryCatch {
            with(result) {
                val goodEan = eanInfo?.ean.orEmpty()
                val markType = getMarkType()

                val good = Good(
                        ean = goodEan,
                        eans = database.getEanListByMaterialUnits(
                                material = materialInfo?.material.orEmpty(),
                                unitsCode = materialInfo?.commonUnitsCode.orEmpty()
                        ),
                        material = materialInfo?.material.orEmpty(),
                        name = materialInfo?.name.orEmpty(),
                        kind = getGoodKind(),
                        type = materialInfo?.goodType.orEmpty(),
                        control = getControlType(),
                        section = materialInfo?.section.orEmpty(),
                        matrix = getMatrixType(materialInfo?.matrix.orEmpty()),
                        commonUnits = database.getUnitsByCode(materialInfo?.commonUnitsCode.orEmpty()),
                        innerUnits = database.getUnitsByCode(materialInfo?.innerUnitsCode.orEmpty()),
                        innerQuantity = materialInfo?.innerQuantity?.toDoubleOrNull()
                                ?: 1.0,
                        providers = providers.orEmpty().toMutableList(),
                        producers = producers.orEmpty().toMutableList(),
                        volume = materialInfo?.volume?.toDoubleOrNull() ?: 0.0,
                        markType = markType,
                        markTypeGroup = database.getMarkTypeGroupByMarkType(markType)
                )

                setFoundGood(good)
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        goods.value?.let { goods ->
            goods.getOrNull(position)?.let { item ->
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
            navigator.goBack()
        }
    }

    fun onClickClose() {
        navigator.showCloseBasketDialog(
                yesCallback = {
                    lockAndUpdateBasketAndTask(isNeedLock = true)
                })
    }

    fun onClickOpen() {
        navigator.showOpenBasketDialog(
                yesCallback = {
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