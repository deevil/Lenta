package com.lenta.bp12.features.create_task.marked_good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.create_task.base.BaseGoodInfoCreateViewModel
import com.lenta.bp12.features.create_task.task_content.TaskContentFragment
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.model.MarkScreenStatus
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.WorkType
import com.lenta.bp12.model.actionByNumber
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.extentions.addMarks
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.request.MarkCartonBoxGoodInfoNetRequest
import com.lenta.bp12.request.ScanInfoResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class MarkedGoodInfoCreateViewModel : BaseGoodInfoCreateViewModel(), PageSelectionListener {

    /**
     * Менеджер ответственный за хранение задания и товаров
     * Имплементация:
     * @see com.lenta.bp12.managers.CreateTaskManager
     * */
    @Inject
    override lateinit var manager: ICreateTaskManager

    /**
     * Менеджер ответственный за обработку марок (не акцизных)
     * Имплементация:
     * @see com.lenta.bp12.managers.MarkManager
     * */
    @Inject
    lateinit var markManager: IMarkManager


    /** ZMP_UTZ_WOB_07_V001
     * «Получение данных по марке/блоку/коробке/товару из ГМ»
     */
    @Inject
    lateinit var markCartonBoxGoodInfoNetRequest: MarkCartonBoxGoodInfoNetRequest


    /**
    Переменные
     */

    private var originalSearchNumber = ""

    val accountingType by unsafeLazy {
        resource.typeMark
    }

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

    val isBasketNumberVisible by unsafeLazy {
        tempMarks.switchMap { tempMarksValue ->
            good.switchMap { goodValue ->
                liveData {
                    val result = goodValue.maxRetailPrice.isEmpty().not() ||
                            tempMarksValue.takeIf { goodValue.isTobacco() }
                                    ?.isNotEmpty()
                                    .orIfNull { true }
                    emit(result)
                }
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
    override val quantityField by unsafeLazy {
        tempMarks.mapSkipNulls {
            "${it.size}"
        }
    }

    override val quantity = quantityField.map {
        it?.toDoubleOrNull() ?: ZERO_QUANTITY
    }

    /**
    МРЦ
     */

    val mrc = MutableLiveData<String>("")

    val isMrcVisible by unsafeLazy {
        good.map {
            it?.markType == MarkType.TOBACCO
        }
    }

    /**
    Кнопки нижнего тулбара
     */
    override val applyEnabled by lazy {
        isProviderSelected.switchMap { isProviderSelected ->
            quantity.switchMap { enteredQuantity ->
                totalQuantity.switchMap { totalQuantity ->
                    basketQuantity.switchMap { basketQuantity ->
                        liveData {
                            val isEnteredQuantityNotZero = enteredQuantity != ZERO_QUANTITY
                            val isTotalQuantityMoreThenZero = totalQuantity > ZERO_QUANTITY
                            val result = isProviderSelected && isEnteredQuantityNotZero && isTotalQuantityMoreThenZero && basketQuantity > ZERO_QUANTITY
                            emit(result)
                        }
                    }
                }
            }
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
                    actionFromGood = true,
                    funcForBox = ::loadBoxInfo,
                    funcForMark = ::checkMark,
                    funcForNotValidBarFormat = navigator::showIncorrectEanFormat
            )
        }
    }

    override fun loadBoxInfo(number: String) {
        launchUITryCatch {
            with(navigator) {
                showProgressLoadingData()
                val result = markManager.loadBoxInfo(number, WorkType.CREATE)
                hideProgress()
                handleMarkResult(result)
            }
        }
    }

    /**
     * Метод уже конкретно определяет Марка обуви, Коробка, или Блок
     * */
    private fun checkMark(number: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val result = markManager.checkMark(number, WorkType.CREATE, true)
            navigator.hideProgress()
            handleMarkResult(result)
        }
    }

    private fun handleMarkResult(result: MarkScreenStatus) {
        with(navigator) {
            when (result) {
                MarkScreenStatus.OK -> setMarksAndProperties()

                MarkScreenStatus.CARTON_ALREADY_SCANNED ->
                    showCartonAlreadyScannedDelete(::handleYesDeleteMappedMarksFromTempCallBack)

                MarkScreenStatus.MARK_ALREADY_SCANNED ->
                    showMarkAlreadyScannedDelete(::handleYesDeleteMappedMarksFromTempCallBack)

                MarkScreenStatus.BOX_ALREADY_SCANNED ->
                    showBoxAlreadyScannedDelete(::handleYesDeleteMappedMarksFromTempCallBack)

                MarkScreenStatus.FAILURE -> handleMarkScanError()

                MarkScreenStatus.INCORRECT_EAN_FORMAT -> showIncorrectEanFormat()

                MarkScreenStatus.GOOD_CANNOT_BE_ADDED -> showGoodCannotBeAdded()

                MarkScreenStatus.INTERNAL_ERROR -> showInternalError(markManager.getInternalErrorMessage())

                MarkScreenStatus.CANT_SCAN_PACK -> showCantScanPackAlert()

                MarkScreenStatus.GOOD_IS_MISSING_IN_TASK -> showGoodIsMissingInTask()

                MarkScreenStatus.MRC_NOT_SAME ->
                    markManager.getCreatedGoodForError()?.let(::showMrcNotSameAlert)

                MarkScreenStatus.MRC_NOT_SAME_IN_BASKET ->
                    showMrcNotSameInBasketAlert(::handleYesSaveCurrentMarkToBasketAndOpenAnother)

                MarkScreenStatus.NOT_MARKED_GOOD -> showIncorrectEanFormat()

                MarkScreenStatus.OK_BUT_NEED_TO_SCAN_MARK -> Unit

                MarkScreenStatus.NOT_SAME_GOOD ->
                    showScannedMarkBelongsToProduct(
                            markManager.getCreatedGoodForError()?.name.orEmpty()
                    )

                MarkScreenStatus.ENTER_MRC_FROM_BOX -> handleEnterMrcFromBox()

                MarkScreenStatus.SOME_MARKS_FROM_BOX_ALREADY_SCANNED -> showScanMarksIndividiuallyAlert()

                else -> showIncorrectEanFormat()

            }
        }
    }


    private fun handleEnterMrcFromBox() {
        navigator.openEnterMrcFromBoxScreen(WorkType.CREATE) {
            navigator.showProgressLoadingData()
            val result = markManager.handleEnterMrcFromBox()
            navigator.hideProgress()
            handleMarkResult(result)
        }
    }

    private fun handleMarkScanError() {
        val failure = markManager.getMarkFailure()
        if (failure is Failure.MessageFailure) {
            navigator.showMarkScanError(failure.message.orEmpty())
        } else {
            handleFailure(failure)
        }
    }

    private fun setMarksAndProperties() {
        tempMarks.value = markManager.getTempMarks()
        properties.value = markManager.getProperties()
        setMrc()
    }

    private fun handleYesSaveCurrentMarkToBasketAndOpenAnother() {
        launchUITryCatch {
            saveChanges()
            markManager.handleYesSaveAndOpenAnotherBox()
            tempMarks.value = markManager.getTempMarks()
            setMrc()
        }
    }

    private fun handleYesDeleteMappedMarksFromTempCallBack() {
        launchAsyncTryCatch {
            markManager.handleYesDeleteMappedMarksFromTempCallBack()
            val tempMarksFromMarkManager = markManager.getTempMarks()
            tempMarks.postValue(tempMarksFromMarkManager)
            setMrc()
        }
    }

    private fun setMrc() {
        val mrcWithoutRub = tempMarks.value?.firstOrNull()?.maxRetailPrice
        val mrcWithRub = mrcWithoutRub?.let ( resource::mrcSpaceRub ).orEmpty()
        val goodValue = good.value?.apply { maxRetailPrice = mrcWithoutRub.orEmpty() }
        mrc.postValue(mrcWithRub)
        good.postValue(goodValue)
    }

    override suspend fun saveChanges(result: ScanInfoResult?) {
        good.value?.let { good ->
            manager.saveGoodInTask(good)
            addMarks(good)
        }.orIfNull {
            Logg.e { "good null" }
            navigator.showInternalError(resource.goodNotFoundErrorMsg)
        }
    }

    private suspend fun addMarks(changedGood: Good) {
        tempMarks.value?.let { tempMarksValue ->
            changedGood.addMarks(tempMarksValue)
            manager.addGoodToBasketWithMarks(changedGood, tempMarksValue, getProvider())
        }
    }

    /**
    Обработка нажатий кнопок
     */

    override fun onBackPressed() {
        handleBackPress<TaskContentFragment>()
        markManager.clearData()
    }

    override fun isExistUnsavedData(): Boolean {
        val size = tempMarks.value?.size
        val isSizeIsNotZero = size != null && size > 0
        val isProducerChanged = isProducerEnabledAndChanged() == true
        return isSizeIsNotZero || isProducerChanged
    }

    override fun onClickRollback() {
        thereWasRollback = true
        markManager.onRollback()
        tempMarks.value = markManager.getTempMarks()
    }

    override fun onClickApply() {
        saveChangesAndExit()
    }

    override fun saveChangesAndExit(result: ScanInfoResult?) {
        launchUITryCatch {
            with(navigator) {
                showProgressLoadingData()
                saveChanges(result)
                hideProgress()
                openBasketCreateGoodListScreen()
                manager.isBasketsNeedsToBeClosed = false
                markManager.clearData()
            }
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
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
                    lastScannedMarks = tempMarksValue
                }
            }.orIfNull {
                Logg.e { "good null" }
                navigator.showInternalError(resource.goodNotFoundErrorMsg)
            }
        }
    }
}