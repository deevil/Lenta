package com.lenta.bp10.features.good_information.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.*
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
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.delay
import javax.inject.Inject

abstract class BaseProductInfoViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

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

    val count: MutableLiveData<String> by lazy {
        initCountLiveData()
    }

    val suffix: MutableLiveData<String> = MutableLiveData()

    val requestFocusToQuantity = MutableLiveData(false)

    internal var limitsChecker: LimitsChecker? = null

    protected val countValue: MutableLiveData<Double> by lazy {
        count.map {
            (it?.toDoubleOrNull() ?: 0.0)
        }
    }

    val totalCount: MutableLiveData<Double> by lazy {
        countValue.map {
            (it ?: 0.0) + getProcessTotalCount()
        }
    }

    val enabledDetailsButton: MutableLiveData<Boolean> by lazy {
        totalCount.map {
            isEnabledDetailsButton(getProcessTotalCount())
        }
    }

    open val totalCountWithUom: MutableLiveData<String> by lazy {
        totalCount.map { getCountWithUom(count = it, productInfo = productInfo) }
    }

    open val enabledApplyButton: MutableLiveData<Boolean> by lazy {
        countValue.combineLatest(reasonPosition).map {
            isEnabledApplyButtons(
                    count = it?.first,
                    productInfo = productInfo.value,
                    isSetComponent = false,
                    reason = getSelectedReason(),
                    taskRepository = getTaskRepo()
            )
        }
    }

    val reasonPosition = MutableLiveData(0)

    val onSelectReason = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            reasonPosition.value = position
        }
    }

    init {
        launchUITryCatch {

            limitsChecker = LimitsChecker(
                    limit = goodInformationRepo.getLimit(getTaskDescription().taskType.code, productInfo.value!!.type),
                    observer = { screenNavigator.openLimitExceededScreen() },
                    countLiveData = totalCount,
                    viewModelScope = this@BaseProductInfoViewModel::viewModelScope

            )

            searchProductDelegate.init(
                    scanResultHandler = this@BaseProductInfoViewModel::handleProductSearchResult,
                    limitsChecker = limitsChecker
            )

            processServiceManager.getWriteOffTask()?.let {
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

                            writeOffReasons.value!!.indexOfFirst { reason -> reason.code == defaultReason }.let { position ->
                                reasonPosition.value = position
                                requestFocusToQuantity.value = true
                            }
                        }
                    }
                }
            }

            suffix.value = productInfo.value?.uom?.name
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        if (enabledApplyButton.value == true) {
            onClickApply()
        }
        return true
    }

    open fun filterReason(writeOffReason: WriteOffReason): Boolean {
        return writeOffReason === WriteOffReason.empty || writeOffReason.gisControl == (if (productInfo.value!!.type == ProductType.General) "N" else "A")
    }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    protected fun getSelectedReason(): WriteOffReason {
        return writeOffReasons.value?.getOrNull((reasonPosition.value ?: -1))
                ?: WriteOffReason.empty
    }

    protected fun showNotPossibleSaveScreen() {
        if (getSelectedReason() === WriteOffReason.empty) {
            screenNavigator.openNotPossibleSaveWithoutReasonScreen()
        } else {
            screenNavigator.openNotPossibleSaveNegativeQuantityScreen()
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

    abstract fun onBackPressed(): Boolean

    abstract fun onScanResult(data: String)

    fun initCount(it: Double) {
        launchUITryCatch {
            delay(100)
            count.value = it.toStringFormatted()
            requestFocusToQuantity.value = true
        }
    }

}