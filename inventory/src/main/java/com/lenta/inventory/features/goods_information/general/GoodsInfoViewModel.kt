package com.lenta.inventory.features.goods_information.general

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.ProcessGeneralProductService
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    private val processGeneralProductService: ProcessGeneralProductService by lazy {
        processServiceManager.getInventoryTask()!!.processGeneralProduct(productInfo.value!!)!!
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    val storePlaceNumber: MutableLiveData<String> = MutableLiveData()
    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { it != "00" }

    val spinList: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val count: MutableLiveData<String> = MutableLiveData("")

    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    private val totalCount: MutableLiveData<Double> = countValue.map {
        (it ?: 0.0) + (productInfo.value!!.factCount ?: 0.0)
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    val suffix: MutableLiveData<String> = MutableLiveData()

    val enabledMissingButton: MutableLiveData<Boolean> = totalCount.map { it ?: 0.0 <= 0.0 }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(totalCount).map {
        it!!.first != 0.0 && it.second >= 0.0
    }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
            storePlaceNumber.value = productInfo.value?.placeCode
        }
    }

    fun onClickDetails(){
        screenNavigator.openGoodsDetailsStorageScreen(productInfo.value!!)
    }

    fun onClickMissing() {
        processGeneralProductService.markMissing()
        screenNavigator.goBack()
    }

    fun onClickApply() {
        processGeneralProductService.setFactCount(totalCount.value!!)
        screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        //TODO необходимо будет прописать тот же функционал, что и на экране Список товаров, когда он будет реализован
    }

    override fun onClickPosition(position: Int) {
        return
    }
}
