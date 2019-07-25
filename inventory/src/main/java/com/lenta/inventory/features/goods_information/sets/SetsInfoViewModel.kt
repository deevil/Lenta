package com.lenta.inventory.features.goods_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_details_storage.ComponentItem
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.ProcessSetsService
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.SetComponentsNetRequest
import com.lenta.inventory.requests.network.SetComponentsRestInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetsInfoViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var setComponentsNetRequest: SetComponentsNetRequest

    @Inject
    lateinit var processSetsService: ProcessSetsService

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    private val componentsInfo : ArrayList<SetComponentInfo> = ArrayList()

    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()

    val storePlaceNumber: MutableLiveData<String> = MutableLiveData()

    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { it != "00" }

    var selectedPage = MutableLiveData(0)

    val componentsSelectionsHelper = SelectionItemsHelper()

    val searchCode: MutableLiveData<String> = MutableLiveData()

    val spinList: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val count: MutableLiveData<String> = MutableLiveData("0")

    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull()?: 0.0 }

    val suffix: MutableLiveData<String> = MutableLiveData()

    private val totalCount: MutableLiveData<Double> by lazy {
        countValue.map {
            (it ?: 0.0) + productInfo.value!!.factCount
        }
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }


    val componentsItem: MutableLiveData<List<ComponentItem>> = countValue.map {
        componentsInfo.mapIndexed { index, componentInfo ->
            val countExciseStampForComponent = processSetsService.getCountExciseStampsForComponent(componentInfo)
            ComponentItem(
                    number = index + 1,
                    name = "${componentInfo.number.substring(componentInfo.number.length - 6)} ${componentInfo.name}",
                    quantity = "$countExciseStampForComponent из ${((componentInfo.count).toDouble() * totalCount.value!!).toStringFormatted()}",
                    menge = componentInfo.count,
                    even = index % 2 == 0,
                    countSets = totalCount.value ?: 0.0,
                    selectedPosition = selectedPosition.value!!,
                    setMaterialNumber = componentInfo.setNumber
            )
        }.reversed()
    }

    val enabledMissingButton: MutableLiveData<Boolean> = totalCount.map { it ?: 0.0 <= 0.0 }

    val enabledApplyButton: MutableLiveData<Boolean> = componentsItem.map {
        var totalCountComponents = 0.0
        var totalCountExciseStampForComponents = 0
        componentsInfo.map {componentInfo ->
            totalCountComponents += componentInfo.count.toDouble() * totalCount.value!!
            totalCountExciseStampForComponents += processSetsService.getCountExciseStampsForComponent(componentInfo)
        }
        countValue.value ?: 0.0 != 0.0 && totalCountComponents == totalCountExciseStampForComponents.toDouble()
    }

    val enabledDetailsCleanBtn: MutableLiveData<Boolean> = selectedPage
            .combineLatest(componentsSelectionsHelper.selectedPositions)
            .combineLatest(totalCount)
            .map {
                val selectedTabPos = selectedPage.value ?: 0
                val selectedComponentsPositions = componentsSelectionsHelper.selectedPositions.value
                if (selectedTabPos == 0) totalCount.value!! > 0.0 else !selectedComponentsPositions.isNullOrEmpty()
            }

    init {
        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            suffix.value = productInfo.value?.uom?.name
            storePlaceNumber.value = productInfo.value!!.placeCode

            setComponentsNetRequest(null).either(::handleFailure, ::componentsInfoHandleSuccess)
            screenNavigator.hideProgress()

        }
    }

    private fun componentsInfoHandleSuccess(componentsRestInfo: List<SetComponentsRestInfo>) {
        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            processSetsService.newProcessSetsService(productInfo.value!!, componentsRestInfo)
            componentsInfo.addAll(processSetsService.getComponentsForSet())
            screenNavigator.hideProgress()
        }
    }

    private fun updateComponents() {
        count.value = count.value
        componentsSelectionsHelper.clearPositions()
    }

    fun onResume() {
        updateComponents()
    }

    fun onClickMissing() {
        //todo onClickMissing
        screenNavigator.openAlertScreen("onClickMissing")
    }

    fun onClickApply() {
        //todo onClickApply
        screenNavigator.openAlertScreen("onClickApply")
    }

    fun onClickButton3() {
        if (selectedPage.value == 0) onClickDetails() else onClickClean()
    }

    private fun onClickClean() {
        componentsSelectionsHelper.selectedPositions.value?.map { position ->
            processSetsService.clearExciseStampsForComponent(componentsInfo[position])
        }
        updateComponents()
    }

    private fun onClickDetails() {
        //todo onClickDetails
        onScanResult("000000000000377980")
    }

    fun onScanResult(data: String) {
        //scannedStampCode.value = data
        when (data.length) {
            68, 150 -> screenNavigator.openSetComponentsScreen(componentInfo = componentsInfo[0], targetTotalCount = totalCount.value!!)
            else -> screenNavigator.openSetComponentsScreen(componentInfo = componentsInfo[0], targetTotalCount = totalCount.value!!)
        }
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
        updateComponents()
    }

    fun onBackPressed() {
        //todo onBackPressed
        return
        //processExciseAlcoProductService.discard()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        //todo onOkInSoftKeyboard
        //searchEANCode()
        return true
    }

    override fun onClickPosition(position: Int) {
        return
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }
}
