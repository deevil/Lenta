package com.lenta.bp10.features.good_information.sets.component

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class ComponentViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    val spinnerEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val componentItem: MutableLiveData<ComponentItem> = MutableLiveData()
    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = componentItem.map { it!!.selectedPosition }
    val count: MutableLiveData<String> = MutableLiveData("")
    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() }
    val totalCount: MutableLiveData<Double> = countValue.map { (it ?: 0.0) }
    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "$it из ${componentItem.value!!.menge}" }
    val suffix: MutableLiveData<String> = MutableLiveData()

    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(selectedPosition).map {
        val count = it?.first ?: 0.0
        var enabled = false
        productInfo.value?.let { productInfoVal ->
            enabled =
                    count != 0.0
                            &&
                            processExciseAlcoProductService.taskRepository.getTotalCountForProduct(productInfoVal) + (countValue.value
                            ?: 0.0) >= 0.0
        }
        enabled
    }

    val enabledRollbackButton: MutableLiveData<Boolean> = totalCount.map {
        //processExciseAlcoProductService.getTotalCount() > 0.0
        processExciseAlcoProductService.taskRepository.getTotalCountForProduct(productInfo.value!!) > 0.0
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
        screenNavigator.openAlertScreen("onClickRollback")
    }

    fun onClickAdd() {
        screenNavigator.openAlertScreen("onClickAdd")
    }

    fun onClickApply() {
        screenNavigator.openAlertScreen("onClickApply")
    }

    fun onBackPressed() {
        //processExciseAlcoProductService.discard()
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

}
