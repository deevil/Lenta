package com.lenta.bp9.features.goods_information.general

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.ReceivingProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener

class GoodsInfoViewModel : CoreViewModel(), OnPositionClickListener {

    val productInfo: MutableLiveData<ReceivingProductInfo> = MutableLiveData()
    val spinQualityList: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val totalCount: MutableLiveData<Double> = countValue.map {
        (it ?: 0.0) //+ productInfo.value!!.factCount
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(totalCount).map {
        it!!.first != 0.0 && it.second > 0.0
    }

    init {
        suffix.value = productInfo.value?.uom?.name
    }

    fun onClickDetails(){
        //screenNavigator.openGoodsDetailsStorageScreen(productInfo.value!!)
    }

    fun onClickAdd() {
        /**if (addGood()) {
            viewModelScope.launch {
                limitsChecker?.check()
            }
        }*/
    }

    fun onClickApply() {
        //processGeneralProductService.setFactCount(totalCount.value!!)
        //screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        //searchProductDelegate.searchCode(code = data, fromScan = true)
    }

    override fun onClickPosition(position: Int) {
        return
    }
}
