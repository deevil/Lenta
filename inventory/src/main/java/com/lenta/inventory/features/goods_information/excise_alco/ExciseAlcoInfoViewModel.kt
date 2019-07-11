package com.lenta.inventory.features.goods_information.excise_alco

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.ProcessExciseAlcoProductService
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.ExciseGoodsParams
import com.lenta.inventory.requests.network.ExciseGoodsRestInfo
import com.lenta.inventory.requests.network.ObtainingDataExciseGoodsNetRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExciseAlcoInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var obtainingDataExciseGoodsNetRequest: ObtainingDataExciseGoodsNetRequest

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getInventoryTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    val editTextFocus: MutableLiveData<Boolean> = MutableLiveData(false)

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    val storePlaceNumber: MutableLiveData<String> = MutableLiveData()
    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { it != "00" }

    val spinList: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val count: MutableLiveData<String> = MutableLiveData("")

    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull()?: 0.0 }

    val suffix: MutableLiveData<String> = MutableLiveData()

    val totalCount: MutableLiveData<Double> = countValue.map {
        (it ?: 0.0) + (productInfo.value!!.factCount ?: 0.0)
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
        }
    }

    fun onClickRollback() {
        //todo
    }

    fun onClickDetails() {
        //todo
    }

    fun onClickMissing() {
        //todo
        //screenNavigator.openGoodsDetailsStorageScreen()
    }

    fun onClickApply() {
        //todo
        viewModelScope.launch {
            obtainingDataExciseGoodsNetRequest(
                    ExciseGoodsParams(
                            werks = "werks",
                            materialNumber = "matnr",
                            materialNumberComp = "materialNumberComp",
                            markNumber = "markNumber",
                            boxNumber = "boxNumber",
                            organCode = "organCode",
                            bottMark = "bottMark",
                            mode = "mode",
                            codeEBP = "codeEBP",
                            factCount = "0")).
                    either(::handleFailure, ::handleSuccess)
        }
    }

    fun onScanResult(data: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClickPosition(position: Int) {
        return
    }

    private fun handleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo) {
        Logg.d { "handleSuccess $exciseGoodsRestInfo" }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }
}
