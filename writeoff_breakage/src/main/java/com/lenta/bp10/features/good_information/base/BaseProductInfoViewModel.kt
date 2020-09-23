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
import com.lenta.bp10.repos.DatabaseRepository
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.delay
import javax.inject.Inject

abstract class BaseProductInfoViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var resourceManager: IStringResourceManager

    @Inject
    lateinit var goodInformationRepo: IGoodInformationRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var database: DatabaseRepository

    @Inject
    lateinit var sessionInfo: ISessionInfo


    /**
    Переменные
     */

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()

    val writeOffReasons: MutableLiveData<List<WriteOffReason>> = MutableLiveData()

    val writeOffReasonTitles: LiveData<List<String>> = writeOffReasons.map { it?.map { reason -> reason.name } }

    val count: MutableLiveData<String> by lazy {
        initCountLiveData()
    }

    val suffix = MutableLiveData("")

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

    val isSpecialMode = MutableLiveData(false)

    open val totalCountWithUom: MutableLiveData<String> by lazy {
        totalCount.map { getCountWithUom(count = it, productInfo = productInfo) }
    }

    val reasonPosition = MutableLiveData(0)

    val onSelectReason = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            reasonPosition.value = position
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val enabledDetailsButton: MutableLiveData<Boolean> by lazy {
        totalCount.map {
            isEnabledDetailsButton(getProcessTotalCount())
        }
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

    val damagedEnabled: LiveData<Boolean> by lazy {
        enabledApplyButton.combineLatest(isSpecialMode).mapSkipNulls {
            val (enabledApplyButton, isSpecialMode) = it
            if (isSpecialMode) isSpecialMode else enabledApplyButton
        }
    }

    /**
    Блок инициализации
     */

    init {
        launchUITryCatch {
            initSpecialMode()

            limitsChecker = LimitsChecker(
                    limit = goodInformationRepo.getLimit(getTaskDescription().taskType.code, productInfo.value!!.type),
                    observer = { navigator.openLimitExceededScreen() },
                    countLiveData = totalCount,
                    viewModelScope = this@BaseProductInfoViewModel::viewModelScope

            )

            searchProductDelegate.init(
                    scanResultHandler = this@BaseProductInfoViewModel::handleProductSearchResult,
                    limitsChecker = limitsChecker
            )

            processServiceManager.getWriteOffTask()?.let {
                getTaskDescription().moveTypes.let { reasons ->
                    writeOffReasons.value = if (reasons.isEmpty()) {
                        listOf(WriteOffReason.emptyWithTitle(resourceManager.emptyCategory()))
                    } else {
                        mutableListOf(WriteOffReason.empty).apply {
                            addAll(reasons)
                        }.filter { writeOffReason ->
                            filterReason(writeOffReason)
                        }
                    }
                }
            }

            suffix.value = productInfo.value?.uom?.name
        }
    }

    /**
    Методы
     */

    private suspend fun initSpecialMode() {
        val taskType = getTaskDescription().taskType.code
        isSpecialMode.value = database.isSpecialMode(taskType)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        if (enabledApplyButton.value == true) {
            onClickApply()
        }
        return true
    }

    open fun filterReason(writeOffReason: WriteOffReason): Boolean {
        return writeOffReason === WriteOffReason.empty || writeOffReason.gisControl == productInfo.value?.type?.code
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
            navigator.openNotPossibleSaveWithoutReasonScreen()
        } else {
            navigator.openNotPossibleSaveNegativeQuantityScreen()
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
            navigator.openGoodsReasonsScreen(productInfo = it)
        }
    }

    fun updateCounter() {
        count.value = initCountLiveData().value
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