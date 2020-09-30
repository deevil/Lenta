package com.lenta.bp12.features.open_task.marked_good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.create_task.marked_good_info.GoodProperty
import com.lenta.bp12.features.create_task.marked_good_info.GoodPropertyItem
import com.lenta.bp12.features.open_task.base.BaseGoodInfoOpenViewModel
import com.lenta.bp12.features.open_task.good_list.GoodListFragment
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MarkedGoodInfoOpenViewModel : BaseGoodInfoOpenViewModel(), PageSelectionListener {

    @Inject
    override lateinit var manager: IOpenTaskManager

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

    val isBasketNumberVisible by unsafeLazy {
        tempMarks.mapSkipNulls { tempMarksValue ->
            good.mapSkipNulls { goodValue ->
                goodValue.maxRetailPrice.isEmpty().not() ||
                        tempMarksValue.takeIf { goodValue.isTobacco() }
                                ?.isNotEmpty()
                                .orIfNull { true }
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
            with(navigator) {
                showProgressLoadingData()
                val status = markManager.checkMark(number, WorkType.OPEN, true)
                Logg.d { status.name }
                hideProgress()
                when (status) {
                    MarkScreenStatus.OK -> setMarksAndProperties()

                    MarkScreenStatus.CARTON_ALREADY_SCANNED ->
                        showCartonAlreadyScannedDelete(::handleYesDeleteMappedMarksFromTempCallBack)

                    MarkScreenStatus.MARK_ALREADY_SCANNED ->
                        showMarkAlreadyScannedDelete(::handleYesDeleteMappedMarksFromTempCallBack)

                    MarkScreenStatus.BOX_ALREADY_SCANNED ->
                        showBoxAlreadyScannedDelete(::handleYesDeleteMappedMarksFromTempCallBack)

                    MarkScreenStatus.FAILURE -> handleMarkScanError()

                    MarkScreenStatus.GOOD_CANNOT_BE_ADDED -> showGoodCannotBeAdded()

                    MarkScreenStatus.INTERNAL_ERROR ->
                        showInternalError(markManager.getInternalErrorMessage())

                    MarkScreenStatus.CANT_SCAN_PACK -> showCantScanPackAlert()

                    MarkScreenStatus.GOOD_IS_MISSING_IN_TASK -> showGoodIsMissingInTask()

                    MarkScreenStatus.MRC_NOT_SAME ->
                        markManager.getCreatedGoodForError()?.let(::showMrcNotSameAlert)

                    MarkScreenStatus.MRC_NOT_SAME_IN_BASKET ->
                        showMrcNotSameInBasketAlert(::handleYesSaveCurrentMarkToBasketAndOpenAnother)

                    MarkScreenStatus.OK_BUT_NEED_TO_SCAN_MARK -> Unit

                    MarkScreenStatus.NO_MARKTYPE_IN_SETTINGS -> showNoMarkTypeInSettings()

                    MarkScreenStatus.NOT_SAME_GOOD ->
                        showScannedMarkBelongsToProduct(
                                productName = markManager.getCreatedGoodForError()?.name.orEmpty()
                        )

                    MarkScreenStatus.MARKS_MORE_THAN_PLANNED -> showQuantityMoreThanPlannedScreen()

                    else -> showIncorrectEanFormat()
                }
            }
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

    private fun handleYesSaveCurrentMarkToBasketAndOpenAnother() {
        launchUITryCatch {
            saveChanges()
            markManager.handleYesSaveAndOpenAnotherBox()
            tempMarks.value = markManager.getTempMarks()
            setMrc()
        }
    }

    override fun loadBoxInfo(number: String) {
        launchUITryCatch {
            with(navigator) {
                showProgressLoadingData()
                val screenStatus = markManager.loadBoxInfo(number, WorkType.OPEN)
                hideProgress()
                when (screenStatus) {
                    MarkScreenStatus.OK -> setMarksAndProperties()

                    MarkScreenStatus.INTERNAL_ERROR ->
                        showInternalError(markManager.getInternalErrorMessage())

                    MarkScreenStatus.FAILURE -> handleMarkScanError()

                    MarkScreenStatus.MARK_ALREADY_SCANNED ->
                        showMarkAlreadyScannedDelete(::handleYesDeleteMappedMarksFromTempCallBack)

                    MarkScreenStatus.CARTON_ALREADY_SCANNED ->
                        showCartonAlreadyScannedDelete(::handleYesDeleteMappedMarksFromTempCallBack)

                    MarkScreenStatus.BOX_ALREADY_SCANNED ->
                        showBoxAlreadyScannedDelete(::handleYesDeleteMappedMarksFromTempCallBack)

                    else -> showIncorrectEanFormat()
                }
            }
        }
    }

    private fun handleYesDeleteMappedMarksFromTempCallBack() {
        launchAsyncTryCatch {
            markManager.handleYesDeleteMappedMarksFromTempCallBack()
            val tempMarksFromMarkManager = markManager.getTempMarks()
            tempMarks.postValue(tempMarksFromMarkManager)
            isExistUnsavedData = true
            setMrc()
        }
    }

    override suspend fun saveChanges(result: ScanInfoResult?) {
        with(navigator){
            showProgressLoadingData()

            good.value?.let { good ->
                withContext(Dispatchers.IO) {
                    manager.saveGoodInTask(good)
                    addMarks(good)
                }
            }.orIfNull {
                Logg.e { "good null" }
                showInternalError(resource.goodNotFoundErrorMsg)
            }

            hideProgress()
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
        with(navigator) {
            val isBasketEmpty = manager.currentBasket.value?.goods?.isEmpty() == true

            fun action() = if (isBasketEmpty){
                goBackTo(GoodListFragment::class.simpleName)
            } else {
                goBack()
            }

            if (isExistUnsavedData) {
                showUnsavedDataWillBeLost {
                    action()
                }
            } else {
                action()
            }
            markManager.clearData()
        }
    }

    override fun onClickRollback() {
        thereWasRollback = true
        markManager.onRollback()
        tempMarks.value = markManager.getTempMarks()
    }


    override fun onClickApply() {
        if (isFactQuantityMoreThanPlanned()) {
            navigator.showQuantityMoreThanPlannedScreen()
            return
        }

        saveChangesAndExit()
    }

    override fun saveChangesAndExit(result: ScanInfoResult?) {
        launchUITryCatch {
            with(navigator) {
                isExistUnsavedData = false
                showProgressLoadingData()
                saveChanges(result)
                hideProgress()
                openBasketOpenGoodListScreen()
                manager.isBasketsNeedsToBeClosed = false
                markManager.clearData()
            }
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun setMarksAndProperties() {
        isExistUnsavedData = true
        tempMarks.value = markManager.getTempMarks()
        properties.value = markManager.getProperties()
        setMrc()
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


    private fun setMrc() {
        val newMrc = tempMarks.value?.firstOrNull()?.run {
            resource.mrcSpaceRub(maxRetailPrice)
        }.orEmpty()
        mrc.postValue(newMrc)
    }
}