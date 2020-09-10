package com.lenta.bp12.features.create_task.marked_good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.create_task.base_good_info.BaseGoodInfoCreateViewModel
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.model.MarkScreenStatus
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.WorkType
import com.lenta.bp12.model.actionByNumber
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.extentions.addMarks
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.MarkCartonBoxGoodInfoNetRequest
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class MarkedGoodInfoCreateViewModel : BaseGoodInfoCreateViewModel(), PageSelectionListener {

    @Inject
    override lateinit var navigator: IScreenNavigator

    /**
     * Менеджер ответственный за хранение задания и товаров
     * Имплементация:
     * @see com.lenta.bp12.managers.CreateTaskManager
     * */
    @Inject
    override lateinit var manager: ICreateTaskManager

    @Inject
    override lateinit var sessionInfo: ISessionInfo

    /**
     * Менеджер ответственный за обработку марок (не акцизных)
     * Имплементация:
     * @see com.lenta.bp12.managers.MarkManager
     * */
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
    override lateinit var scanInfoNetRequest: ScanInfoNetRequest

    /** ZMP_UTZ_WOB_07_V001
     * «Получение данных по марке/блоку/коробке/товару из ГМ»
     */
    @Inject
    lateinit var markCartonBoxGoodInfoNetRequest: MarkCartonBoxGoodInfoNetRequest

    @Inject
    override lateinit var database: IDatabaseRepository

    @Inject
    override lateinit var resource: IResourceManager

    val selectedPage = MutableLiveData(0)

    /**
    Переменные
     */

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
    private val tempMarks = MutableLiveData(mutableListOf<Mark>())

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

    override val quantity = quantityField.map {
        it?.toDoubleOrNull() ?: 0.0
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

    override val applyEnabled by lazy {
        isProviderSelected
                .combineLatest(good)
                .combineLatest(quantity)
                .combineLatest(totalQuantity)
                .combineLatest(basketQuantity)
                .map {
                    it?.let {
                        val isProviderSelected = it.first.first.first.first
                        val enteredQuantity = it.first.first.second
                        val totalQuantity = it.first.second
                        val basketQuantity = it.second

                        val isEnteredQuantityNotZero = enteredQuantity != 0.0
                        val isTotalQuantityMoreThenZero = totalQuantity > 0.0

                        isProviderSelected && isEnteredQuantityNotZero && isTotalQuantityMoreThenZero && basketQuantity > 0.0
                    } ?: false
                }
    }


    val rollbackEnabled = tempMarks.map {
        it?.isEmpty()?.not()
    }

    /**
    Блок инициализации
     */

    init {
        onInitGoodInfo()
    }

    /**
    Методы
     */

    /**
     * Метод actionByNumber - общий, просто определяет марка внутри или коробка
     * */
    override fun checkSearchNumber(number: String) {
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

    override fun loadBoxInfo(number: String) {
        launchUITryCatch {
            when (markManager.loadBoxInfo(number)) {
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
                MarkScreenStatus.MRC_NOT_SAME_IN_BASKET -> {
                    handleMrcNotSameInBasket()
                }
                MarkScreenStatus.NOT_MARKED_GOOD -> {
                    navigator.hideProgress()
                    navigator.showIncorrectEanFormat()
                }
                MarkScreenStatus.OK_BUT_NEED_TO_SCAN_MARK -> {
                    Unit
                }
                MarkScreenStatus.NOT_SAME_GOOD -> {
                    navigator.hideProgress()
                    navigator.showScannedMarkBelongsToProduct(
                            markManager.getCreatedGoodForError()?.name.orEmpty()
                    )
                }
                else -> {
                    navigator.hideProgress()
                    navigator.showIncorrectEanFormat()
                }
            }
        }
    }

    private fun handleMrcNotSameInBasket() {
        navigator.hideProgress()
        navigator.showMrcNotSameInBasketAlert(
                yesCallback = ::handleYesSaveCurrentMarkToBasketAndOpenAnother
        )

    }

    private fun handleYesSaveCurrentMarkToBasketAndOpenAnother() {
        launchUITryCatch {

            Logg.e { "tempMarks: ${tempMarks.value}, tempMarksFromManager: ${markManager.getTempMarks()}" }
            saveChanges()

            markManager.handleYesSaveAndOpenAnotherBox()

            tempMarks.value = markManager.getTempMarks()
        }
    }

    private fun handleYesDeleteMappedMarksFromTempCallBack() {
        launchAsyncTryCatch {
            markManager.handleYesDeleteMappedMarksFromTempCallBack()
            val tempMarksFromMarkManager = markManager.getTempMarks()
            tempMarks.postValue(tempMarksFromMarkManager)
        }
    }


    override suspend fun saveChanges() {
        good.value?.let { good ->
            manager.saveGoodInTask(good)
            isExistUnsavedData = false
            addMarks(good)
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private suspend fun addMarks(changedGood: Good) {
        tempMarks.value?.let { tempMarksValue ->
            changedGood.addMarks(tempMarksValue)
            Logg.e { "$changedGood" }
            manager.addGoodToBasketWithMarks(changedGood, tempMarksValue, getProvider())
        }
    }

    /**
    Обработка нажатий кнопок
     */

    override fun onBackPressed() {
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

    override fun onClickRollback() {
        thereWasRollback = true
        markManager.onRollback()
        tempMarks.value = markManager.getTempMarks()
    }

    override fun onClickApply() {
        saveChangesAndExit()
    }

    override fun saveChangesAndExit() {
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
        } ?: Logg.e { "marks empty " }
        propertiesFromBundle?.let { listOfProperties ->
            properties.value?.addAll(listOfProperties)
        } ?: Logg.e { "properties empty " }
    }

    private fun onInitGoodInfo() {
        launchUITryCatch {
            good.value?.let {
                tempMarks.value = markManager.getTempMarks()
                properties.value = markManager.getProperties()
                updateProviders(it.providers)
                val tempMarksValue = tempMarks.value
                val size = tempMarksValue?.size
                if (size != null && size > 0) {
                    isExistUnsavedData = true
                    lastScannedMarks = tempMarksValue
                }

                Logg.e {
                    it.maxRetailPrice.toString()
                }
            }.orIfNull {
                Logg.e { "good null" }
                navigator.showInternalError(resource.goodNotFoundErrorMsg)
            }
        }
    }
}