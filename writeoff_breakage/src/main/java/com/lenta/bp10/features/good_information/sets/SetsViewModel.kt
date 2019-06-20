package com.lenta.bp10.features.good_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.goods_list.SearchProductDelegate
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getComponentsForSet
import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
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

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var searchComponentDelegate: SearchProductDelegate

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate


    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }


    var selectedPage = MutableLiveData(0)

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData("1")
    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    val totalCount: MutableLiveData<Double> = countValue.map {
        (it
                ?: 0.0) + processExciseAlcoProductService.taskRepository.getTotalCountForProduct(productInfo.value!!)
    }
    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }
    val suffix: MutableLiveData<String> = MutableLiveData()
    val componentsItem: MutableLiveData<List<ComponentItem>> = MutableLiveData()
    val componentsSelectionsHelper = SelectionItemsHelper()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val enabledApplyButton: MutableLiveData<Boolean> = MutableLiveData(false)

    private lateinit var components: List<ZmpUtz46V001.ItemLocal_ET_SET_LIST>
    private val componentsInfo = mutableListOf<ProductInfo>()

    val enabledDetailsCleanBtn: MutableLiveData<Boolean> = selectedPage
            .combineLatest(componentsSelectionsHelper.selectedPositions)
            .map {
                val selectedTabPos = it?.first ?: 0
                val selectedComponentsPositions = it?.second
                if (selectedTabPos == 0) processExciseAlcoProductService.taskRepository.getTotalCountForProduct(productInfo.value!!) > 0.0 else !selectedComponentsPositions.isNullOrEmpty()
            }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }


    init {
        viewModelScope.launch {
            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                writeOffReasonTitles.value = writeOffTask.taskDescription.moveTypes.map { it.name }
            }

            searchComponentDelegate.init(viewModelScope = this@SetsViewModel::viewModelScope,
                    scanResultHandler = this@SetsViewModel::handleComponentSearchResult)

            searchProductDelegate.init(viewModelScope = this@SetsViewModel::viewModelScope,
                    scanResultHandler = this@SetsViewModel::handleProductSearchSuccess)

            components = zmpUtz46V001.getComponentsForSet(productInfo.value!!.materialNumber)
            components.forEachIndexed { index, _ ->
                searchComponentDelegate.searchCode(components[index].matnr, fromScan = false, isBarCode = false)
            }
            screenNavigator.hideProgress()
            suffix.value = productInfo.value?.uom?.name
            updateComponents()
        }
    }


    fun onResume() {
        updateComponents()
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
            processExciseAlcoProductService.add(getReason(), it, TaskExciseStamp(
                    materialNumber = "",
                    code = "",
                    setMaterialNumber = "",
                    writeOffReason = "",
                    isBadStamp = false)
            )
            count.value = ""
        }
        updateComponents()
    }

    private fun getReason(): WriteOffReason {
        return processExciseAlcoProductService.taskDescription.moveTypes
                .getOrElse(selectedPosition.value ?: -1) { WriteOffReason.empty }
    }

    private fun handleComponentSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        scanInfoResult?.let {
            componentsInfo.add(it.productInfo)
            updateComponents()
        }

        return true
    }

    private fun handleProductSearchSuccess(scanInfoResult: ScanInfoResult?): Boolean {
        eanCode.value = ""
        scanInfoResult?.productInfo?.let { info ->
            componentsInfo.forEachIndexed { index, componentInfo ->
                if (componentInfo.materialNumber == info.materialNumber) {
                    if (getCountExciseStampsForComponent(componentInfo) == components[index].menge * totalCount.value!!) {
                        screenNavigator.openAlertScreen(Failure.MarksComponentAlreadyScanned, pageNumber = "96")
                        return true
                    } else {
                        screenNavigator.openComponentSetScreen(componentInfo, componentsItem.value!![index])
                        return true
                    }
                }
            }
            screenNavigator.openProductNotSetAlertScreen()
        }

        return true

    }

    private fun updateComponents() {
        var countExciseStampAll = 0.0
        var mengeTotalCount = 0.0
        componentsItem.postValue(
                mutableListOf<ComponentItem>().apply {
                    componentsInfo.forEachIndexed { index, compInfo ->
                        val countExciseStampForComponent = getCountExciseStampsForComponent(compInfo)
                        mengeTotalCount = components[index].menge * totalCount.value!!
                        add(ComponentItem(
                                number = index + 1,
                                name = "${compInfo.materialNumber.substring(compInfo.materialNumber.length - 6)} ${compInfo.description}",
                                quantity = "${countExciseStampForComponent
                                        .toStringFormatted()} из ${(mengeTotalCount).toStringFormatted()}",
                                menge = components[index].menge.toString(),
                                even = index % 2 == 0,
                                countSets = totalCount.value!!,
                                selectedPosition = selectedPosition.value!!,
                                writeOffReason = getReason(),
                                setMaterialNumber = productInfo.value!!.materialNumber)
                        )

                        countExciseStampAll += countExciseStampForComponent
                    }
                }
        )
        enabledApplyButton.value = countExciseStampAll == mengeTotalCount && countValue.value != 0.0
        componentsSelectionsHelper.clearPositions()
    }

    private fun getCountExciseStampsForComponent(componentInfo: ProductInfo): Double {

        return processServiceManager
                .getWriteOffTask()!!
                .taskRepository
                .getExciseStamps()
                .findExciseStampsOfProduct(componentInfo)
                .size
                .toDouble()
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            searchEANCode(it)
        }

        return true
    }

    private fun searchEANCode(code: String) {
        searchProductDelegate.searchCode(code, fromScan = true)
    }


    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
        updateComponents()
    }

    fun onBackPressed() {
        processExciseAlcoProductService.discard()
    }

    fun onClickButton3() {
        if (selectedPage.value == 0) onClickDetails() else onClickClean()
    }

    private fun onClickClean() {
        processServiceManager.getWriteOffTask()?.let { writeOffTask ->
            componentsSelectionsHelper.selectedPositions.value?.map { position ->
                componentsInfo[position]
            }?.let {
                writeOffTask.deleteProducts(it)
            }
            updateComponents()
        }
    }

    private fun onClickDetails() {
        productInfo.value?.let {
            screenNavigator.openGoodsReasonsScreen(productInfo = it)
        }
    }

    fun onScanResult(data: String) {
        searchEANCode(data)
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
        val setMaterialNumber: String
) : Evenable {
    override fun isEven() = even

}