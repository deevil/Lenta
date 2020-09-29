package com.lenta.bp10.features.good_information.sets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.IGoodInformationRepo
import com.lenta.bp10.features.goods_list.SearchProductDelegate
import com.lenta.bp10.models.StampsCollectorManager
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.requestCodeNotSaveComponents
import com.lenta.bp10.platform.resources.IStringResourceManager
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getComponentsForSet
import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    lateinit var searchSetDelegate: SearchProductDelegate

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var stampsCollectorManager: StampsCollectorManager

    @Inject
    lateinit var resourceManager: IStringResourceManager

    @Inject
    lateinit var goodInformationRepo: IGoodInformationRepo

    val zmpUtz46V001: ZmpUtz46V001 by lazy {
        ZmpUtz46V001(hyperHive)
    }


    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(setProductInfo.value!!)!!
    }

    val setProductInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val writeOffReasons: MutableLiveData<List<WriteOffReason>> = MutableLiveData()
    val writeOffReasonTitles: LiveData<List<String>> = writeOffReasons.map { it?.map { reason -> reason.name } }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData()
    val countValue: MutableLiveData<Double> = count.map {
        it?.toDoubleOrNull() ?: 0.0
    }
    val totalCount: MutableLiveData<Double> = countValue.map {
        (it
                ?: 0.0) + processExciseAlcoProductService.taskRepository.getTotalCountForProduct(setProductInfo.value!!)
    }
    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${setProductInfo.value!!.uom.name}" }
    val suffix: MutableLiveData<String> = MutableLiveData()
    val componentsLiveData: LiveData<List<ComponentItem>> = countValue.map {
        components.mapIndexed { index, compInfo ->
            val countExciseStampForComponent = getCountExciseStampsForComponent(compInfo)
            val rightCount = componentsDataList[index].menge * countValue.value!!
            ComponentItem(
                    number = index + 1,
                    name = "${compInfo.materialNumber.substring(compInfo.materialNumber.length - 6)} ${compInfo.description}",
                    quantity = "${countExciseStampForComponent.toStringFormatted()} из ${(rightCount).toStringFormatted()}",
                    menge = componentsDataList[index].menge.toString(),
                    even = index % 2 == 0,
                    countSets = totalCount.value?: 0.0,
                    selectedPosition = selectedPosition.value!!,
                    writeOffReason = getReason(),
                    setMaterialNumber = setProductInfo.value!!.materialNumber,
                    rightCount = rightCount,
                    processedStampsCount = countExciseStampForComponent
            )
        }
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

        totalProcessedStampsCount == totalRightStampsCount && countValue.value != 0.0 && getReason() !== WriteOffReason.empty
    }

    val title: LiveData<String> by lazy {
        MutableLiveData("${setProductInfo.value!!.getMaterialLastSix()} ${setProductInfo.value!!.description}")
    }

    private lateinit var componentsDataList: List<ZmpUtz46V001.ItemLocal_ET_SET_LIST>

    private val components = mutableListOf<ProductInfo>()

    private var anotherProductMaterialNumber: String? = null

    val enabledDetailsCleanBtn: LiveData<Boolean> = selectedPage
            .combineLatest(componentsSelectionsHelper.selectedPositions)
            .combineLatest(countValue)
            .map {
                val selectedTabPos = selectedPage.value ?: 0
                val selectedComponentsPositions = componentsSelectionsHelper.selectedPositions.value
                if (selectedTabPos == 0) processExciseAlcoProductService.taskRepository.getTotalCountForProduct(setProductInfo.value!!) > 0.0 else !selectedComponentsPositions.isNullOrEmpty()
            }

    val editTextFocus: MutableLiveData<Boolean> = MutableLiveData()


    init {
        launchUITryCatch {

            stampsCollectorManager.newStampCollector(processExciseAlcoProductService)

            processServiceManager.getWriteOffTask()!!.taskDescription.moveTypes.let { reasons ->
                if (reasons.isEmpty()) {
                    writeOffReasons.value = listOf(WriteOffReason.emptyWithTitle(resourceManager.emptyCategory()))
                } else {
                    setProductInfo.value?.let { it ->
                        val defaultReason = goodInformationRepo.getDefaultReason(
                                taskType = processServiceManager.getWriteOffTask()!!.taskDescription.taskType.code,
                                sectionId = it.sectionId,
                                materialNumber = it.materialNumber
                        )

                        writeOffReasons.value = mutableListOf(WriteOffReason.empty)
                                .apply {
                                    addAll(reasons)
                                }.filter { filterReason(it) }

                        onClickPosition(writeOffReasons.value!!.indexOfFirst { reason -> reason.code == defaultReason })
                    }
                }
            }



            searchSetDelegate.init(
                    scanResultHandler = this@SetsViewModel::handleSetSearchResult,
                    checksEnabled = false
            )

            searchComponentDelegate.init(
                    scanResultHandler = this@SetsViewModel::handleComponentSearchResult,
                    checksEnabled = false
            )

            searchProductDelegate.init(
                    scanResultHandler = this@SetsViewModel::handleProductSearchResult
            )

            componentsDataList = withContext(Dispatchers.IO) {
                return@withContext zmpUtz46V001.getComponentsForSet(setProductInfo.value!!.materialNumber)
            }

            componentsDataList.forEachIndexed { index, _ ->
                searchComponentDelegate.copy().searchCode(componentsDataList[index].matnr)
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

            editTextFocus.postValue(true)

        }
    }

    private fun filterReason(writeOffReason: WriteOffReason): Boolean {
        return writeOffReason === WriteOffReason.empty || writeOffReason.gisControl == if (setProductInfo.value!!.type == ProductType.General) "N" else "A"
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
    }

    fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    private fun addSet() {
        countValue.value?.let {
            if (countValue.value!! > 0.0) {
                stampsCollectorManager.getSetsStampCollector()!!.processAllForSet(getReason(), countValue.value!!)
            }
            count.value = ""
        }
    }

    private fun getReason(): WriteOffReason {
        return writeOffReasons.value?.getOrNull(selectedPosition.value
                ?: -1) ?: WriteOffReason.empty
    }

    private fun handleSetSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        eanCode.value = ""
        scanInfoResult?.productInfo?.let { info ->
            stampsCollectorManager.clearComponentsStampCollector()
            openComponentScreen(info.materialNumber)
        }

        return true

    }

    private fun handleComponentSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        Logg.d { "scanInfoResult: $scanInfoResult" }
        scanInfoResult?.let {
            components.add(it.productInfo)
            updateComponents()
        }

        return true
    }

    private fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        Logg.d { "scanInfoResult: $scanInfoResult" }
        onClickApply()
        return false
    }

    private fun openComponentScreen(materialNumber: String) {
        components.forEachIndexed { index, componentInfo ->
            if (componentInfo.materialNumber == materialNumber) {
                if (getCountExciseStampsForComponent(componentInfo) == componentsDataList[index].menge * totalCount.value!!) {
                    screenNavigator.openStampsCountAlreadyScannedScreen()
                } else {
                    screenNavigator.openComponentSetScreen(
                            componentInfo,
                            componentsLiveData.value!![index],
                            componentsLiveData.value!![index].rightCount)
                }
                return
            }
        }
        if (enabledApplyButton.value == true || countValue.value == 0.0) {
            searchProductDelegate.searchCode(code = materialNumber)
        } else {
            anotherProductMaterialNumber = materialNumber
            screenNavigator.openNotAllComponentProcessedScreen(requestCodeNotSaveComponents)
        }

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
            searchSetDelegate.searchCode(code)
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
        componentsSelectionsHelper.selectedPositions.value?.map { position ->
            components[position]
        }?.let {
            stampsCollectorManager.clearAllStampsCollectors()
        }
        updateComponents()
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
        if (fragmentResultCode == requestCodeNotSaveComponents) {
            stampsCollectorManager.clearAllStampsCollectors()
            count.value = "0"
            anotherProductMaterialNumber?.let {
                searchProductDelegate.searchCode(it)
            }

            return
        }
    }

    fun onClickItemPosition(position: Int) {
        components.getOrNull(position)?.let {
            searchEANCode(it.materialNumber)
        }
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