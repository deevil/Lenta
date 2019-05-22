package com.lenta.bp10.features.good_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetsViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var productInfoDbRequest: ProductInfoDbRequest

    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData("")
    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() }
    val totalCount: MutableLiveData<Double> = countValue.map { (it ?: 0.0) + processExciseAlcoProductService.getTotalCount()}
    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "$it ${productInfo.value!!.uom.name}" }
    val suffix: MutableLiveData<String> = MutableLiveData()
    val componentsSets: MutableLiveData<List<ComponentItem>> = MutableLiveData()
    val componentsSelectionsHelper = SelectionItemsHelper()
    val eanCode: MutableLiveData<String> = MutableLiveData()

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(selectedPosition).map {
        val count = it?.first ?: 0.0
        var enabled = false
        productInfo.value?.let { productInfoVal ->
            enabled =
                    count != 0.0
                            &&
                            processExciseAlcoProductService.taskRepository.getTotalCountForProduct(productInfoVal, getReason()) + (countValue.value
                            ?: 0.0) >= 0.0
        }
        enabled
    }

    val enabledDetailsButton: MutableLiveData<Boolean> = totalCount.map {
        processExciseAlcoProductService.getTotalCount() > 0.0
    }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    init {
        viewModelScope.launch {
            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                writeOffReasonTitles.value = writeOffTask.taskDescription.moveTypes.map { it.name }
            }
            suffix.value = productInfo.value?.uom?.name
            updateComponents()
        }
    }

    fun onResume() {
        updateComponents()
    }

    private fun updateComponents() {
        processServiceManager.getWriteOffTask()?.let {
            componentsSets.postValue(
                    it.getProcessedProducts()
                            .mapIndexed { index, productInfo ->
                                ComponentItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        quantity = "0 из ${it.taskRepository.getTotalCountForProduct(productInfo)}",
                                        /**quantity = "${processExciseAlcoProductService.getTotalCount()} ${productInfo.uom.name}",*/
                                        even = index % 2 == 0,
                                        productInfo = productInfo)
                            }
                            .reversed())
        }

        componentsSelectionsHelper.clearPositions()

    }

    fun onClickClean() {
        screenNavigator.openAlertScreen("onClickClean")
    }

    fun onClickDetails() {
        productInfo.value?.let {
            screenNavigator.openGoodsReasonsScreen(productInfo = it)
        }
    }


    fun onClickAdd() {
        addSet()
    }

    fun onClickApply() {
        addSet()
        processExciseAlcoProductService.apply()
        screenNavigator.goBack()
    }

    private fun addSet() {
        countValue.value?.let {
            processExciseAlcoProductService.add(getReason(), it, TaskExciseStamp.empty)
            count.value = ""
        }
        updateComponents()
    }

    private fun getReason(): WriteOffReason {
        return processExciseAlcoProductService.taskDescription.moveTypes
                .getOrElse(selectedPosition.value ?: -1) { WriteOffReason.empty }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        //TODO редактировать

        searchCode()

        Logg.d { "processServiceManager taskDescription: ${processServiceManager.getWriteOffTask()?.taskDescription}" }
        return true
    }

    private fun searchCode() {
        viewModelScope.launch {
            eanCode.value?.let {
                productInfoDbRequest(ProductInfoRequestParams(number = it)).either(::handleFailure, ::handleScanSuccess)
            }

        }
    }

    private fun handleScanSuccess(productInfo: ProductInfo) {
        //TODO редактировать
        Logg.d { "productInfo: ${productInfo.isSet}" }
        if (productInfo.isSet){
            screenNavigator.openSetsInfoScreen(productInfo)
            return
        }
        screenNavigator.openGoodInfoScreen(productInfo)
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    fun onBackPressed() {
        processExciseAlcoProductService.discard()
    }

}

data class ComponentItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean,
        val productInfo: ProductInfo
) : Evenable {
    override fun isEven() = even

}