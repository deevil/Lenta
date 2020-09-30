package com.lenta.bp9.features.goods_information.sets.task_ppp

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_information.sets.task_pge.ListComponentsItem
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessNonExciseSetsReceivingProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskSetsInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.fmp.resources.dao_ext.getEanInfo
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMatcode
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class NonExciseSetsReceivingViewModel : CoreViewModel(),
        PageSelectionListener,
        OnOkInSoftKeyboardListener,
        OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var processNonExciseSetsReceivingProductService: ProcessNonExciseSetsReceivingProductService

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var context: Context

    private val zmpUtz25V001: ZmpUtz25V001 by lazy {
        ZmpUtz25V001(hyperHive)
    }

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val componentsSelectionsHelper = SelectionItemsHelper()
    val listComponents: MutableLiveData<List<ListComponentsItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProcessingUnit: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnitSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val tvAccept: MutableLiveData<String> by lazy {
        MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
    }

    val acceptTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)

            if (qualityInfo.value?.get(it!!.second)?.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                (it?.first ?: 0.0) + countAccept
            } else {
                countAccept
            }
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
        val totalCount = it ?: DEFAULT_DOUBLE_ZERO_VALUE

        when {
            totalCount > DEFAULT_DOUBLE_ZERO_VALUE -> {
                val purchaseName = productInfo.value?.purchaseOrderUnits?.name
                purchaseName?.run {
                    buildString {
                        append(LEAD_PLUS)
                        append(totalCount.toStringFormatted())
                        append(" ")
                        append(purchaseName)
                    }
                }.orEmpty()
            }
            totalCount == DEFAULT_DOUBLE_ZERO_VALUE -> {
                val purchaseName = productInfo.value?.purchaseOrderUnits?.name
                purchaseName?.run {
                    buildString {
                        append(LEAD_ZERO)
                        append(purchaseName)
                    }
                }.orEmpty()
            }
            else -> {
                val firstPart = LEAD_PLUS.takeIf{countAccept > DEFAULT_DOUBLE_ZERO_VALUE }.orEmpty()
                val middlePart = countAccept.toStringFormatted()
                val lastPart = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
                buildString {
                    append(firstPart)
                    append(middlePart)
                    append(" ")
                    append(lastPart)
                }
            }
        }
    }

    /**val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
            }
            it == 0.0 -> {
                "0 ${productInfo.value?.purchaseOrderUnits?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
            }
        }
    }*/

    val refusalTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)

            if (qualityInfo.value?.get(it!!.second)?.code != "1") {
                (it?.first ?: 0.0) + countRefusal
            } else {
                countRefusal
            }
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)
        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
        }
    }

    val enabledCleanButton: MutableLiveData<Boolean> = componentsSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(listComponents).map {
        it?.second?.filter { componentItem ->
            !componentItem.full
        }?.size == 0
    }

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processNonExciseSetsReceivingProductService.newProcessNonExciseSetsReceivingProductService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }
                    .orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@NonExciseSetsReceivingViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.purchaseOrderUnits?.name
            if (isDiscrepancy.value == true) {
                productInfo.value?.let {
                    count.value = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProduct(it).toStringFormatted()
                }
                qualityInfo.value = dataBase.getQualityInfoForDiscrepancy()
                spinQualitySelectedPosition.value = qualityInfo.value?.indexOfLast { it.code == "4" }
            } else {
                qualityInfo.value = dataBase.getQualityInfo()
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        eanCode.postValue("")
        return false
    }

    fun onResume() {
        updateListComponents()
    }

    private fun updateListComponents() {
        repoInMemoryHolder.sets.value.let { setsInfoList ->
            listComponents.postValue(
                    setsInfoList?.filter { filterSetInfo ->
                        filterSetInfo.setNumber == productInfo.value?.materialNumber
                    }?.sortedByDescending { sorted ->
                        sorted.componentNumber
                    }?.mapIndexed { index, taskSetsInfo ->
                        val componentDescription = zfmpUtz48V001.getProductInfoByMaterial(taskSetsInfo.componentNumber)?.name
                        val countProcessedComponents = processNonExciseSetsReceivingProductService.getCountDiscrepanciesOfComponent(taskSetsInfo.componentNumber).toStringFormatted()
                        val howMuchProcessComponents = ((productInfo.value?.quantityInvest?.toDouble()
                                ?: 0.0) * (countValue.value
                                ?: 0.0) * taskSetsInfo.quantity).toStringFormatted()
                        ListComponentsItem(
                                number = index + 1,
                                name = "${taskSetsInfo.componentNumber.substring(taskSetsInfo.componentNumber.length - 6)} $componentDescription",
                                quantity = "$countProcessedComponents ${context.getString(R.string.of)} $howMuchProcessComponents",
                                componentInfo = taskSetsInfo,
                                full = countProcessedComponents == howMuchProcessComponents && (countProcessedComponents.toDouble() + howMuchProcessComponents.toDouble()) > 0,
                                even = index % 2 == 0
                        )
                    }
            )
        }

        componentsSelectionsHelper.clearPositions()
    }

    fun onClickClean() {
        componentsSelectionsHelper.selectedPositions.value?.map { position ->
            listComponents.value?.let {
                processNonExciseSetsReceivingProductService.cleanCurrentComponent(
                        it[position].componentInfo.componentNumber
                )
            }
        }

        updateListComponents()
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() {
        if (count.value != null && qualityInfo.value != null) {
            processNonExciseSetsReceivingProductService.apply(count.value!!, qualityInfo.value!![spinQualitySelectedPosition.value
                    ?: 0].code)
            count.value = "0"
            processNonExciseSetsReceivingProductService.clearCurrentComponent()
            updateListComponents()
        }
    }

    fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    fun onClickItemPosition(position: Int) {
        listComponents.value?.get(position)?.componentInfo?.let {
            val qualityInfoValue = qualityInfo.value
            val productInfoValue = productInfo.value
            val spinQualitySelectedPositionValue = spinQualitySelectedPosition.value
            if (qualityInfoValue != null && spinQualitySelectedPositionValue != null && productInfoValue != null) {
                screenNavigator.openNonExciseSetComponentInfoReceivingScreen(it, qualityInfoValue[spinQualitySelectedPositionValue].code, productInfoValue)
            } else {
                screenNavigator.openAlertGoodsNotFoundTaskScreen()
            }
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
        updateListComponents()
        setRequestFocus()
    }

    private fun setRequestFocus() {
        when (selectedPage.value) {
            0 -> {
                requestFocusToEan.value = false
                requestFocusToCount.value = true
            }
            1 -> {
                requestFocusToCount.value = false
                requestFocusToEan.value = true
            }
        }
    }

    fun onClickPositionSpinQuality(position: Int) {
        spinQualitySelectedPosition.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        onScanResult(eanCode.value ?: "")
        return true
    }

    override fun onClickPosition(position: Int) {
        spinProcessingUnitSelectedPosition.value = position
    }

    fun onScanResult(data: String) {
        val componentNumber = searchCode(data)?.material
        val componentInfo: TaskSetsInfo? = repoInMemoryHolder.sets.value?.findLast {
            it.setNumber == productInfo.value?.materialNumber && it.componentNumber == componentNumber
        }
        if (componentInfo == null) {
            screenNavigator.openAlertGoodsNotFoundTaskScreen()
        } else {
            screenNavigator.openNonExciseSetComponentInfoReceivingScreen(componentInfo, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, productInfo.value!!)
        }
    }

    private fun searchCode(data: String): ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST? {
        val eanInfo = zmpUtz25V001.getEanInfo(ean = data)
        //не менять последовательность
        return zfmpUtz48V001.getProductInfoByMaterial(material = eanInfo?.material)
                ?: zfmpUtz48V001.getProductInfoByMatcode(matcode = data)
                ?: zfmpUtz48V001.getProductInfoByMaterial(material = "${context.getString(R.string.code_with_12_digits)}${data.takeLast(6)}")
    }

    fun onBackPressed() {
        if (enabledApplyButton.value == true) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        screenNavigator.goBack()
                    }
            )
        } else {
            screenNavigator.goBack()
        }
    }

    companion object {
        private const val DEFAULT_DOUBLE_ZERO_VALUE = 0.0
        private const val LEAD_PLUS = "+ "
        private const val LEAD_ZERO = "0 "
    }

}
