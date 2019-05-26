package com.lenta.bp10.features.good_information.sets.component

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class ComponentViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    @Inject
    lateinit var productInfoDbRequest: ProductInfoDbRequest

    val spinnerEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val componentItem: MutableLiveData<ComponentItem> = MutableLiveData()
    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = componentItem.map { it!!.selectedPosition }
    val count: MutableLiveData<String> = MutableLiveData("0")
    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() }
    val totalCount: MutableLiveData<Double> = countValue.map { (it ?: 0.0) + processServiceManager.getWriteOffTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(productInfo.value!!).size}
    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "$it из ${componentItem.value!!.menge.toDouble() * componentItem.value!!.countSets}" }
    val suffix: MutableLiveData<String> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    private val exciseStamp = mutableListOf<TaskExciseStamp>()

    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    val enabledButton: MutableLiveData<Boolean> = countValue.map {
        it!! > 0.0
    }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    fun setComponentItem(componentItem: ComponentItem) {
        this.componentItem.value = componentItem
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
        exciseStamp.forEachIndexed { index, taskExciseStamp ->
            processExciseAlcoProductService.add(componentItem.value!!.writeOffReason, 1.0, taskExciseStamp)
        }

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
        searchCode()
        return true
    }

    private fun searchCode() {
        viewModelScope.launch {
            eanCode.value?.let {
                productInfoDbRequest(ProductInfoRequestParams(number = it)).either(::handleFailure, ::handleSearchSuccess)
            }

        }
    }

    private fun handleSearchSuccess(componentInfo: ProductInfo) {
        if (totalCount.value!! >= componentItem.value!!.menge.toDouble() * componentItem.value!!.countSets) {
            screenNavigator.openAlertScreen("Превышен лимит")
            return
        }

        if (componentItem.value!!.materialNumber == componentInfo.materialNumber) {
            count.value = (count.value!!.toInt() + 1).toString()
            exciseStamp.add(TaskExciseStamp(
                    materialNumber = componentItem.value!!.materialNumber,
                    code = eanCode.value!!,
                    setMaterialNumber = productInfo.value!!.materialNumber,
                    writeOffReason = componentItem.value!!.writeOffReason.name,
                    isBasStamp = true
            ))

            Logg.d { "taskExciseStamp_size ${exciseStamp.size}" }
            Logg.d { "taskExciseStamp_setMaterialNumber ${exciseStamp[exciseStamp.size-1].setMaterialNumber}" }
            Logg.d { "taskExciseStamp_materialNumber ${exciseStamp[exciseStamp.size-1].materialNumber}" }
            Logg.d { "taskExciseStamp_code ${exciseStamp[exciseStamp.size-1].code}" }

            countValue.value = exciseStamp.size.toDouble()
            return
        }
        screenNavigator.openAlertScreen("Акцизная марка не найдена")
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }
    //TODO тестовый код==================================================

}
