package com.lenta.inventory.features.goods_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_details_storage.ComponentItem
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.SetComponentsNetRequest
import com.lenta.inventory.requests.network.SetComponentsRestInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetsInfoViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var setComponentsNetRequest: SetComponentsNetRequest

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    private val componentsInfo : ArrayList<SetComponentInfo> = ArrayList()

    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()

    val storePlaceNumber: MutableLiveData<String> = MutableLiveData()

    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { it != "00" }

    var selectedPage = MutableLiveData(0)

    val componentsSelectionsHelper = SelectionItemsHelper()

    val eanCode: MutableLiveData<String> = MutableLiveData()

    val spinList: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val count: MutableLiveData<String> = MutableLiveData("")

    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull()?: 0.0 }

    val suffix: MutableLiveData<String> = MutableLiveData()

    val totalCount: MutableLiveData<Double> = countValue.map {
        (it ?: 0.0)
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    val componentsItem: MutableLiveData<List<ComponentItem>> = countValue.map {
        componentsInfo.mapIndexed { index, componentInfo ->
            //val countExciseStampForComponent = getCountExciseStampsForComponent(compInfo)
            //val rightCount = componentsDataList[index].menge * countValue.value!!
            ComponentItem(
                    number = index + 1,
                    name = "${componentInfo.materialNumber.substring(componentInfo.materialNumber.length - 6)}", // ${componentInfo.description}",
                    quantity = componentInfo.countComponent, //"${countExciseStampForComponent.toStringFormatted()} из ${(componentInfo.countComponent).toStringFormatted()}",
                    menge = "-1", //componentsDataList[index].menge.toString(),
                    even = index % 2 == 0,
                    countSets = totalCount.value?: 0.0,
                    selectedPosition = selectedPosition.value!!,
                    setMaterialNumber = "00000" //setProductInfo.value!!.materialNumber
            )
        }
    }

    val enabledMissingButton: MutableLiveData<Boolean> = totalCount.map { it ?: 0.0 <= 0.0 }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(totalCount).map {
        it!!.first != 0.0 && it.second >= 0.0
    }

    val enabledDetailsCleanBtn: MutableLiveData<Boolean> = MutableLiveData(true)

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
            storePlaceNumber.value = productInfo.value!!.placeCode

            screenNavigator.showProgress(titleProgressScreen.value!!)
            setComponentsNetRequest(null).either(::handleFailure, ::componentsInfoHandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun componentsInfoHandleSuccess(componentsRestInfo: List<SetComponentsRestInfo>) {
        componentsRestInfo[0].data.filter{ data ->
            data[1] == productInfo.value!!.materialNumber
        }.map {
            componentsInfo.add(SetComponentInfo(
                    materialNumber = it[1],
                    componentNumber = it[2],
                    countComponent = it[3],
                    uom = it[4]
            ))
        }

        Logg.d { "componentsInfo ${componentsInfo}" }
    }

    fun onResume() {
        return
        //updateComponents()
    }

    fun onClickMissing() {
        //todo
        screenNavigator.openAlertScreen("onClickMissing")
    }

    fun onClickApply() {
        //todo
        screenNavigator.openAlertScreen("onClickApply")
    }

    fun onClickButton3() {
        if (selectedPage.value == 0) onClickDetails() else onClickClean()
    }

    fun onClickDetails() {
        //todo
        screenNavigator.openAlertScreen("onClickDetails")
    }

    fun onClickClean() {
        //todo
        screenNavigator.openAlertScreen("onClickClean")
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
        //updateComponents()
    }
    fun onBackPressed() {
        return
        //processExciseAlcoProductService.discard()
    }

    override fun onOkInSoftKeyboard(): Boolean {
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
