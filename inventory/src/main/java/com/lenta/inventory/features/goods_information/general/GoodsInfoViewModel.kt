package com.lenta.inventory.features.goods_information.general

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.ProcessGeneralProductService
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsInfoViewModel : MessageViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    @Inject
    lateinit var processGeneralProductService: ProcessGeneralProductService

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val storePlaceNumber: MutableLiveData<String> = MutableLiveData()
    val msgWrongProducType: MutableLiveData<String> = MutableLiveData()
    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { it != "00" }
    val spinList: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val totalCount: MutableLiveData<Double> = countValue.map {
        (it ?: 0.0) + productInfo.value!!.factCount
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }
    val suffix: MutableLiveData<String> = MutableLiveData()
    val enabledMissingButton: MutableLiveData<Boolean> = totalCount.map { it ?: 0.0 == 0.0}

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(totalCount).map {
        it!!.first != 0.0 && it.second > 0.0
    }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
            storePlaceNumber.value = productInfo.value?.placeCode
            if (processGeneralProductService.newProcessGeneralProductService(productInfo.value!!) == null){
                screenNavigator.goBack()
                screenNavigator.openAlertScreen(
                        message = msgWrongProducType.value!!,
                        iconRes = iconRes,
                        textColor = textColor,
                        pageNumber = "98")
            }
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
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                findProduct(materialNumber = data, storePlaceNumber = storePlaceNumber.value!!)?.
                let{
                    when (it.type){
                        ProductType.General, ProductType.NonExciseAlcohol -> {
                            enabledBtn()
                            screenNavigator.goBack()
                            screenNavigator.openGoodsInfoScreen(productInfo = it)
                        }
                        ProductType.ExciseAlcohol -> {
                            if (it.isSet){
                                enabledBtn()
                                screenNavigator.goBack()
                                screenNavigator.openSetsInfoScreen(it)
                            }
                            else{
                                enabledBtn()
                                screenNavigator.goBack()
                                screenNavigator.openExciseAlcoInfoScreen(it)
                            }
                        }
                    }
                    return
                }

        screenNavigator.openAlertScreen(
                                    message = message,
                                    iconRes = iconRes,
                                    textColor = textColor,
                                    pageNumber = "98")
    }

    private fun enabledBtn(){
        if (enabledApplyButton.value!!){
            processGeneralProductService.setFactCount(totalCount.value!!)
        }
        else{
            if (enabledMissingButton.value!!){
                processGeneralProductService.markMissing()
            }
        }
    }

    override fun onClickPosition(position: Int) {
        return
    }
}
