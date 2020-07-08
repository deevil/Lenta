package com.lenta.movement.features.task.eo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.lenta.movement.R
import com.lenta.movement.exception.PersonnelNumberFailure
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.*
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.ConsolidationNetRequest
import com.lenta.movement.requests.network.DocumentsToPrintNetRequest
import com.lenta.movement.requests.network.DocumentsToPrintParams
import com.lenta.movement.requests.network.EndConsolidationNetRequest
import com.lenta.movement.requests.network.models.RestCargoUnit
import com.lenta.movement.requests.network.models.consolidation.ConsolidationParams
import com.lenta.movement.requests.network.models.consolidation.ConsolidationProcessingUnit
import com.lenta.movement.requests.network.models.endConsolidation.EndConsolidationParams
import com.lenta.movement.requests.network.models.toCargoUnitList
import com.lenta.movement.requests.network.models.toTask
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.mapSkipNulls
import kotlinx.coroutines.launch
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

    val selectedPagePosition = MutableLiveData(EO_LIST_TAB)
    val currentPage = selectedPagePosition.mapSkipNulls { TaskEOMergePage.values()[it] }

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val eoList by unsafeLazy { MutableLiveData(listOf<ProcessingUnit>()) }

    val eoItemList by unsafeLazy {
        eoList.switchMap { eoList ->
            liveData {
                emit(eoList.mapIndexed { index, eo ->
                    EoListItem(
                            number = index + 1,
                            title = eo.processingUnitNumber,
                            subtitle = formatter.getEOSubtitle(eo),
                            quantity = eo.quantity.orEmpty(),
                            isClickable = true,
                            stateResId = when (eo.state) {
                                ProcessingUnit.State.NOT_PROCESSED -> EMPTY_GE_STATE_RESOURCE_ID
                                ProcessingUnit.State.TOP_LEVEL_EO -> R.drawable.ic_top_level_processing_unit_32dp
                                ProcessingUnit.State.COMBINED -> R.drawable.ic_cargo_unit_32dp
                            }
                    )
                })

            }
        }
    }

    val geList by unsafeLazy { MutableLiveData(mutableListOf<CargoUnit>()) }

    val geItemList by unsafeLazy {
        geList.switchMap { list ->
            liveData {
                emit(list.mapIndexed { index, ge ->
                    SimpleListItem(
                            number = index + 1,
                            title = formatter.getGETitle(ge),
                            countWithUom = ge.eoList.size.toString(),
                            isClickable = true)
                })
            }
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
        eoList.map { eoList ->
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

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
    }

    fun getTitle(): String {
        return "${taskManager.getTask().taskType.shortName} // ${taskManager.getTask().name}"
    }


    fun onProcessBtnClick() {
        eoSelectionHelper.selectedPositions.value?.let { listOfSelected ->
            eoList.value?.let { eoList ->
                val numberOfSelectedItems = listOfSelected.size
                val notProcessedEOList = eoList.mapNotNull {
                    if (it.state == ProcessingUnit.State.NOT_PROCESSED) {
                        ConsolidationProcessingUnit(it.processingUnitNumber)
                    } else null
                }

                when (numberOfSelectedItems) {

                    0 -> {
                        screenNavigator.openZeroSelectedEODialog({
                            //Кнопка обработать
                            addAllEOAsGE()
                        }, {
                            //Кнопка объединить
                            if (notProcessedEOList.isNotEmpty())
                                consolidate(
                                        notProcessedEOList,
                                        listOf(),
                                        ConsolidationNetRequest.CONSOLIDATION_EO_IN_GE_MODE
                                )
                        })
                    }

                    1 -> {
                        geList.value?.let { geListValue ->
                            val eoListIndex = listOfSelected.first()
                            val eo = eoList[eoListIndex]
                            val newCargoUnit =
                                    CargoUnit(eo.processingUnitNumber, listOf())
                            if (eo.state == ProcessingUnit.State.NOT_PROCESSED
                                    && geListValue.contains(newCargoUnit).not()) {
                                geListValue.add(newCargoUnit)
                                eoList[eoListIndex].state = ProcessingUnit.State.TOP_LEVEL_EO
                                geList.value = geListValue
                                changeTabToGEList()
                            }
                        }
                    }

                    else -> {
                        if (notProcessedEOList.isNotEmpty()) {
                            val selectedEO = mutableListOf<ConsolidationProcessingUnit>()
                            listOfSelected.forEach { eoListIndex ->
                                selectedEO.add(notProcessedEOList[eoListIndex])
                            }
                            consolidate(
                                    selectedEO,
                                    listOf(),
                                    ConsolidationNetRequest.CONSOLIDATION_EO_IN_GE_MODE
                            )
                        } else {
                            Logg.e { "Выделенные элементы уже обработаны" }
                        }
                    }
                }
                eoSelectionHelper.clearPositions()
            }
        }
    }

    private fun consolidate(sendEOList: List<ConsolidationProcessingUnit>, sendGEList: List<RestCargoUnit>, mode: String) {
        viewModelScope.launch {
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
            }
                    ?: Either.Left(PersonnelNumberFailure(context.getString(R.string.alert_null_personnel_number)))
            either.either({ failure ->
                screenNavigator.hideProgress()
                screenNavigator.openAlertScreen(failure)
            }, { result ->
                screenNavigator.hideProgress()

                val resultGeList = result.geList

                eoList.value?.let { eoListValue ->
                    eoListValue.forEach { eo ->
                        when (mode) {
                            ConsolidationNetRequest.CONSOLIDATION_EO_IN_GE_MODE -> {
                                resultGeList.forEach { ge ->
                                    if (ge.processingUnitNumber == eo.processingUnitNumber) {
                                        eo.cargoUnitNumber = ge.cargoUnitNumber
                                        eo.state = ProcessingUnit.State.COMBINED
                                    }
                                }
                            }

                            ConsolidationNetRequest.SEPARATION_GE_TO_EO_MODE -> {
                                sendGEList.forEach { geToExclude ->
                                    eoListValue.forEach { eo ->
                                        if (geToExclude.cargoUnitNumber == eo.cargoUnitNumber) {
                                            eo.cargoUnitNumber = null
                                            eo.state = ProcessingUnit.State.NOT_PROCESSED
                                        }
                                    }
                                }
                            }
                        }
                        eoList.value = eoListValue
                    }
                }
                geList.value = resultGeList.toCargoUnitList()
                changeTabToGEList()
            })
        }
    }

    private fun addAllEOAsGE() {
        eoList.value?.forEach { eo ->
            geList.value?.let { geListValue ->
                val newCargoUnit = CargoUnit(eo.processingUnitNumber, listOf())
                if (eo.state == ProcessingUnit.State.NOT_PROCESSED && geListValue.contains(newCargoUnit).not()) {
                    geListValue.add(newCargoUnit)
                    geList.value = geListValue
                    eo.state = ProcessingUnit.State.TOP_LEVEL_EO
                    changeTabToGEList()
                    eoSelectionHelper.clearPositions()
                }
            }
        }
    }

    fun onExcludeBtnClick() {
        geSelectionHelper.selectedPositions.value?.let { setOfSelectedPositions ->
            geList.value?.let { geListValue ->
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
    }

    fun onSaveBtnClick() {
        viewModelScope.launch {
            screenNavigator.showProgress(endConsolidationNetRequest)
            val either = sessionInfo.personnelNumber?.let { personnelNumber ->
                val params = EndConsolidationParams(
                        deviceIp = context.getDeviceIp(),
                        taskNumber = taskManager.getTask().number,
                        personnelNumber = personnelNumber
                )
                endConsolidationNetRequest(params)
            }
                    ?: Either.Left(PersonnelNumberFailure(context.getString(R.string.alert_null_personnel_number)))
            either.either({ failure ->
                screenNavigator.hideProgress()
                screenNavigator.openAlertScreen(failure)
            }, { result ->
                screenNavigator.hideProgress()
                val task = result.taskList[0].toTask()
                taskManager.setTask(task)
                screenNavigator.openTaskScreen(task)
            })
        }

    }

    fun onClickEOListItem(position: Int) {
        // TODO OPEN EO INSIDES SCREEN
    }

    fun onClickGEListItem(position: Int) {
        // TODO Open GE Edit screen 86
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
        viewModelScope.launch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                screenNavigator.openTaskGoodsInfoScreen(productInfo)
            }
        }
    }

    fun onPrintBtnClick() {
        viewModelScope.launch {
            screenNavigator.showProgress(documentsToPrintNetRequest)
            documentsToPrintNetRequest(
                    DocumentsToPrintParams(
                            taskManager.getTask().number
                    )
            ).either({ failure ->
                screenNavigator.hideProgress()
                screenNavigator.openAlertScreen(failure)
            }, { result ->
                screenNavigator.hideProgress()
                // TODO screenNavigator.openFormedDocuments(result.docList)
            }
            )
        }
    }

    private fun changeTabToGEList() {
        selectedPagePosition.value = GE_LIST_TAB
    }

    fun onDigitPressed(digit: Int) = Unit // TODO

    companion object {
        private const val EO_LIST_TAB = 0
        private const val GE_LIST_TAB = 1
        private const val EMPTY_GE_STATE_RESOURCE_ID = 0
    }
}