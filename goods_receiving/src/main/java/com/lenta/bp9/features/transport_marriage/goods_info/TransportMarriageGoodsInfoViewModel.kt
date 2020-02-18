package com.lenta.bp9.features.transport_marriage.goods_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskTransportMarriageInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class TransportMarriageGoodsInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val transportMarriageInfo: MutableLiveData<TaskTransportMarriageInfo> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProcessingUnit: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnitSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    val acceptTotalCountWithUom by lazy {
        "0 ${transportMarriageInfo.value?.uom?.name}"
    }
    val refusalTotalCountWithUom: MutableLiveData<String> = countValue.map {
        if (it != 0.0) {
            "- " + it.toStringFormatted() + " " + transportMarriageInfo.value?.uom?.name
        } else {
            "0 " + transportMarriageInfo.value?.uom?.name
        }
    }
    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        it!! != 0.0
    }

    fun getTitle() : String {
        return "${transportMarriageInfo.value!!.getMaterialLastSix()} ${transportMarriageInfo.value!!.materialName}"
    }

    init {
        viewModelScope.launch {
            suffix.value = transportMarriageInfo.value?.uom?.name
        }
    }

    fun onClickDetails() {
        //screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() {

    }

    fun onClickApply() {

    }

    override fun onClickPosition(position: Int) {
        //spinReasonRejectionSelectedPosition.value = position
    }

    fun onScanResult(data: String) {

    }

}
