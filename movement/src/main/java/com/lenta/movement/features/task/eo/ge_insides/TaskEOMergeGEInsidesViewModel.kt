package com.lenta.movement.features.task.eo.ge_insides

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.lenta.movement.R
import com.lenta.movement.exception.PersonnelNumberFailure
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.CargoUnit
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.models.repositories.ICargoUnitRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.ConsolidationNetRequest
import com.lenta.movement.requests.network.models.RestCargoUnit
import com.lenta.movement.requests.network.models.consolidation.ConsolidationParams
import com.lenta.movement.requests.network.models.consolidation.ConsolidationProcessingUnit
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskEOMergeGEInsidesViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

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
    lateinit var scanInfoHelper: ScanInfoHelper

    @Inject
    lateinit var consolidationNetRequest: ConsolidationNetRequest

    val selectionsHelper = SelectionItemsHelper()

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val ge by unsafeLazy { MutableLiveData<CargoUnit>() }

    val eoItemsList by unsafeLazy {
        ge.switchMap { ge ->
            liveData {
                val eoMappedList = ge.eoList.mapIndexed { index, processingUnit ->
                    SimpleListItem(
                            number = index + 1,
                            title = "$EO-${processingUnit.processingUnitNumber}",
                            subtitle = formatter.getEOSubtitleForInsides(processingUnit),
                            countWithUom = processingUnit.quantity.orEmpty(),
                            isClickable = true
                    )
                }
                emit(eoMappedList)
            }
        }
    }

    val isExcludeBtnEnabled by unsafeLazy {
        selectionsHelper.selectedPositions.map { setOfSelectedItems ->
            setOfSelectedItems?.size?.let {
                it > 0
            }
        }
    }

    fun getTitle(): String {
        return ge.value?.let {
            "$GE-${it.number}"
        }.orIfNull {
            Logg.e {
                "eoList is null"
            }
            SERVER_ERROR
        }
    }

    fun onBackPressed() {
        ge.value?.let { geValue ->
            cargoUnitRepository.updateGE(geValue)
        }
        screenNavigator.goBack()
    }

    fun onExcludeBtnClick() {
        selectionsHelper.selectedPositions.value?.let { setOfSelectedIndexes ->
            val geValue = ge.value
            geValue?.let { ge ->
                setOfSelectedIndexes.forEach { index ->
                    ge.eoList.getOrNull(index)?.let { eoToRemove ->
                        ge.eoList.remove(eoToRemove)
                        val consolidationEo = ConsolidationProcessingUnit(eoToRemove.processingUnitNumber)
                        val consolidationGe = RestCargoUnit(ge.number, "")
                        consolidate(listOf(consolidationEo), listOf(consolidationGe))
                    }
                }
            }
            ge.value = geValue
        }
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

    fun onDigitPressed(digit: Int) = Unit

    fun onResume() {
        val geValue = ge.value
        geValue?.let {
            val newGe = cargoUnitRepository.getGEbyItsNumber(it.number)
            ge.value = newGe
        }
    }

    private fun consolidate(sendEOList: List<ConsolidationProcessingUnit>, sendGEList: List<RestCargoUnit>) {
        viewModelScope.launch {
            screenNavigator.showProgress(consolidationNetRequest)
            val either = sessionInfo.personnelNumber?.let { personnelNumber ->
                val params = ConsolidationParams(
                        deviceIp = context.getDeviceIp(),
                        taskNumber = taskManager.getTask().number,
                        personnelNumber = personnelNumber,
                        mode = ConsolidationNetRequest.EXCLUDE_EO_FROM_GE_MODE,
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
            }, {
                screenNavigator.hideProgress()
            })
        }
    }

    fun onEoItemClickListener(position : Int) {
        val eoListValue = cargoUnitRepository.getEOList()
        val eo = eoListValue[position]
        screenNavigator.openEOInsidesScreen(eo)
    }

    companion object {
        private const val GE = "ГЕ"
        private const val EO = "ЕО"
        private const val SERVER_ERROR = "Ошибка сервера"
    }
}