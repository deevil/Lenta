package com.lenta.bp10.features.good_information.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
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

    private val writeOffReasons: LiveData<List<WriteOffReason>> = productInfo.switchMap {
        asyncLiveData<List<WriteOffReason>> {
            val reasons = getWriteOffReasons()
            val taskCode = getTaskDescription().taskType.code
            val reasonCode = goodInformationRepo.getStartReasonPosition(taskCode, it.sectionId)
            val positionIndex = reasons.indexOfFirst { it.code == reasonCode }
            reasonPosition.postValue(positionIndex)
            emit(reasons)
        }
    }

    val writeOffReasonTitles: LiveData<List<String>> by unsafeLazy {
        writeOffReasons.switchMap {
            asyncLiveData<List<String>> {
                val reasons = it.map { reason ->
                    reason.name
                }
                emit(reasons)
            }
        }
    }

    val count: MutableLiveData<String> by unsafeLazy {
        initCountLiveData()
    }

    val suffix = productInfo.mapSkipNulls {
        it.uom.name
    }

    val requestFocusToQuantity = MutableLiveData(false)

    internal var limitsChecker: LimitsChecker? = null

    protected val countValue by unsafeLazy {
        count.mapSkipNulls {
            it.toDoubleOrNull() ?: 1.0
        }
    }

    val totalCount: MutableLiveData<Double> by unsafeLazy {
        countValue.mapSkipNulls {
            (it ?: DEFAULT_COUNT_VALUE) + getProcessTotalCount()
        }
    }

    val isSpecialMode = MutableLiveData(false)

    open val totalCountWithUom: MutableLiveData<String> by unsafeLazy {
        totalCount.mapSkipNulls {
            getCountWithUom(count = it, productInfo = productInfo)
        }
    }

    val reasonPosition = MutableLiveData(0)

    /**
    Кнопки нижнего тулбара
     */

    val enabledDetailsButton: MutableLiveData<Boolean> by unsafeLazy {
        totalCount.map {
            isEnabledDetailsButton(getProcessTotalCount())
        }
    }

    open val enabledApplyButton: MutableLiveData<Boolean> by unsafeLazy {
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

    val damagedEnabled: LiveData<Boolean> by unsafeLazy {
        enabledApplyButton.combineLatest(isSpecialMode).mapSkipNulls {
            val (enabledApplyButton, isSpecialMode) = it
            if (isSpecialMode) isSpecialMode else enabledApplyButton
        }
    }

    /**
    Блок инициализации
     */

    init {
        initViewModel()
    }

    private fun initViewModel() {
        launchUITryCatch {
            initSpecialMode()

            productInfo.value?.let {
                val taskCode = getTaskDescription().taskType.code
                val taskType = it.type

                limitsChecker = LimitsChecker(
                        limit = goodInformationRepo.getLimit(taskCode, taskType),
                        observer = navigator::openLimitExceededScreen,
                        countLiveData = totalCount,
                        viewModelScope = this@BaseProductInfoViewModel::viewModelScope

                )

                searchProductDelegate.init(
                        scanResultHandler = this@BaseProductInfoViewModel::handleProductSearchResult,
                        limitsChecker = limitsChecker
                )
            }
        }
    }

    /**
    Методы
     */

    private fun getWriteOffReasons(): List<WriteOffReason> {
        val reasons = getTaskDescription().moveTypes.toMutableList()
        reasons.add(0, WriteOffReason.emptyWithTitle(resourceManager.emptyCategory()))
        return reasons.filter { writeOffReason ->
            filterReason(writeOffReason)
        }
    }

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
        val typeCode = productInfo.value?.type?.code.orEmpty()
        val gisControl = writeOffReason.gisControl
        return writeOffReason === WriteOffReason.empty || gisControl == typeCode
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

    open fun updateCounter() {
        count.value = initCountLiveData().value
    }

    abstract fun onBackPressed(): Boolean

    abstract fun onScanResult(data: String)

    fun initCount(it: Double) {
        launchUITryCatch {
            delay(DEFAULT_DELAY)
            count.value = it.toStringFormatted()
            requestFocusToQuantity.value = true
        }
    }

    companion object {
        const val DEFAULT_COUNT_VALUE = 0.0
        private const val DEFAULT_DELAY = 100L
    }

}