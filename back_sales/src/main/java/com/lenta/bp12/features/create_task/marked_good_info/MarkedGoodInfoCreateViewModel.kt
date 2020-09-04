package com.lenta.bp12.features.create_task.marked_good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.model.pojo.extentions.addMarks
import com.lenta.bp12.model.pojo.extentions.getQuantityOfGood
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.MarkCartonBoxGoodInfoNetRequest
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class MarkedGoodInfoCreateViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var markManager: IMarkManager

    /** "ZMP_UTZ_BKS_05_V001"
     * Получение данных товара по ШК / SAP-коду
     */
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    /** "ZMP_UTZ_100_V001"
     * Получение данных по акцизному товару  */
    @Inject
    lateinit var scanInfoNetRequest: ScanInfoNetRequest

    /** ZMP_UTZ_WOB_07_V001
     * «Получение данных по марке/блоку/коробке/товару из ГМ»
     */
    @Inject
    lateinit var markCartonBoxGoodInfoNetRequest: MarkCartonBoxGoodInfoNetRequest

    @Inject
    lateinit var database: IDatabaseRepository

    @Inject
    lateinit var resource: IResourceManager

    val selectedPage = MutableLiveData(0)

    /**
    Переменные
     */

    val task by unsafeLazy {
        manager.currentTask
    }

    val good by unsafeLazy {
        manager.currentGood
    }

    val title by unsafeLazy {
        good.map { good ->
            good?.getNameWithMaterial() ?: task.value?.getFormattedName()
        }
    }

    private var originalSearchNumber = ""

    val accountingType by unsafeLazy {
        resource.typeMark()
    }

    private var isExistUnsavedData = false


    private var thereWasRollback = false

    val properties = MutableLiveData(mutableListOf<GoodProperty>())

    val propertiesItems by unsafeLazy {
        properties.switchMap { properties ->
            liveData {
                val items = properties.mapIndexed { index, property ->
                    GoodPropertyItem(
                            position = "${index + 1}",
                            gtin = property.gtin,
                            property = property.property,
                            value = property.value
                    )
                }
                emit(items)
            }
        }
    }

    /**
     * Все сканированные марки хранятся в этом списке до нажатия кнопки применить.
     * После нажатия применить все марки обрабатываются менедежером по корзинам и сохраняется в задании.
     * */
    val tempMarks = MutableLiveData(mutableListOf<Mark>())

    /**
     * Последние отсканированные марки, для удаления их из общего списка по кнопке Откат
     * */
    private var lastScannedMarks: List<Mark> = emptyList()

    /**
    Ввод количества
     */
    val quantityField by unsafeLazy {
        tempMarks.switchMap {
            liveData {
                val size = "${it.size}"
                emit(size)
            }
        }
    }

    val quantity = quantityField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    val quantityFieldEnabled by lazy {
        false
    }

    /**
    Количество товара итого
     */

    val totalTitle by lazy {
        good.map { good ->
            resource.totalWithConvertingInfo(good?.getConvertingInfo().orEmpty())
        }
    }

    private val totalQuantity by lazy {
        good.combineLatest(quantity).map {
            it?.let {
                val total = it.first.getTotalQuantity()
                val current = it.second

                total.sumWith(current)
            }
        }
    }

    val totalWithUnits by lazy {
        totalQuantity.map { quantity ->
            "${quantity.dropZeros()} ${good.value?.commonUnits?.name}"
        }
    }

    /**
    Количество товара по корзинам
     */

    val basketTitle by lazy {
        MutableLiveData(resource.byBasket())
    }

    val basketNumber by lazy {
        good.combineLatest(quantity).map {
            it?.let {
                task.value?.let { task ->
                    getBasket()?.let { basket ->
                        "${task.baskets.indexOf(basket) + 1}"
                    } ?: "${task.baskets.size + 1}"
                }.orEmpty()
            }
        }
    }

    private val basketQuantity by lazy {
        good.combineLatest(quantity).map {
            it?.let {
                val good = it.first
                val enteredQuantity = it.second

                getBasket()?.getQuantityOfGood(good)?.sumWith(enteredQuantity)
                        ?: enteredQuantity
            }
        }
    }

    val basketQuantityWithUnits by lazy {
        good.combineLatest(basketQuantity).map {
            it?.let {
                val (good, quantity) = it
                "${quantity.dropZeros()} ${good.commonUnits.name}"
            }
        }
    }

    /**
    МРЦ
     */

    val mrc by unsafeLazy {
        good.map { goodValue ->
            goodValue?.let { good ->
                val mrc = good.maxRetailPrice
                mrc.takeIf { it.isNotEmpty() }
                        ?.run { "${good.maxRetailPrice} ${resource.rub}" }.orEmpty()
            }
        }
    }

    val isMrcVisible by unsafeLazy {
        good.map {
            it?.markType == MarkType.TOBACCO
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val applyEnabled by lazy {
        good.combineLatest(quantity)
                .combineLatest(totalQuantity)
                .combineLatest(basketQuantity)
                .map {
                    it?.let {
                        val enteredQuantity = it.first.first.second
                        val totalQuantity = it.first.second
                        val basketQuantity = it.second

                        val isEnteredQuantityNotZero = enteredQuantity != 0.0
                        val isTotalQuantityMoreThenZero = totalQuantity > 0.0

                        isEnteredQuantityNotZero && isTotalQuantityMoreThenZero && basketQuantity > 0.0
                    } ?: false
                }
    }

    val rollbackVisibility = MutableLiveData(true)

    val rollbackEnabled = tempMarks.map {
        it?.isEmpty()?.not()
    }

    /**
    Блок инициализации
     */

    init {
        launchUITryCatch {
            good.value?.let {
                tempMarks.value = markManager.getTempMarks()
                properties.value = markManager.getProperties()
                val tempMarksValue = tempMarks.value
                val size = tempMarksValue?.size
                if (size != null && size > 0) {
                    isExistUnsavedData = true
                    lastScannedMarks = tempMarksValue
                }
            }.orIfNull {
                manager.clearCurrentGood()
                checkSearchNumber(manager.searchNumber)
            }
        }
    }

    /**
    Методы
     */

    fun onScanResult(number: String) {
        launchUITryCatch {
            manager.clearSearchFromListParams()
            checkSearchNumber(number)
        }
    }

    /**
     * Метод actionByNumber - общий, просто определяет марка внутри или коробка
     * */
    private fun checkSearchNumber(number: String) {
        originalSearchNumber = number
        good.value?.let { goodValue ->
            Logg.e {
                goodValue.toString()
            }
            actionByNumber(
                    number = number,
                    funcForBox = ::loadBoxInfo,
                    funcForMark = ::checkMark,
                    funcForNotValidBarFormat = navigator::showIncorrectEanFormat
            )
        }
    }

    private fun loadBoxInfo(number: String) {
        launchUITryCatch {
            val screenStatus = markManager.loadBoxInfo(number)
            when (screenStatus) {
                MarkScreenStatus.OK -> {
                    handleOkMark()
                }
                MarkScreenStatus.INTERNAL_ERROR -> {
                    handleInternalError()
                }
                MarkScreenStatus.FAILURE -> {
                    handleMarkFailure()
                }
                MarkScreenStatus.MARK_ALREADY_SCANNED -> {
                    handleMarkAlreadyScanned()
                }
                MarkScreenStatus.CARTON_ALREADY_SCANNED -> {
                    handleCartonAlreadyScanned()
                }
                MarkScreenStatus.BOX_ALREADY_SCANNED -> {
                    handleBoxAlreadyScanned()
                }
                else -> {
                    handleIncorrectEanFormat()
                }
            }
        }
    }

    /**
     * Метод уже конкретно определяет Марка обуви, Коробка, или Блок
     * */
    private fun checkMark(number: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            when (markManager.checkMark(number, WorkType.CREATE)) {
                MarkScreenStatus.OK -> {
                    tempMarks.value = markManager.getTempMarks()
                    properties.value = markManager.getProperties()
                    navigator.hideProgress()
                }
                MarkScreenStatus.CARTON_ALREADY_SCANNED -> {
                    navigator.hideProgress()
                    navigator.showCartonAlreadyScannedDelete(
                            yesCallback = ::handleYesDeleteMappedMarksFromTempCallBack
                    )
                }
                MarkScreenStatus.MARK_ALREADY_SCANNED -> {
                    navigator.hideProgress()
                    navigator.showMarkAlreadyScannedDelete(
                            yesCallback = ::handleYesDeleteMappedMarksFromTempCallBack
                    )
                }
                MarkScreenStatus.BOX_ALREADY_SCANNED -> {
                    navigator.hideProgress()
                    navigator.showBoxAlreadyScannedDelete(
                            yesCallback = ::handleYesDeleteMappedMarksFromTempCallBack
                    )
                }
                MarkScreenStatus.FAILURE -> {
                    navigator.hideProgress()
                    handleFailure(markManager.getMarkFailure())
                }
                MarkScreenStatus.INCORRECT_EAN_FORMAT -> {
                    navigator.hideProgress()
                    navigator.showIncorrectEanFormat()
                }
                MarkScreenStatus.GOOD_CANNOT_BE_ADDED -> {
                    navigator.hideProgress()
                    navigator.showGoodCannotBeAdded()
                }
                MarkScreenStatus.INTERNAL_ERROR -> {
                    navigator.hideProgress()
                    navigator.showInternalError(
                            cause = markManager.getInternalErrorMessage()
                    )
                }
                MarkScreenStatus.CANT_SCAN_PACK -> {
                    navigator.hideProgress()
                    navigator.showCantScanPackAlert()
                }
                MarkScreenStatus.GOOD_IS_MISSING_IN_TASK -> {
                    navigator.hideProgress()
                    navigator.showGoodIsMissingInTask()
                }
                MarkScreenStatus.MRC_NOT_SAME -> {
                    navigator.hideProgress()
                    markManager.getCreatedGoodForError()?.let {
                        navigator.showMrcNotSameAlert(it)
                    }
                }
                MarkScreenStatus.NOT_MARKED_GOOD -> {
                    navigator.hideProgress()
                    navigator.showIncorrectEanFormat()
                }
                MarkScreenStatus.OK_BUT_NEED_TO_SCAN_MARK -> {
                    Unit
                }
            }
        }
    }


    private fun handleYesDeleteMappedMarksFromTempCallBack() {
        markManager.handleYesDeleteMappedMarksFromTempCallBack()
        tempMarks.value = markManager.getTempMarks()
    }

    private fun getBasket(): Basket? {
        return manager.getBasket(ProviderInfo.getEmptyCode())
    }

    private suspend fun saveChanges() {
        good.value?.let { good ->
            manager.saveGoodInTask(good)
            isExistUnsavedData = false
            addMarks(good)
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }


    }

    private suspend fun addMarks(changedGood: GoodCreate) {
        tempMarks.value?.let { tempMarksValue ->
            changedGood.addMarks(tempMarksValue)
            tempMarksValue.forEach { mark ->
                manager.addGoodToBasketWithMark(
                        good = changedGood,
                        mark = mark,
                        provider = ProviderInfo.getEmptyProvider()
                )
            }
        }
    }

    fun updateData() {
        val good = good.value
        if (manager.isWasAddedProvider && good != null) {
            manager.isWasAddedProvider = false
        }
    }

    /**
    Обработка нажатий кнопок
     */

    fun onBackPressed() {
        if (isExistUnsavedData) {
            navigator.showUnsavedDataWillBeLost {
                manager.clearSearchFromListParams()
                navigator.goBack()
            }
        } else {
            navigator.goBack()
        }
        markManager.clearData()
    }

    fun onClickRollback() {
        thereWasRollback = true
        markManager.onRollback()
        tempMarks.value = markManager.getTempMarks()
    }

    fun onClickDetails() {
        manager.updateCurrentGood(good.value)
        navigator.openGoodDetailsCreateScreen()
    }

    fun onClickApply() {
        saveChangesAndExit()
    }

    private fun saveChangesAndExit() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            saveChanges()
            navigator.hideProgress()
            navigator.goBack()
            navigator.openBasketCreateGoodListScreen()
            manager.isBasketsNeedsToBeClosed = false
            markManager.clearData()
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun handleCartonAlreadyScanned() {
        navigator.hideProgress()
        navigator.showCartonAlreadyScannedDelete(
                yesCallback = ::handleYesDeleteMappedMarksFromTempCallBack
        )
    }

    private fun handleMarkAlreadyScanned() {
        navigator.hideProgress()
        navigator.showMarkAlreadyScannedDelete(
                yesCallback = ::handleYesDeleteMappedMarksFromTempCallBack
        )
    }

    private fun handleBoxAlreadyScanned() {
        navigator.hideProgress()
        navigator.showBoxAlreadyScannedDelete(
                yesCallback = ::handleYesDeleteMappedMarksFromTempCallBack
        )
    }

    private fun handleInternalError() {
        navigator.hideProgress()
        navigator.showInternalError(
                cause = markManager.getInternalErrorMessage()
        )
    }

    private fun handleMarkFailure() {
        navigator.hideProgress()
        handleFailure(markManager.getMarkFailure())
    }

    private fun handleIncorrectEanFormat() {
        navigator.hideProgress()
        navigator.showIncorrectEanFormat()
    }

    private fun handleOkMark() {
        tempMarks.value = markManager.getTempMarks()
        properties.value = markManager.getProperties()
        navigator.hideProgress()
    }

    fun setupData(marksFromBundle: List<Mark>?, propertiesFromBundle: List<GoodProperty>?) {
        marksFromBundle?.let { listOfMarks ->
            tempMarks.value?.addAll(listOfMarks)
            Logg.e { marksFromBundle.toString() }
        } ?: Logg.e { "marks empty " }
        propertiesFromBundle?.let { listOfProperties ->
            properties.value?.addAll(listOfProperties)
            Logg.e { propertiesFromBundle.toString() }
        } ?: Logg.e { "properties empty " }
    }
}