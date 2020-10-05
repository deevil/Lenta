package com.lenta.bp9.features.goods_information.sets.task_pge

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessNonExciseSetsPGEProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskSetsInfo
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
import com.lenta.shared.utilities.databinding.Evenable
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

class NonExciseSetsPGEViewModel : CoreViewModel(),
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
    lateinit var processNonExciseSetsPGEProductService: ProcessNonExciseSetsPGEProductService

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

    val acceptTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)

            if (qualityInfo.value?.get(it!!.second)?.code == "1" || qualityInfo.value?.get(it!!.second)?.code == "2") {
                (it?.first ?: 0.0) + countAccept
            } else {
                countAccept
            }
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
            it == 0.0 -> {
                "0 ${productInfo.value?.uom?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)

            if (qualityInfo.value?.get(it!!.second)?.code == "3" || qualityInfo.value?.get(it!!.second)?.code == "4" || qualityInfo.value?.get(it!!.second)?.code == "5") {
                (it?.first ?: 0.0) + countRefusal
            } else {
                countRefusal
            }
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)
        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.uom?.name}"
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
                        if (processNonExciseSetsPGEProductService.newProcessNonExciseSetsPGEProductService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@NonExciseSetsPGEViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.uom?.name
            if (isDiscrepancy.value == true) {
                val processingUnitsOfProduct = productInfo.value?.let {
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProducts()
                            ?.getProcessingUnitsOfProduct(it.materialNumber)
                }.orEmpty()
                count.value = if (processingUnitsOfProduct.size > 1) { //если у товара две ЕО
                    val countOrderQuantity = processingUnitsOfProduct.map { unitInfo ->
                        unitInfo.orderQuantity.toDouble()
                    }.sumByDouble {
                        it
                    }
                    productInfo.value?.let {
                        taskManager
                                .getReceivingTask()
                                ?.taskRepository
                                ?.getProductsDiscrepancies()
                                ?.getCountProductNotProcessedOfProductPGEOfProcessingUnits(
                                        product = it,
                                        orderQuantity = countOrderQuantity)
                                .toStringFormatted()
                    }

                } else {
                    productInfo.value?.let {
                        taskManager
                                .getReceivingTask()
                                ?.taskRepository
                                ?.getProductsDiscrepancies()
                                ?.getCountProductNotProcessedOfProductPGE(it)
                                .toStringFormatted()
                    }
                }
                qualityInfo.value = dataBase.getQualityInfoPGEForDiscrepancy()
                spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast { it.code == "3" }
            } else {
                qualityInfo.value = dataBase.getQualityInfoPGE()
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            spinProcessingUnit.value = listOf("${context.getString(R.string.prefix_processing_unit)}${productInfo.value?.processingUnit.orEmpty()}")
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
                        val countProcessedComponents = processNonExciseSetsPGEProductService.getCountDiscrepanciesOfComponent(taskSetsInfo.componentNumber).toStringFormatted()
                        val howMuchProcessComponents = (taskSetsInfo.quantity * (countValue.value
                                ?: 0.0)).toStringFormatted()
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
        processNonExciseSetsPGEProductService.setCountSet(countValue.value ?: 0.0)
    }

    fun onClickClean() {
        componentsSelectionsHelper.selectedPositions.value?.map { position ->
            listComponents.value?.let {
                processNonExciseSetsPGEProductService.cleanCurrentComponent(
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
            processNonExciseSetsPGEProductService.apply(count.value!!, qualityInfo.value!![spinQualitySelectedPosition.value
                    ?: 0].code, productInfo.value!!.processingUnit)
            count.value = "0"
            processNonExciseSetsPGEProductService.clearCurrentComponent()
            updateListComponents()
        }
    }

    fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    fun onClickItemPosition(position: Int) {
        listComponents.value?.get(position)?.componentInfo?.let {
            if (qualityInfo.value != null && spinQualitySelectedPosition.value != null && productInfo.value != null) {
                screenNavigator.openNonExciseSetComponentInfoPGEScreen(it, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, productInfo.value!!)
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
        onScanResult(eanCode.value.orEmpty())
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
            screenNavigator.openNonExciseSetComponentInfoPGEScreen(componentInfo, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, productInfo.value!!)
        }
    }

    private fun searchCode(data: String): ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST? {
        val eanInfo = zmpUtz25V001.getEanInfo(ean = data)
        //не менять последовательность
        return zfmpUtz48V001.getProductInfoByMaterial(material = eanInfo?.material)
                ?: zfmpUtz48V001.getProductInfoByMatcode(matcode = data)
                ?: zfmpUtz48V001.getProductInfoByMaterial(material = "000000000000${data.takeLast(6)}")
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

}

data class ListComponentsItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val componentInfo: TaskSetsInfo,
        val full: Boolean,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}
