package com.lenta.bp10.features.good_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.fmp.resources.dao_ext.getComponentsForSet
import com.lenta.bp10.fmp.resources.dao_ext.getComponentsInfoForSet
import com.lenta.bp10.fmp.resources.slow.ZmpUtz30V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz46V001
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
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetsViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var hyperHive: HyperHive
    val zmpUtz46V001: ZmpUtz46V001 by lazy {
        ZmpUtz46V001(hyperHive)
    }
    val zmpUtz30V001: ZmpUtz30V001 by lazy {
        ZmpUtz30V001(hyperHive)
    }

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var productInfoDbRequest: ProductInfoDbRequest

    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    private val msgBrandNotSet: MutableLiveData<String> = MutableLiveData()

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData("")
    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() }
    val totalCount: MutableLiveData<Double> = countValue.map { (it ?: 0.0) + processExciseAlcoProductService.taskRepository.getTotalCountForProduct(productInfo.value!!)} //processExciseAlcoProductService.getTotalCount()
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
        //processExciseAlcoProductService.getTotalCount() > 0.0
        processExciseAlcoProductService.taskRepository.getTotalCountForProduct(productInfo.value!!) > 0.0
    }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    fun setMsgBrandNotSet(string: String) {
        this.msgBrandNotSet.value = string
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
        val components =  zmpUtz46V001.getComponentsForSet(productInfo.value!!.materialNumber)
        //Logg.d { "testrest ${test46.map { it!!.matnr }}"}

        val componentsInfo = zmpUtz30V001.getComponentsInfoForSet(components.map { it.matnr })

        processServiceManager.getWriteOffTask()?.let { writeOffTask ->
            componentsSets.postValue(
                    mutableListOf<ComponentItem>().apply {
                        componentsInfo.forEachIndexed { indComp, itemComp ->
                            add(ComponentItem(
                                    number = indComp + 1,
                                    name = "${componentsInfo.get(indComp).material.substring(componentsInfo.get(indComp).material.length - 6)} ${componentsInfo.get(indComp).name}",
                                    quantity = "${getCountExciseStampsForComponent(componentsInfo.get(indComp).material)} из ${components.get(indComp).menge}",
                                    even = indComp % 2 == 0,
                                    materialNumber = componentsInfo.get(indComp).material))
                        }
                    }
            )
        }
        componentsSelectionsHelper.clearPositions()
    }

    private fun getCountExciseStampsForComponent(materialComponent: String) : Double {
        var countExciseStamp: Double = 0.0
        processServiceManager.getWriteOffTask().let { writeOffTask ->
            writeOffTask!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(productInfo.value!!).forEachIndexed { indES, taskExciseStamp ->
                countExciseStamp = if (taskExciseStamp.materialNumber == materialComponent) +1.0 else 0.0
            }
        }
        return countExciseStamp
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
        searchEANCode()
        Logg.d { "processServiceManager taskDescription: ${processServiceManager.getWriteOffTask()?.taskDescription}" }
        return true
    }

    private fun searchEANCode() {
        viewModelScope.launch {
            eanCode.value?.let {
                productInfoDbRequest(ProductInfoRequestParams(number = it)).either(::handleFailure, ::handleSearchSuccess)
            }

        }
    }

    private fun handleSearchSuccess(componentInfo: ProductInfo) {
        //TODO редактировать
        componentsSets.value!!.forEachIndexed { index, componentItem ->
            if (componentItem.materialNumber == componentInfo.materialNumber) {
                screenNavigator.openComponentSetScreen()
                return
            }
        }
        screenNavigator.openAlertScreen(msgBrandNotSet.value!!)
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
        val materialNumber: String
) : Evenable {
    override fun isEven() = even

}