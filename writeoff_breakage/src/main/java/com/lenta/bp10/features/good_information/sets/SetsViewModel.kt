package com.lenta.bp10.features.good_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.fmp.resources.dao_ext.getComponentsForSet
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

    //TODO надо реализовать асинхронный запрос
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

    //TODO уточнить, надо ли добавлять набо в списание, если да, то к какому виду товаров принадлежит набор, обычный или акцизный, если акцизный, тогда проблема с маркой, невозможно пустое поле добавить
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
    private val componentsInfo = mutableListOf<ProductInfo>() //: MutableLiveData<List<ProductInfo>> = MutableLiveData()
    val componentsItem: MutableLiveData<List<ComponentItem>> = MutableLiveData()
    val componentsSelectionsHelper = SelectionItemsHelper()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    private val components = mutableListOf<ZmpUtz46V001.ItemLocal_ET_SET_LIST>()

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

            components.addAll(zmpUtz46V001.getComponentsForSet(productInfo.value!!.materialNumber))
            components.forEachIndexed { index, itemLocal_ET_SET_LIST ->
                productInfoDbRequest(ProductInfoRequestParams(components[index].matnr)).either(::handleFailure, ::handleComponentInfoSuccess)
            }
            suffix.value = productInfo.value?.uom?.name
            updateComponents()
            Logg.d { "componentsInfo ${componentsInfo}" }
        }
    }

    private fun handleComponentInfoSuccess(componentInfo: ProductInfo) {
        componentsInfo.add(componentInfo)
    }

    fun onResume() {
        updateComponents()
    }

    private fun updateComponents() {
        if (totalCount.value ?: 0.0 > 0.0) {
            count.value = count.value
            componentsItem.postValue(
                    mutableListOf<ComponentItem>().apply {
                        componentsInfo.forEachIndexed { index, compInfo ->
                            add(ComponentItem(
                                    number = index + 1,
                                    name = "${compInfo.materialNumber.substring(compInfo.materialNumber.length - 6)} ${compInfo.description}",
                                    quantity = "${getCountExciseStampsForComponent(compInfo)} из ${components[index].menge * totalCount.value!!}",
                                    menge = components[index].menge.toString(),
                                    even = index % 2 == 0,
                                    countSets = totalCount.value!!,
                                    selectedPosition = selectedPosition.value!!,
                                    writeOffReason = getReason(),
                                    materialNumber = compInfo.materialNumber))
                        }
                    }
            )
            componentsSelectionsHelper.clearPositions()
        }
    }

    private fun getCountExciseStampsForComponent(componentInfo: ProductInfo) : Double {

        return processServiceManager
                .getWriteOffTask()!!
                .taskRepository
                .getExciseStamps()
                .findExciseStampsOfProduct(componentInfo)
                .size
                .toDouble()
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
        processExciseAlcoProductService.apply()
    }

    fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    private fun addSet() {
        countValue.value?.let {
            //TODO уточнить, надо ли добавлять набо в списание, если да, то к какому виду товаров принадлежит набор, обычный или акцизный, если акцизный, тогда проблема с маркой, невозможно пустое поле добавить
            processExciseAlcoProductService.add(getReason(), it, TaskExciseStamp(
                                                                    materialNumber = "",
                                                                    code = "",
                                                                    setMaterialNumber = "",
                                                                    writeOffReason = "",
                                                                    isBasStamp = false)
            )
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
        updateComponents()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchEANCode()
        return true
    }

    private fun searchEANCode() {
        viewModelScope.launch {
            eanCode.value?.let {
                productInfoDbRequest(ProductInfoRequestParams(number = it)).either(::handleFailure, ::handleSearchEANSuccess)
            }
        }
    }

    private fun handleSearchEANSuccess(componentInfo: ProductInfo) {
        componentsItem.value?.let { list ->  list.forEachIndexed { index, componentItem ->
            if (componentItem.materialNumber == componentInfo.materialNumber) {
                screenNavigator.openComponentSetScreen(componentInfo, componentItem)
                return
            }
        }}
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
        val menge: String,
        val even: Boolean,
        val countSets: Double,
        val selectedPosition: Int,
        val writeOffReason: WriteOffReason,
        val materialNumber: String
) : Evenable {
    override fun isEven() = even

}