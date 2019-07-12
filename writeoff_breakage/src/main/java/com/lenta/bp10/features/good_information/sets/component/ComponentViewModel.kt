package com.lenta.bp10.features.good_information.sets.component

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.features.good_information.excise_alco.ExciseAlcoDelegate
import com.lenta.bp10.features.good_information.isEnabledApplyButtons
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.models.StampsCollectorManager
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class ComponentViewModel : BaseProductInfoViewModel() {


    private var targetTotalCount: Double = 0.0

    private lateinit var componentItem: ComponentItem

    @Inject
    lateinit var exciseAlcoDelegate: ExciseAlcoDelegate

    @Inject
    lateinit var stampsCollectorManager: StampsCollectorManager

    override val enabledApplyButton: MutableLiveData<Boolean> by lazy {
        countValue.combineLatest(selectedPosition).map {
            isEnabledApplyButtons(
                    count = it?.first,
                    productInfo = productInfo.value,
                    isSetComponent = true,
                    reason = getSelectedReason(),
                    taskRepository = getTaskRepo()
            )
        }
    }

    //Not used for this screen
    override val totalCountWithUom: MutableLiveData<String> = MutableLiveData()

    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    init {
        viewModelScope.launch {
            exciseAlcoDelegate.init(
                    viewModelScope = this@ComponentViewModel::viewModelScope,
                    handleNewStamp = this@ComponentViewModel::handleNewStamp,
                    tkNumber = getTaskDescription().tkNumber,
                    materialNumber = productInfo.value!!.materialNumber
            )
        }

    }

    fun setComponentItem(componentItem: ComponentItem) {
        this.componentItem = componentItem
    }

    private fun getCountSavedExciseStamps(): Double {
        return stampsCollectorManager.getSetsStampCollector()!!.getCount(productInfo.value!!.materialNumber)
    }


    override fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        //not used search product for this screen
        return true

    }

    override fun getTaskDescription(): TaskDescription {
        return processServiceManager.getWriteOffTask()!!.taskDescription
    }

    override fun getTaskRepo(): ITaskRepository {
        return processServiceManager.getWriteOffTask()!!.taskRepository
    }

    override fun getProcessTotalCount(): Double {
        return processExciseAlcoProductService.getTotalCount() + getCountSavedExciseStamps()
    }

    override fun onClickAdd() {
        stampsCollectorManager.saveStampsToSet()
    }

    override fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    override fun onBackPressed(): Boolean {
        stampsCollectorManager.clearComponentsStampCollector()
        return true
    }


    override fun onScanResult(data: String) {
        if (totalCount.value ?: 0.0 >= targetTotalCount) {
            screenNavigator.openStampsCountAlreadyScannedScreen()
            return
        }
        if (stampsCollectorManager.getComponentsStampCollector()!!.prepare(stampCode = data)) {
            exciseAlcoDelegate.searchExciseStamp(data)
        } else {
            screenNavigator.openAlertDoubleScanStamp()
        }
    }

    private fun handleNewStamp(isBadStamp: Boolean) {
        if (!stampsCollectorManager.addStampToComponentsStampCollector(
                        materialNumber = productInfo.value!!.materialNumber,
                        setMaterialNumber = componentItem.setMaterialNumber,
                        writeOffReason = getSelectedReason().code,
                        isBadStamp = isBadStamp
                )) {
            screenNavigator.openAlertDoubleScanStamp()
        }
    }

    fun onClickRollBack() {
        stampsCollectorManager.getComponentsStampCollector()!!.rollback()
    }

    override fun filterReason(writeOffReason: WriteOffReason): Boolean {
        return writeOffReason.gisControl == (if (productInfo.value!!.type == ProductType.General) "N" else "A") && writeOffReason.code == componentItem.writeOffReason.code
    }

    override fun initCountLiveData(): MutableLiveData<String> {
        return stampsCollectorManager.getComponentsStampCollector()!!.observeCount().map { it.toStringFormatted() }
    }

    fun setTargetTotalCount(targetTotalCount: Double) {
        this.targetTotalCount = targetTotalCount
    }

    override fun handleFragmentResult(code: Int?): Boolean {
        if (exciseAlcoDelegate.handleResult(code)) {
            return true
        }
        return super.handleFragmentResult(code)

    }

    fun getTargetCount(): String {
        return targetTotalCount.toStringFormatted()
    }

}
