package com.lenta.bp10.features.good_information.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.IGoodInformationRepo
import com.lenta.bp10.features.good_information.getCountWithUom
import com.lenta.bp10.features.good_information.isEnabledApplyButtons
import com.lenta.bp10.features.good_information.isEnabledDetailsButton
import com.lenta.bp10.features.goods_list.SearchProductDelegate
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.resources.IStringResourceManager
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseProductInfoViewModel : CoreViewModel(), OnPositionClickListener {
    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var resourceManager: IStringResourceManager
    @Inject
    lateinit var goodInformationRepo: IGoodInformationRepo
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()

    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val count: MutableLiveData<String> by lazy { initCountLiveData() }

    val suffix: MutableLiveData<String> = MutableLiveData()

    protected val countValue: MutableLiveData<Double> by lazy {
        count.map {
            it?.toDoubleOrNull() ?: 0.0
        }
    }

    val totalCount: MutableLiveData<Double> by lazy {
        countValue.map {
            (it ?: 0.0) + getProcessTotalCount()
        }
    }

    val enabledDetailsButton: MutableLiveData<Boolean>  by lazy {
        totalCount.map {
            isEnabledDetailsButton(getProcessTotalCount())
        }
    }

    val totalCountWithUom: MutableLiveData<String> by lazy {
        totalCount.map { getCountWithUom(count = it, productInfo = productInfo) }
    }

    val enabledApplyButton: MutableLiveData<Boolean> by lazy {
        countValue.combineLatest(selectedPosition).map {
            isEnabledApplyButtons(
                    count = it?.first,
                    productInfo = productInfo.value,
                    reason = getReason(),
                    taskRepository = getTaskRepo()
            )
        }
    }


    init {
        viewModelScope.launch {

            searchProductDelegate.init(
                    viewModelScope = this@BaseProductInfoViewModel::viewModelScope,
                    scanResultHandler = this@BaseProductInfoViewModel::handleProductSearchResult
            )

            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                getTaskDescription().moveTypes.let { reasons ->
                    if (reasons.isEmpty()) {
                        writeOffReasonTitles.value = listOf(resourceManager.emptyCategory())
                    } else {
                        productInfo.value?.let { it ->
                            val defaultReason = goodInformationRepo.getDefaultReason(
                                    taskType = processServiceManager.getWriteOffTask()!!.taskDescription.taskType.code,
                                    sectionId = it.sectionId,
                                    materialNumber = it.materialNumber
                            )

                            writeOffReasonTitles.value = mutableListOf("").filter { filterReason(it) }
                                    .toMutableList()
                                    .apply {
                                        addAll(reasons.filter { filterReason(it.code) }.map { it.name })
                                    }

                            onClickPosition(reasons.indexOfFirst { reason -> reason.code == defaultReason } + 1)
                        }
                    }
                }
            }

            suffix.value = productInfo.value?.uom?.name

        }
    }

    open fun filterReason(code: String): Boolean {
        return true
    }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    open fun onResult(code: Int?) {
        searchProductDelegate.handleResultCode(code)

    }

    open fun getReason(): WriteOffReason {
        getTaskDescription().moveTypes.let { moveTypes ->
            if (moveTypes.isEmpty()) {
                return WriteOffReason(code = "", name = resourceManager.emptyCategory())
            }
            return moveTypes
                    .getOrElse((selectedPosition.value ?: 0) - 1) { WriteOffReason.empty }
        }

    }

    protected fun getSelectedReason(): WriteOffReason {
        getTaskDescription().moveTypes.let { moveTypes ->
            return if (moveTypes.isEmpty()) {
                WriteOffReason(code = "", name = resourceManager.emptyCategory())
            } else {
                moveTypes.getOrElse((selectedPosition.value ?: 0) - 1) { WriteOffReason.empty }
            }
        }

    }

    abstract fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean

    abstract fun getTaskDescription(): TaskDescription

    abstract fun getTaskRepo(): ITaskRepository

    abstract fun getProcessTotalCount(): Double

    abstract fun onClickAdd()

    abstract fun onClickApply()

    abstract fun initCountLiveData(): MutableLiveData<String>

    fun onClickDetails() {
        productInfo.value?.let {
            screenNavigator.openGoodsReasonsScreen(productInfo = it)
        }

    }


    override fun onClickPosition(position: Int) {
        Logg.d { "onClickPosition $position" }
        selectedPosition.postValue(position)
    }

    abstract fun onBackPressed()

    abstract fun onScanResult(data: String)

}