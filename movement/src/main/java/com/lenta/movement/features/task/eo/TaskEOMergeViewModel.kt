package com.lenta.movement.features.task.eo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.movement.R
import com.lenta.movement.exception.PersonnelNumberFailure
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.*
import com.lenta.movement.models.repositories.ICargoUnitRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.ConsolidationNetRequest
import com.lenta.movement.requests.network.DocumentsToPrintNetRequest
import com.lenta.movement.requests.network.EndConsolidationNetRequest
import com.lenta.movement.requests.network.models.RestCargoUnit
import com.lenta.movement.requests.network.models.consolidation.ConsolidationParams
import com.lenta.movement.requests.network.models.consolidation.ConsolidationProcessingUnit
import com.lenta.movement.requests.network.models.documentsToPrint.DocumentsToPrintParams
import com.lenta.movement.requests.network.models.endConsolidation.EndConsolidationParams
import com.lenta.movement.requests.network.models.toModelList
import com.lenta.movement.requests.network.models.toTask
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

class TaskEOMergeViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var cargoUnitRepository: ICargoUnitRepository

    @Inject
    lateinit var formatter: IFormatter

    @Inject
    lateinit var consolidationNetRequest: ConsolidationNetRequest

    @Inject
    lateinit var endConsolidationNetRequest: EndConsolidationNetRequest

    @Inject
    lateinit var documentsToPrintNetRequest: DocumentsToPrintNetRequest

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    val eoSelectionHelper = SelectionItemsHelper()
    val geSelectionHelper = SelectionItemsHelper()

    val currentPage = selectedPage.mapSkipNulls { TaskEOMergePage.values()[it] }

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val eoItemList = MutableLiveData<List<EoListItem>>()
    val geItemList = MutableLiveData<List<SimpleListItem>>()

    private val eoListLiveData by unsafeLazy {
        MutableLiveData<List<ProcessingUnit>>()
    }

    private fun choseResIdByEOState(state: ProcessingUnit.State): Int {
        return when (state) {
            ProcessingUnit.State.NOT_PROCESSED -> EMPTY_GE_STATE_RESOURCE_ID
            ProcessingUnit.State.TOP_LEVEL_EO -> R.drawable.ic_top_level_processing_unit_32dp
            ProcessingUnit.State.COMBINED -> R.drawable.ic_cargo_unit_32dp
        }
    }

    val isProcessBtnVisible by unsafeLazy {
        currentPage.map { page ->
            page == TaskEOMergePage.EO_LIST
        }
    }

    val isExcludeBtnVisible by unsafeLazy {
        currentPage.map { page ->
            page == TaskEOMergePage.GE_LIST
        }
    }

    val isSaveBtnEnabled by unsafeLazy {
        eoListLiveData.map { eoList ->
            eoList?.any {
                it.state == ProcessingUnit.State.NOT_PROCESSED
            }?.not()
        }
    }

    val isExcludeBtnEnabled by unsafeLazy {
        geSelectionHelper.selectedPositions.map { setOfSelectedItems ->
            setOfSelectedItems?.size?.let {
                it > 0
            }
        }
    }

    fun onResume() {
        updateEoItemList()
        updateGeItemList()
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun getTitle(): String {
        return "${taskManager.getTask().taskType.shortName} // ${taskManager.getTask().name}"
    }

    fun onProcessBtnClick() {
        eoSelectionHelper.selectedPositions.value?.let { listOfSelected ->
            val eoListValue = cargoUnitRepository.getEOList()
            val numberOfSelectedItems = listOfSelected.size
            val notProcessedEOList = eoListValue.mapNotNull { eo ->
                val eoNumber = eo.processingUnitNumber
                eoNumber.takeIf { eo.state == ProcessingUnit.State.NOT_PROCESSED }
                        ?.let { ConsolidationProcessingUnit(it) }
            }
            when (numberOfSelectedItems) {
                NO_ITEM_SELECTED -> onNoItemSelectedProcessing(notProcessedEOList)
                ONE_ITEM_SELECTED -> onOneItemSelectedProcessing(listOfSelected, eoListValue)
                else -> onMoreThanOneItemSelectedProcessing(notProcessedEOList, listOfSelected)
            }
            eoSelectionHelper.clearPositions()
        }
    }

    private fun onNoItemSelectedProcessing(notProcessedEOList: List<ConsolidationProcessingUnit>) {
        screenNavigator.openZeroSelectedEODialog(
                processCallbackFunc = ::addAllEOAsGE,
                combineCallbackFunc = { combineEO(notProcessedEOList) }
        )
    }

    private fun combineEO(notProcessedEOList: List<ConsolidationProcessingUnit>) {
        if (notProcessedEOList.isNotEmpty())
            consolidate(
                    sendEOList = notProcessedEOList,
                    sendGEList = listOf(),
                    mode = ConsolidationNetRequest.CONSOLIDATION_EO_IN_GE_MODE
            )
    }

    private fun addAllEOAsGE() {
        val eoListValue = cargoUnitRepository.getEOList()
        val geListValue = cargoUnitRepository.getGEList().let { geListValue ->
            eoListValue.filter { eo ->
                eo.state == ProcessingUnit.State.NOT_PROCESSED
                        && geListValue.all { it.number != eo.processingUnitNumber }
            }.mapTo(geListValue) { eo ->
                eo.state = ProcessingUnit.State.TOP_LEVEL_EO
                CargoUnit(eo.processingUnitNumber, mutableListOf())
            }
        }
        cargoUnitRepository.setGE(geListValue.toMutableList())
        updateEoItemList()
        updateGeItemList()
        changeTabToGEList()
        eoSelectionHelper.clearPositions()
    }

    private fun onOneItemSelectedProcessing(
            listOfSelected: MutableSet<Int>,
            eoListValue: List<ProcessingUnit>) {
        val geListValue = cargoUnitRepository.getGEList()
        val eoListIndex = listOfSelected.first()
        val eo = eoListValue[eoListIndex]
        val newCargoUnit =
                CargoUnit(eo.processingUnitNumber, mutableListOf())
        if (eo.state == ProcessingUnit.State.NOT_PROCESSED
                && geListValue.contains(newCargoUnit).not()) {
            geListValue.add(newCargoUnit)
            eo.state = ProcessingUnit.State.TOP_LEVEL_EO
            updateEoItemList()
            updateGeItemList()
            changeTabToGEList()
        }
    }

    private fun onMoreThanOneItemSelectedProcessing(
            notProcessedEOList: List<ConsolidationProcessingUnit>,
            listOfSelected: MutableSet<Int>) {
        if (notProcessedEOList.isNotEmpty()) {
            val selectedEO = listOfSelected.mapTo(mutableListOf()) {
                notProcessedEOList[it]
            }
            consolidate(
                    sendEOList = selectedEO,
                    sendGEList = emptyList(),
                    mode = ConsolidationNetRequest.CONSOLIDATION_EO_IN_GE_MODE
            )
        } else {
            Logg.e { "Выделенные элементы уже обработаны" }
        }
    }

    private fun consolidate(sendEOList: List<ConsolidationProcessingUnit>, sendGEList: List<RestCargoUnit>, mode: String) {
        launchUITryCatch {
            screenNavigator.showProgress(consolidationNetRequest)
            val either = sessionInfo.personnelNumber?.let { personnelNumber ->
                val params = ConsolidationParams(
                        deviceIp = context.getDeviceIp(),
                        taskNumber = taskManager.getTask().number,
                        personnelNumber = personnelNumber,
                        mode = mode,
                        eoNumberList = sendEOList,
                        geNumberList = sendGEList
                )
                consolidationNetRequest(params)
            } ?: Either.Left(
                    PersonnelNumberFailure(
                            context.getString(R.string.alert_null_personnel_number)
                    )
            )

            either.either({ failure ->
                screenNavigator.hideProgress()
                screenNavigator.openAlertScreen(failure)
            }, { result ->
                val resultGeList = result.geList
                val eoListValue = cargoUnitRepository.getEOList()
                when (mode) {
                    ConsolidationNetRequest.CONSOLIDATION_EO_IN_GE_MODE -> {
                        eoListValue.forEach { eo ->
                            resultGeList
                                    .find { it.processingUnitNumber == eo.processingUnitNumber }
                                    ?.let { ge ->
                                        eo.cargoUnitNumber = ge.cargoUnitNumber
                                        eo.state = ProcessingUnit.State.COMBINED
                                    }
                        }
                    }
                    ConsolidationNetRequest.SEPARATION_GE_TO_EO_MODE -> {
                        eoListValue.forEach { eo ->
                            sendGEList.find { it.cargoUnitNumber == eo.cargoUnitNumber }
                                    ?.let {
                                        eo.cargoUnitNumber = null
                                        eo.state = ProcessingUnit.State.NOT_PROCESSED
                                    }
                        }
                    }
                }
                val newGeListValue = resultGeList.toModelList()
                cargoUnitRepository.setGE(newGeListValue)
                updateEoItemList()
                updateGeItemList()
                screenNavigator.hideProgress()
                changeTabToGEList()
            })
        }
    }

    fun onExcludeBtnClick() {
        screenNavigator.openExcludeConfirmationDialog {
            exclude()
        }
    }

    private fun exclude() {
        geSelectionHelper.selectedPositions.value?.let { setOfSelectedPositions ->
            val geListValue = cargoUnitRepository.getGEList()
            val geNumbersList = geListValue.map { ge ->
                RestCargoUnit(ge.number, null)
            }
            val selectedGEList = setOfSelectedPositions.map { geListIndex ->
                geNumbersList[geListIndex]
            }
            consolidate(
                    mutableListOf(),
                    selectedGEList,
                    ConsolidationNetRequest.SEPARATION_GE_TO_EO_MODE
            )
        }
    }

    fun onSaveBtnClick() {
        screenNavigator.openSaveTaskConfirmationDialog(
                yesCallbackFunc = ::endConsolidation,
                status = Task.Status.CONSOLIDATED
        )
    }

    private fun endConsolidation() {
        launchUITryCatch {
            screenNavigator.showProgress(endConsolidationNetRequest)
            val either = sessionInfo.personnelNumber?.let { personnelNumber ->
                val params = EndConsolidationParams(
                        deviceIp = context.getDeviceIp(),
                        taskNumber = taskManager.getTask().number,
                        personnelNumber = personnelNumber
                )
                endConsolidationNetRequest(params)
            } ?: Either.Left(
                    PersonnelNumberFailure(
                            context.getString(R.string.alert_null_personnel_number)
                    )
            )
            either.either({ failure ->
                screenNavigator.hideProgress()
                screenNavigator.openAlertScreen(failure)
            }, { result ->
                screenNavigator.hideProgress()
                val task = result.taskList[0].toTask()
                taskManager.setTask(task)
                cargoUnitRepository.clear() //Очищаем репозиторий ЕО/ГЕ так как задание сохранено
                screenNavigator.goBack()
                screenNavigator.goBack()
                screenNavigator.openTaskScreen(task)
            })
        }
    }

    fun onClickEOListItem(position: Int) {
        val eoListValue = cargoUnitRepository.getEOList()
        val eo = eoListValue[position]
        screenNavigator.openEOInsidesScreen(eo)
    }

    fun onClickGEListItem(position: Int) {
        val geListValue = cargoUnitRepository.getGEList()
        val ge = geListValue[position]
        val geEoList = ge.eoList
        if (geEoList.isEmpty()) {
            onClickEOListItem(position)
        } else {
            val selectedGe = cargoUnitRepository.getSelectedGE(position)
            screenNavigator.openGEInsidesScreen(selectedGe)
        }
    }

    fun onBackPressed() {
        screenNavigator.goBack()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCode(eanCode.value.orEmpty(), fromScan = false)
        return true
    }

    fun onScanResult(data: String) {
        searchCode(code = data, fromScan = true, isBarCode = true)
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        launchUITryCatch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                screenNavigator.openTaskGoodsInfoScreen(productInfo)
            }
        }
    }

    fun onPrintBtnClick() {
        launchUITryCatch {
            screenNavigator.showProgress(documentsToPrintNetRequest)
            val params = DocumentsToPrintParams(
                    taskManager.getTask().number
            )
            val either = documentsToPrintNetRequest(params)
            either.either({ failure ->
                screenNavigator.hideProgress()
                screenNavigator.openAlertScreen(failure)
            }, { result ->
                screenNavigator.hideProgress()
                result.docList?.let { docList ->
                    screenNavigator.openTaskEoMergeFormedDocumentsScreen(docList)
                }.orIfNull {
                    screenNavigator.openAlertScreen(Failure.ServerError)
                    Logg.e {
                        "Список документов null"
                    }
                }
            }
            )
        }
    }

    private fun updateEoItemList() {
        val repositoryEoList = cargoUnitRepository.getEOList()
        eoListLiveData.postValue(repositoryEoList)
        val newEoList = repositoryEoList.mapIndexed { index, processingUnit ->
            EoListItem(
                    number = index + 1,
                    title = "$EO-${processingUnit.processingUnitNumber}",
                    subtitle = formatter.getEOSubtitle(processingUnit),
                    quantity = processingUnit.quantity.orEmpty(),
                    isClickable = true,
                    stateResId = choseResIdByEOState(processingUnit.state)
            )
        }
        eoItemList.postValue(newEoList)
    }

    private fun updateGeItemList() {
        val newGeList = cargoUnitRepository.getGEList().mapIndexed { index, cargoUnit ->
            SimpleListItem(
                    number = index + 1,
                    title = formatter.getGETitle(cargoUnit),
                    countWithUom = cargoUnit.eoList.size.toString(),
                    isClickable = true)
        }
        geItemList.postValue(newGeList)
    }

    private fun changeTabToGEList() {
        selectedPage.value = GE_LIST_TAB
    }

    fun onDigitPressed(digit: Int) = Unit // TODO

    companion object {
        private const val EO = "ЕО"
        private const val EO_LIST_TAB = 0
        private const val GE_LIST_TAB = 1
        private const val EMPTY_GE_STATE_RESOURCE_ID = 0
        private const val NO_ITEM_SELECTED = 0
        private const val ONE_ITEM_SELECTED = 1
    }
}