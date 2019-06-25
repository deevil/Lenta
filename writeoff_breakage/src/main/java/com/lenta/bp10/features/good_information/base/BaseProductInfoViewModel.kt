package com.lenta.bp10.features.good_information.base

import androidx.lifecycle.LiveData
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
import com.lenta.shared.models.core.ProductType
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

    val writeOffReasons: MutableLiveData<List<WriteOffReason>> = MutableLiveData()
    val writeOffReasonTitles: LiveData<List<String>> = writeOffReasons.map { it?.map { reason -> reason.name } }

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

    open val enabledApplyButton: MutableLiveData<Boolean> by lazy {
        countValue.combineLatest(selectedPosition).map {
            isEnabledApplyButtons(
                    count = it?.first,
                    productInfo = productInfo.value,
                    isSetComponent = false,
                    reason = getSelectedReason(),
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
                        writeOffReasons.value = listOf(WriteOffReason.emptyWithTitle(resourceManager.emptyCategory()))
                    } else {
                        productInfo.value?.let { it ->
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
            }

            suffix.value = productInfo.value?.uom?.name

        }
    }

    open fun filterReason(writeOffReason: WriteOffReason): Boolean {
        return writeOffReason === WriteOffReason.empty || writeOffReason.gisControl == (if (productInfo.value!!.type == ProductType.General) "N" else "A")
    }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    open fun onResult(code: Int?) {
        searchProductDelegate.handleResultCode(code)

    }

    protected fun getSelectedReason(): WriteOffReason {
        return writeOffReasons.value?.getOrNull((selectedPosition.value ?: -1))
                ?: WriteOffReason.empty
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