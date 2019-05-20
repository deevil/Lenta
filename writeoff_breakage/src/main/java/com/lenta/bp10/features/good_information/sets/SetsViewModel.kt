package com.lenta.bp10.features.good_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.ProcessGeneralProductService
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
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

    private val processGeneralProductService: ProcessGeneralProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processGeneralProduct(productInfo.value!!)!!
    }

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData()
    val suffix: MutableLiveData<String> = MutableLiveData()
    val totalCount: MutableLiveData<String> = MutableLiveData() //count.map { "${(getCount() + processGeneralProductService.getTotalCount())} ${productInfo.value!!.uom.name}" }

    val componentsSets: MutableLiveData<List<ComponentItem>> = MutableLiveData()
    val categories: MutableLiveData<List<String>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    init {
        viewModelScope.launch {
            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                writeOffReasonTitles.value = writeOffTask.taskDescription.moveTypes.map { it.name }
            }
            suffix.value = productInfo.value?.uom?.name
        }
    }

    fun onClickClean() {
        screenNavigator.openAlertScreen("onClickClean")
    }

    fun onClickDetails() {
        screenNavigator.openAlertScreen("onClickDetails")
    }

    fun onClickAdd() {
        screenNavigator.openAlertScreen("onClickAdd")
    }

    fun onClickApply() {
        screenNavigator.openAlertScreen("onClickApply")
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    private fun getCount(): Double {
        return count.value?.toDoubleOrNull() ?: 0.0
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

}

data class ComponentItem(val number: Int, val name: String, val quantity: String, val even: Boolean) : Evenable {
    override fun isEven() = even

}