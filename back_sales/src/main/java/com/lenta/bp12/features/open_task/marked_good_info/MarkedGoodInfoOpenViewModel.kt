package com.lenta.bp12.features.open_task.marked_good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.create_task.marked_good_info.GoodProperty
import com.lenta.bp12.features.create_task.marked_good_info.GoodPropertyItem
import com.lenta.bp12.features.open_task.base_good_info.BaseGoodInfoOpenViewModel
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.extentions.addMarks
import com.lenta.bp12.model.pojo.open_task.GoodOpen
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.MarkCartonBoxGoodInfoNetRequest
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MarkedGoodInfoOpenViewModel : BaseGoodInfoOpenViewModel(), PageSelectionListener {

    @Inject
    override lateinit var navigator: IScreenNavigator

    @Inject
    override lateinit var manager: IOpenTaskManager

    @Inject
    lateinit var markManager: IMarkManager

    @Inject
    override lateinit var sessionInfo: ISessionInfo

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

    val selectedPage = MutableLiveData(DEFAULT_PAGE)

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
    private var lastScannedMarks: List<Mark> = listOf()

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
        it?.toDoubleOrNull() ?: DEFAULT_QUANTITY_VALUE
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
                    actionFromGood = true,
                    funcForBox = ::loadBoxInfo,
                    funcForMark = ::checkMark,
                    funcForNotValidBarFormat = navigator::showIncorrectEanFormat
            )
        }
    }

    /**
     * Метод уже конкретно определяет Марка обуви, Коробка, или Блок
     * */
    private fun checkMark(number: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val status = markManager.checkMark(number, WorkType.OPEN)
            Logg.e { status.name }
            when (status) {
                MarkScreenStatus.OK -> {
                    handleOkMark()
                }
                MarkScreenStatus.CARTON_ALREADY_SCANNED -> {
                    handleCartonAlreadyScanned()
                }
                MarkScreenStatus.MARK_ALREADY_SCANNED -> {
                    handleMarkAlreadyScanned()
                }
                MarkScreenStatus.BOX_ALREADY_SCANNED -> {
                    handleBoxAlreadyScanned()
                }
                MarkScreenStatus.FAILURE -> {
                    handleMarkFailure()
                }
                MarkScreenStatus.INCORRECT_EAN_FORMAT -> {
                    handleIncorrectEanFormat()
                }
                MarkScreenStatus.GOOD_CANNOT_BE_ADDED -> {
                    navigator.hideProgress()
                    navigator.showGoodCannotBeAdded()
                }
                MarkScreenStatus.INTERNAL_ERROR -> {
                    handleInternalError()
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
                    handleIncorrectEanFormat()
                }
                MarkScreenStatus.OK_BUT_NEED_TO_SCAN_MARK -> {
                    Unit
                }
                MarkScreenStatus.NO_MARKTYPE_IN_SETTINGS -> {
                    handleNoMarkTypeInSettings()
                }

            }
        }
    }

    override fun loadBoxInfo(number: String) {
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

    private fun handleYesDeleteMappedMarksFromTempCallBack() {
        markManager.handleYesDeleteMappedMarksFromTempCallBack()
        tempMarks.value = markManager.getTempMarks()
    }

    override suspend fun saveChanges() {
        navigator.showProgressLoadingData()

        good.value?.let { good ->
            withContext(Dispatchers.IO) {
                manager.saveGoodInTask(good)
                isExistUnsavedData = false
                addMarks(good)
            }
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }

        navigator.hideProgress()
    }

    private suspend fun addMarks(changedGood: GoodOpen) {
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
        if(isPlannedQuantityActual()) {
            navigator.showQuantityMoreThenPlannedScreen()
            return
        }

        saveChangesAndExit()
    }

    override fun saveChangesAndExit() {
        launchUITryCatch {
            saveChanges()
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
        isExistUnsavedData = true
        tempMarks.value = markManager.getTempMarks()
        properties.value = markManager.getProperties()
        navigator.hideProgress()
    }

    private fun handleNoMarkTypeInSettings() {
        navigator.hideProgress()
        navigator.showNoMarkTypeInSettings()
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

    private fun onInitGoodInfo() {
        launchUITryCatch {
            good.value?.let {
                val tempMarksValue = tempMarks.value
                val size = tempMarksValue?.size
                if (size != null && size > 0) {
                    isExistUnsavedData = true
                    lastScannedMarks = tempMarksValue
                }
            }.orIfNull {
                Logg.e { "good null" }
                navigator.showInternalError(resource.goodNotFoundErrorMsg)
            }
        }
    }

    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_QUANTITY_VALUE = 0.0
    }
}