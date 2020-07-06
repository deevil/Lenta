package com.lenta.movement.features.task.eo

import android.content.Context
import androidx.lifecycle.*
import com.lenta.movement.R
import com.lenta.movement.exception.PersonnelNumberFailure
import com.lenta.movement.models.CargoUnit
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.Consolidation
import com.lenta.movement.requests.network.models.consolidation.ConsolidationParams
import com.lenta.movement.requests.network.models.toCargoUnitList
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.mapSkipNulls
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskEOMergeViewModel : CoreViewModel(), PageSelectionListener {

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
    lateinit var consolidation: Consolidation

    val eoSelectionHelper = SelectionItemsHelper()
    val geSelectionHelper = SelectionItemsHelper()

    val selectedPagePosition = MutableLiveData(0)
    val currentPage = selectedPagePosition.mapSkipNulls { TaskEOMergePage.values()[it] }

    val eoList by unsafeLazy { MutableLiveData(listOf<ProcessingUnit>()) }
    val eoItemList by unsafeLazy {
        eoList.mapSkipNulls { list ->
            list.mapIndexed { index, eo ->
                SimpleListItem(
                        number = index + 1,
                        title = eo.processingUnitNumber,
                        subtitle = formatter.getEOSubtitle(eo),
                        countWithUom = eo.quantity.orEmpty(),
                        isClickable = true
                )
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
                            countWithUom = 1.toString(),
                            isClickable = true)
                })
            }
        }
    }

    private val geNumbersList by unsafeLazy {
        geList.mapSkipNulls { list ->
            list.map {
                it.number
            }
        }
    }

    val isProcessBtnVisible by unsafeLazy {
        currentPage.map { page ->
            when (page) {
                TaskEOMergePage.EO_LIST -> true
                else -> false
            }
        }
    }

    val isExcludeBtnVisible by unsafeLazy {
        currentPage.map { page ->
            when (page) {
                TaskEOMergePage.EO_LIST -> false
                else -> true
            }
        }
    }


    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
    }

    fun onResume() {
        // TODO
    }

    fun getTitle(): String {
        return "${taskManager.getTask().taskType.shortName} // ${taskManager.getTask().name}"
    }

    fun onPrintBtnClick() {
        // TODO
    }

    fun onProcessBtnClick() {
        eoSelectionHelper.selectedPositions.value?.let { listOfSelected ->
            eoList.value?.let { eoList ->
                val numberOfSelectedItems = listOfSelected.size
                val eoNumbersList = eoList.map { it.processingUnitNumber }

                when (numberOfSelectedItems) {

                    0 -> {
                        screenNavigator.openZeroSelectedEODialog({
                            //Кнопка обработать
                            addAllEOAsGE()
                        }, {
                            //Кнопка объединить
                            consolidate(eoNumbersList, listOf(), Consolidation.CONSOLIDATION_EO_IN_GE_MODE)
                        })
                    }

                    1 -> {
                        geList.value?.let {
                            val list = it
                            val eoListIndex = listOfSelected.first()
                            val newCargoUnit = CargoUnit(eoNumbersList[eoListIndex], listOf())
                            list.add(newCargoUnit)
                            geList.value = list
                        }
                    }

                    else -> {
                        val selectedEO = mutableListOf<String>()
                        listOfSelected.forEach { eoListIndex ->
                            selectedEO.add(eoNumbersList[eoListIndex])
                        }
                        consolidate(selectedEO, listOf(), Consolidation.CONSOLIDATION_EO_IN_GE_MODE)
                    }
                }
            }
        }
    }

    private fun consolidate(sendEOList: List<String>, sendGEList: List<String>, mode: String) {

        viewModelScope.launch {
            screenNavigator.showProgress(consolidation)
            val either = sessionInfo.personnelNumber?.let { personnelNumber ->
                val params = ConsolidationParams(
                        deviceIp = context.getDeviceIp(),
                        taskNumber = taskManager.getTask().number,
                        personnelNumber = personnelNumber,
                        mode = mode,
                        eoNumberList = sendEOList,
                        geNumberList = sendGEList
                )
                consolidation(params)
            }
                    ?: Either.Left(PersonnelNumberFailure(context.getString(R.string.alert_null_personnel_number)))
            either.either({ failure ->
                screenNavigator.hideProgress()
                screenNavigator.openAlertScreen(failure)
            }, { result ->
                screenNavigator.hideProgress()
                geList.value = result.geList.toCargoUnitList()
                Unit
            })
        }
    }

    private fun addAllEOAsGE() {
        eoList.value?.forEach { eo ->
            if (eo.cargoUnitNumber == null) {
                geList.value?.let {
                    val list = it
                    val newCargoUnit = CargoUnit(eo.processingUnitNumber, listOf())
                    list.add(newCargoUnit)
                    geList.value = list
                }
            }
        }
    }

    fun onExcludeBtnClick() {
        // TODO
    }

    fun onSaveBtnClick() {
        // TODO create and call EndConsolidation.kt
    }

    fun onClickEOListItem(position: Int) {
        // TODO
    }

    fun onClickGEListItem(position: Int) {
        // TODO Open GE Edit screen 86
    }

    fun onBackPressed() {
        screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        // TODO
    }

    fun onDigitPressed(digit: Int) {
        // TODO
    }
}