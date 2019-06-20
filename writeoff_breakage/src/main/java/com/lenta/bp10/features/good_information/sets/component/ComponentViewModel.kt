package com.lenta.bp10.features.good_information.sets.component

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.ExciseStampNetRequest
import com.lenta.bp10.requests.network.ExciseStampParams
import com.lenta.bp10.requests.network.ExciseStampRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class ComponentViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    @Inject
    lateinit var exciseStampNetRequest: ExciseStampNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    val limitExceeded: MutableLiveData<String> = MutableLiveData()
    val spinnerEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val componentItem: MutableLiveData<ComponentItem> = MutableLiveData()
    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = componentItem.map { it!!.selectedPosition }
    val count: MutableLiveData<String> = MutableLiveData("0")
    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() }
    val totalCount: MutableLiveData<Double> = countValue.map {
        (it
                ?: 0.0) + processServiceManager.getWriteOffTask()!!.taskRepository.getExciseStamps()
                .findExciseStampsOfProduct(productInfo.value!!).size
    }
    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} из ${(componentItem.value!!.menge.toDouble() * componentItem.value!!.countSets).toStringFormatted()}" }
    val suffix: MutableLiveData<String> = MutableLiveData()
    val exciseStampCode: MutableLiveData<String> = MutableLiveData()
    private val exciseStamp = mutableListOf<TaskExciseStamp>()

    val enabledButton: MutableLiveData<Boolean> = countValue.map {
        it!! > 0.0
    }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    fun setComponentItem(componentItem: ComponentItem) {
        this.componentItem.value = componentItem
    }

    fun setLimitExceeded(limitExceeded: String) {
        this.limitExceeded.value = limitExceeded
    }

    init {
        viewModelScope.launch {
            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                writeOffReasonTitles.value = writeOffTask.taskDescription.moveTypes.map { it.name }
            }
            suffix.value = productInfo.value?.uom?.name
        }
    }

    fun onClickRollback() {
        if (exciseStamp.size > 0) {
            exciseStamp.removeAt(exciseStamp.lastIndex)
            count.value = exciseStamp.size.toString()
        }
    }

    fun onClickAdd() {
        processServiceManager.getWriteOffTask()!!.taskRepository.getExciseStamps().addExciseStamps(exciseStamp)

        exciseStamp.clear()
        count.value = "0"

        Logg.d { "exiseStampsForProduct ${processServiceManager.getWriteOffTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(productInfo.value!!).map { it.code }}" }
        Logg.d { "exiseStampsAll ${processServiceManager.getWriteOffTask()!!.taskRepository.getExciseStamps().getExciseStamps().size}" }

    }

    fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    //TODO тестовый код, для проверки сканирования, потом переписать
    override fun onOkInSoftKeyboard(): Boolean {
        searchExciseStamp()
        return true
    }

    private fun searchExciseStamp() {
        viewModelScope.launch {
            exciseStampCode.value?.let {
                exciseStampNetRequest(ExciseStampParams(pdf417 = it, werks = sessionInfo.market!!, matnr = productInfo.value!!.materialNumber)).either(::handleFailure, ::handleExciseStampSuccess)
            }
        }
    }

    private fun handleExciseStampSuccess(exciseStampRestInfo: List<ExciseStampRestInfo>) {
        //Logg.d { "handleSuccess ${exciseStampRestInfo}" }
        if (totalCount.value!! >= componentItem.value!!.menge.toDouble() * componentItem.value!!.countSets) {
            screenNavigator.openAlertScreen(limitExceeded.value!!)
            return
        }

        val retcodeCode = exciseStampRestInfo[1].data[0][0].toInt()
        val retcodeName = exciseStampRestInfo[1].data[0][1]

        when (retcodeCode) {
            0 -> addExciseStamp()
            1 -> screenNavigator.openAlertScreen(message = retcodeName)
            2 -> screenNavigator.openAlertScreen(message = retcodeName)
            3 -> screenNavigator.openAlertScreen(message = retcodeName)
            4 -> screenNavigator.openAlertScreen(message = retcodeName)
        }
    }

    fun addExciseStamp() {
        count.value = (count.value!!.toInt() + 1).toString()
        exciseStamp.add(TaskExciseStamp(
                materialNumber = productInfo.value!!.materialNumber,
                code = exciseStampCode.value!!,
                setMaterialNumber = componentItem.value!!.setMaterialNumber,
                writeOffReason = componentItem.value!!.writeOffReason.name,
                isBadStamp = true
        ))
        countValue.value = exciseStamp.size.toDouble()
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    fun onScanResult(data: String) {
        exciseStampCode.value = data
        searchExciseStamp()
    }

}
