package com.lenta.bp10.features.good_information.sets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.goods_list.SearchProductDelegate
import com.lenta.bp10.models.StampsCollectorManager
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
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

    @Inject
    lateinit var setsAlcoStampSearchDelegate: SetsAlcoStampSearchDelegate

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var searchComponentDelegate: SearchProductDelegate

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var stampsCollectorManager: StampsCollectorManager

    val zmpUtz46V001: ZmpUtz46V001 by lazy {
        ZmpUtz46V001(hyperHive)
    }


    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(setProductInfo.value!!)!!
    }


    var selectedPage = MutableLiveData(0)

    val setProductInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData("1")
    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    val totalCount: MutableLiveData<Double> = countValue.map {
        (it
                ?: 0.0) + processExciseAlcoProductService.taskRepository.getTotalCountForProduct(setProductInfo.value!!)
    }
    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${setProductInfo.value!!.uom.name}" }
    val suffix: MutableLiveData<String> = MutableLiveData()
    val componentsLiveData: LiveData<List<ComponentItem>> = countValue.map {
        mutableListOf<ComponentItem>().apply {
            components.forEachIndexed { index, compInfo ->
                val countExciseStampForComponent = getCountExciseStampsForComponent(compInfo)
                val rightCount = componentsDataList[index].menge * countValue.value!!
                add(ComponentItem(
                        number = index + 1,
                        name = "${compInfo.materialNumber.substring(compInfo.materialNumber.length - 6)} ${compInfo.description}",
                        quantity = "${countExciseStampForComponent.toStringFormatted()} из ${(rightCount).toStringFormatted()}",
                        menge = componentsDataList[index].menge.toString(),
                        even = index % 2 == 0,
                        countSets = totalCount.value!!,
                        selectedPosition = selectedPosition.value!!,
                        writeOffReason = getReason(),
                        setMaterialNumber = setProductInfo.value!!.materialNumber,
                        rightCount = rightCount,
                        processedStampsCount = countExciseStampForComponent
                )
                )
            }
        }.toList()
    }
    val componentsSelectionsHelper = SelectionItemsHelper()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val enabledApplyButton: LiveData<Boolean> = selectedPosition.combineLatest(componentsLiveData).map {

        var totalProcessedStampsCount = 0.0
        var totalRightStampsCount = 0.0

        componentsLiveData.value?.let {
            it.forEach { componentItem ->
                totalProcessedStampsCount += componentItem.processedStampsCount
                totalRightStampsCount += componentItem.rightCount
            }
        }

        totalProcessedStampsCount == totalRightStampsCount && countValue.value != 0.0
    }

    private lateinit var componentsDataList: List<ZmpUtz46V001.ItemLocal_ET_SET_LIST>

    private val components = mutableListOf<ProductInfo>()

    val enabledDetailsCleanBtn: LiveData<Boolean> = selectedPage
            .combineLatest(componentsSelectionsHelper.selectedPositions)
            .combineLatest(countValue)
            .map {
                val selectedTabPos = selectedPage.value ?: 0
                val selectedComponentsPositions = componentsSelectionsHelper.selectedPositions.value
                if (selectedTabPos == 0) processExciseAlcoProductService.taskRepository.getTotalCountForProduct(setProductInfo.value!!) > 0.0 else !selectedComponentsPositions.isNullOrEmpty()
            }

    fun setProductInfo(productInfo: ProductInfo) {
        this.setProductInfo.value = productInfo
    }


    init {
        viewModelScope.launch {

            stampsCollectorManager.newStampCollector(processExciseAlcoProductService)

            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                writeOffReasonTitles.value = writeOffTask.taskDescription.moveTypes.map { it.name }
            }

            searchComponentDelegate.init(viewModelScope = this@SetsViewModel::viewModelScope,
                    scanResultHandler = this@SetsViewModel::handleComponentSearchResult)

            searchProductDelegate.init(viewModelScope = this@SetsViewModel::viewModelScope,
                    scanResultHandler = this@SetsViewModel::handleProductSearchSuccess)

            componentsDataList = zmpUtz46V001.getComponentsForSet(setProductInfo.value!!.materialNumber)
            componentsDataList.forEachIndexed { index, _ ->
                searchComponentDelegate.searchCode(componentsDataList[index].matnr, fromScan = false, isBarCode = false)
            }
            screenNavigator.hideProgress()
            suffix.value = setProductInfo.value?.uom?.name

            setsAlcoStampSearchDelegate.init(
                    viewModelScope = this@SetsViewModel::viewModelScope,
                    handleNewStamp = this@SetsViewModel::handleStampSearchResult,
                    materialNumber = setProductInfo.value!!.materialNumber,
                    tkNumber = processServiceManager.getWriteOffTask()!!.taskDescription.tkNumber,
                    components = components
            )

        }
    }


    private fun handleStampSearchResult(isBadStamp: Boolean, productInfo: ProductInfo) {
        stampsCollectorManager.clearComponentsStampCollector()
        stampsCollectorManager.getComponentsStampCollector()!!.add(
                materialNumber = productInfo.materialNumber,
                setMaterialNumber = setProductInfo.value!!.materialNumber,
                writeOffReason = "",
                isBadStamp = isBadStamp
        )
        openComponentScreen(productInfo.materialNumber)
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
            stampsCollectorManager.getSetsStampCollector()!!.processAllForSet(getReason(), countValue.value!!)
            count.value = ""
        }
    }

    private fun getReason(): WriteOffReason {
        return processExciseAlcoProductService.taskDescription.moveTypes
                .getOrElse(selectedPosition.value ?: -1) { WriteOffReason.empty }
    }

    private fun handleComponentSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        scanInfoResult?.let {
            components.add(it.productInfo)
            updateComponents()
        }

        return true
    }

    private fun handleProductSearchSuccess(scanInfoResult: ScanInfoResult?): Boolean {
        eanCode.value = ""
        scanInfoResult?.productInfo?.let { info ->
            stampsCollectorManager.clearComponentsStampCollector()
            openComponentScreen(info.materialNumber)
        }

        return true

    }

    private fun openComponentScreen(materialNumber: String) {
        components.forEachIndexed { index, componentInfo ->
            if (componentInfo.materialNumber == materialNumber) {
                if (getCountExciseStampsForComponent(componentInfo) == componentsDataList[index].menge * totalCount.value!!) {
                    screenNavigator.openStampsCountAlreadyScannedScreen()
                    return
                } else {
                    screenNavigator.openComponentSetScreen(
                            componentInfo,
                            componentsLiveData.value!![index],
                            componentsLiveData.value!![index].rightCount)
                    return
                }
            }
        }
        screenNavigator.openProductNotSetAlertScreen()
    }


    private fun updateComponents() {
        count.value = count.value
        componentsSelectionsHelper.clearPositions()
    }

    private fun getCountExciseStampsForComponent(componentInfo: ProductInfo): Double {
        return stampsCollectorManager.getSetsStampCollector()!!.getCount(componentInfo.materialNumber)
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
        if (code.length > 60) {
            if (stampsCollectorManager.getComponentsStampCollector()!!.prepare(stampCode = code)) {
                setsAlcoStampSearchDelegate.searchExciseStamp(code)
            } else {
                screenNavigator.openAlertDoubleScanStamp()
            }
        } else {
            searchProductDelegate.searchCode(code, fromScan = true)
        }
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
                components[position]
            }?.let {
                stampsCollectorManager.clearAllStampsCollectors()
            }
            updateComponents()
        }
    }

    private fun onClickDetails() {
        setProductInfo.value?.let {
            screenNavigator.openGoodsReasonsScreen(productInfo = it)
        }
    }

    fun onScanResult(data: String) {
        searchEANCode(data)
    }

    fun onResult(fragmentResultCode: Int?) {
        setsAlcoStampSearchDelegate.handleResult(fragmentResultCode)
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
        val setMaterialNumber: String,
        val rightCount: Double,
        val processedStampsCount: Double

) : Evenable {
    override fun isEven() = even

}