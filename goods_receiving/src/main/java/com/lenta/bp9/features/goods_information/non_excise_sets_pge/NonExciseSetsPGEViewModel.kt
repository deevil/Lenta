package com.lenta.bp9.features.goods_information.non_excise_sets_pge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_information.excise_alco_receiving.excise_alco_box_acc.excise_alco_box_list.BoxListItem
import com.lenta.bp9.features.goods_list.ListCountedItem
import com.lenta.bp9.features.goods_list.ListWithoutBarcodeItem
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessNonExciseSetsPGEProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskSetsInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
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

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val componentsSelectionsHelper = SelectionItemsHelper()
    val listComponents: MutableLiveData<List<ListComponentsItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()
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

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        (it ?: 0.0) > 0.0
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@NonExciseSetsPGEViewModel::viewModelScope,
                    scanResultHandler = this@NonExciseSetsPGEViewModel::handleProductSearchResult)

            suffix.value = productInfo.value?.uom?.name
            if (isDiscrepancy.value!!) {
                count.value = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProduct(productInfo.value!!).toStringFormatted()
                qualityInfo.value = dataBase.getQualityInfoPGEForDiscrepancy()
                spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "4"}
            } else {
                qualityInfo.value = dataBase.getQualityInfoPGE()
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            spinProcessingUnit.value = listOf("ЕО - ${productInfo.value!!.processingUnit}")

            if (processNonExciseSetsPGEProductService.newProcessNonExciseSetsPGEProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
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
        repoInMemoryHolder.sets.value.let {setsInfoList ->
            listComponents.postValue(
                    setsInfoList?.filter {filterSetInfo ->
                        filterSetInfo.setNumber == productInfo.value?.materialNumber
                    }?.sortedByDescending {sorted ->
                        sorted.componentNumber
                    }?.mapIndexed { index, taskSetsInfo ->
                        val componentDescription = zfmpUtz48V001.getProductInfoByMaterial(taskSetsInfo.componentNumber)?.name
                        ListComponentsItem(
                                number = index + 1,
                                name = "${taskSetsInfo.componentNumber.substring(taskSetsInfo.componentNumber.length - 6)} $componentDescription",
                                quantity = "${taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getAllCountDiscrepanciesOfProduct(taskSetsInfo.componentNumber).toStringFormatted()} из ${(taskSetsInfo.quantity * (countValue.value ?: 0.0)).toStringFormatted()}",
                                componentInfo = taskSetsInfo,
                                even = index % 2 == 0
                        )
                    }
            )
        }

        componentsSelectionsHelper.clearPositions()
    }

    fun onClickClean() {
        componentsSelectionsHelper.selectedPositions.value?.map { position ->
            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getProductsDiscrepancies()
                    ?.deleteProductsDiscrepanciesForProduct(listComponents.value?.get(position)!!.componentInfo.componentNumber)
        }

        updateListComponents()
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() {
        processNonExciseSetsPGEProductService.addSet(count.value!!, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, productInfo.value!!.processingUnit)
    }

    fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    fun onClickItemPosition(position: Int) {
        listComponents.value?.get(position)?.componentInfo?.let { screenNavigator.openNonExciseSetComponentInfoPGEScreen(it) }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
        updateListComponents()
    }

    fun onClickPositionSpinQuality(position: Int) {
        spinQualitySelectedPosition.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            searchProductDelegate.searchCode(it, fromScan = false)
        }
        return true
    }

    override fun onClickPosition(position: Int) {
        spinProcessingUnitSelectedPosition.value = position
    }

    fun onScanResult(data: String) {
        val componentNumber: TaskSetsInfo? = repoInMemoryHolder.sets.value?.findLast {
            it.componentNumber == data
        }
        if (componentNumber == null) {
            screenNavigator.openAlertGoodsNotFoundTaskScreen()
        } else {
            screenNavigator.openNonExciseSetComponentInfoPGEScreen(componentNumber)
        }
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
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}
