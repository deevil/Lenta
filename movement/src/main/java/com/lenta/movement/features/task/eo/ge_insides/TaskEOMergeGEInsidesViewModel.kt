package com.lenta.movement.features.task.eo.ge_insides

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
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
    lateinit var formatter: IFormatter

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    val eoSelectionHelper = SelectionItemsHelper()

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val eoList by unsafeLazy { MutableLiveData(listOf<ProcessingUnit>()) }

    val eoItemsList by unsafeLazy {
        eoList.switchMap { list ->
            liveData {
                val eoMappedList = list.mapIndexed { index, processingUnit ->
                    SimpleListItem(
                            number = index + 1,
                            title = processingUnit.processingUnitNumber,
                            subtitle = formatter.getEOSubtitle(processingUnit),
                            countWithUom = processingUnit.quantity.orEmpty(),
                            isClickable = true
                    )
                }
                emit(eoMappedList)
            }
        }
    }

    val isExcludeBtnEnabled by unsafeLazy {
        eoSelectionHelper.selectedPositions.map { setOfSelectedItems ->
            setOfSelectedItems?.size?.let {
                it > 0
            }
        }
    }

    fun getTitle(): String {
        return eoList.value?.let{ list ->
            "${list.first().cargoUnitNumber}"
        } ?: ""

    }

    fun onBackPressed() {
        screenNavigator.goBack()
    }

    fun onExcludeBtnClick() {

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

    companion object {

    }
}