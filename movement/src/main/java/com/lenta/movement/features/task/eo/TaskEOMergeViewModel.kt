package com.lenta.movement.features.task.eo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.models.startConsolidation.CargoUnit
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.mapSkipNulls
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

    val eoSelectionHelper = SelectionItemsHelper()
    val geSelectionHelper = SelectionItemsHelper()

    val selectedPagePosition = MutableLiveData(0)
    val currentPage = selectedPagePosition.mapSkipNulls { TaskEOMergePage.values()[it] }

    val eoList by unsafeLazy { MutableLiveData(listOf<ProcessingUnit>()) }
    val eoItemList by unsafeLazy {
        eoList.mapSkipNulls {  list ->
            list.mapIndexed { index, eo ->
                SimpleListItem(
                        number = index + 1,
                        title = eo.processingUnitNumber,
                        subtitle = formatter.getEOSubtitle(eo),
                        countWithUom = eo.quantity,
                        isClickable = true
                        )
            }
        }
    }
    val geList by unsafeLazy { MutableLiveData(mutableListOf<CargoUnit>()) }
    val geItemList by unsafeLazy {
        geList.mapSkipNulls { list ->
            list.mapIndexed { index, ge ->
                SimpleListItem(
                        number = index + 1,
                        title = formatter.getGETitle(ge),
                        countWithUom = 1.toString(),
                        isClickable = true
                )
            }
        }
    }

    val processBtnIsVisible = unsafeLazy {
        when(currentPage) {
            MutableLiveData(TaskEOMergePage.EO_LIST) -> true
            else -> false
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
        when (eoSelectionHelper.selectedPositions.value?.size) {
            0 -> screenNavigator.openNotImplementedScreenAlert("Open 86 screen")//TODO Open 86 screen
            1 -> {
                eoSelectionHelper.selectedPositions.value?.first()?.let { selectedIndex ->
                    eoList.value?.get(selectedIndex)?.let { eo ->
                        geList.value?.add(CargoUnit("", eo.processingUnitNumber))
                    }
                }
            }
            else -> screenNavigator.openNotImplementedScreenAlert("Send REST Call ZMP_UTZ_MVM_09_V001 Консолидация ЕО\\ГЕ")//TODO Send REST Call ZMP_UTZ_MVM_09_V001 Консолидация ЕО\ГЕ
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