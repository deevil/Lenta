package com.lenta.inventory.features.goods_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_details_storage.ComponentItem
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetsInfoViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    //val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData(TaskProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
            true, "1", MatrixType.Active, "materialType","3", null, false))

    var selectedPage = MutableLiveData(0)

    val componentsItem: MutableLiveData<List<ComponentItem>> = MutableLiveData()

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

    val enabledApplyButton: MutableLiveData<Boolean> = MutableLiveData(true)
    val enabledDetailsCleanBtn: MutableLiveData<Boolean> = MutableLiveData(true)

    fun setProductInfo(productInfo: TaskProductInfo) {
        this.productInfo.value = productInfo
    }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
        }
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
}
