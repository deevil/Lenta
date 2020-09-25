package com.lenta.bp10.features.goods_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.TaskWriteOffReason
import com.lenta.bp10.models.task.WriteOffTask
import com.lenta.bp10.models.task.getPrinterTask
import com.lenta.bp10.models.task.getReport
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.requestCodeDelete
import com.lenta.bp10.platform.requestCodeSelectPersonnelNumber
import com.lenta.bp10.requests.network.PrintTaskNetRequest
import com.lenta.bp10.requests.network.SendWriteOffDataNetRequest
import com.lenta.bp10.requests.network.SendWriteOffDataResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.resources.ISharedStringResourceManager
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
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    @Inject
    lateinit var sharedStringResourceManager: ISharedStringResourceManager

    @Inject
    lateinit var sendWriteOffDataNetRequest: SendWriteOffDataNetRequest

    @Inject
    lateinit var printTaskNetRequest: PrintTaskNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper


    val countedSelectionsHelper = SelectionItemsHelper()

    val filteredSelectionsHelper = SelectionItemsHelper()

    val eanCode = MutableLiveData("")

    val requestFocusToEan = MutableLiveData(false)

    val countedGoods: MutableLiveData<List<GoodItem>> = MutableLiveData()

    val filteredGoods: MutableLiveData<List<FilterItem>> = MutableLiveData()

    val categories: MutableLiveData<List<String>> = MutableLiveData()

    val selectedCategoryPosition = MutableLiveData(0)


    val deleteEnabled: MutableLiveData<Boolean> = selectedPage
            .combineLatest(countedSelectionsHelper.selectedPositions)
            .combineLatest(filteredSelectionsHelper.selectedPositions).map {

                val selectedTabPos = it?.first?.first ?: 0
                val selectedCountedPositions = it?.first?.second
                val selectedFilterPositions = it?.second
                val activeSet = if (selectedTabPos == 0) selectedCountedPositions else selectedFilterPositions

                activeSet?.isNotEmpty() ?: false || (selectedTabPos == 1 && !filteredGoods.value.isNullOrEmpty())
            }

    val saveButtonEnabled = countedGoods.map { it?.isNotEmpty() == true }

    val printButtonEnabled: MutableLiveData<Boolean> = countedGoods.map {
        !it.isNullOrEmpty() && (processServiceManager.getWriteOffTask()?.taskDescription?.printer?.isNotEmpty()
                ?: false)
    }


    val onCategoryPositionClickListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedCategoryPosition.value = position
            updateFilter()
        }
    }

    init {
        launchUITryCatch {
            searchProductDelegate.init(scanResultHandler = this@GoodsListViewModel::handleProductSearchResult)
            updateCounted()
            updateFilter()
        }

    }

    fun onResume() {
        updateCounted()
        updateFilter()

        requestFocusToEan.value = true
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            searchProductDelegate.searchCode(it)
        }
        return true
    }


    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure, pageNumber = "97")
    }


    fun getTitle(): String {
        processServiceManager.getWriteOffTask()?.let {
            return "${it.taskDescription.taskType.code} - ${it.taskDescription.taskName}"
        }
        return ""
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data)
    }

    fun onClickSave() {
        if (sessionInfo.personnelNumber.isNullOrEmpty()) {
            screenNavigator.openSelectionPersonnelNumberScreen(
                    codeConfirmation = requestCodeSelectPersonnelNumber,
                    isScreenMainMenu = true
            )
        } else {
            saveData()
        }
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {
        processServiceManager.getWriteOffTask()?.let { writeOffTask ->
            when (selectedPage.value) {
                0 -> {
                    countedSelectionsHelper.selectedPositions.value?.map { position ->
                        countedGoods.value!![position].productInfo
                    }?.let {
                        writeOffTask.deleteProducts(it)
                    }
                }
                1 -> {
                    with(filteredSelectionsHelper) {
                        val categoriesSize = categories.value?.size ?: 0
                        val isEmptyCategory = (selectedCategoryPosition.value == 0) && (categoriesSize > 1)
                        val countGoodsForRemove = filteredGoods.value?.size ?: 0

                        if (isSelectedEmpty() && isEmptyCategory) {
                            screenNavigator.openRemoveLinesConfirmationScreen(
                                    taskDescription = writeOffTask.taskDescription.taskName,
                                    count = countGoodsForRemove,
                                    codeConfirmation = requestCodeDelete
                            )

                            return
                        }

                        if (isSelectedEmpty()) {
                            filteredGoods.value?.let { addAll(it) }
                            screenNavigator.openRemoveItemsFromSelectedCategory(countGoodsForRemove) {
                                removeSelectedFilteredPositions(writeOffTask)
                            }

                            return
                        }

                        removeSelectedFilteredPositions(writeOffTask)
                    }
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }

            selectedCategoryPosition.value = 0
            updateCounted()
            updateFilter()
        }
    }

    private fun removeSelectedFilteredPositions(writeOffTask: WriteOffTask) {
        filteredSelectionsHelper.selectedPositions.value?.map { position ->
            filteredGoods.value?.getOrNull(position)?.let { filterItem ->
                writeOffTask.deleteTaskWriteOffReason(filterItem.taskWriteOffReason)
            }.orIfNull {
                Logg.e { "removeSelectedFilteredPositions no item in position $position" }
            }
        }
    }

    fun onResult(code: Int?) {
        when (code) {
            requestCodeDelete -> onConfirmAllDelete()
            requestCodeSelectPersonnelNumber -> saveData()
        }
    }


    fun onClickPrint() {
        processServiceManager.getWriteOffTask()?.let {
            launchUITryCatch {
                screenNavigator.showProgress(printTaskNetRequest)
                printTaskNetRequest(it.getPrinterTask()).either(::handleFailure, ::handleSuccessPrint)
                screenNavigator.hideProgress()
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        if (selectedPage.value == 0) {
            countedGoods.value?.getOrNull(position)?.productInfo
        } else {
            filteredGoods.value?.getOrNull(position)?.productInfo
        }?.let {
            searchProductDelegate.openProductScreen(it, DEFAULT_QUANTITY)
        }
    }

    private fun saveData() {
        launchUITryCatch {
            screenNavigator.showProgress(sendWriteOffDataNetRequest)
            processServiceManager.getWriteOffTask()?.let {
                sendWriteOffDataNetRequest(it.getReport()).either(::handleFailure, ::handleSentSuccess)
            }

            screenNavigator.hideProgress()
        }

    }


    private fun handleSentSuccess(sendWriteOffDataResult: SendWriteOffDataResult) {
        Logg.d { "writeOffReportResponse: $sendWriteOffDataResult" }
        if (sendWriteOffDataResult.retCode.isEmpty() || sendWriteOffDataResult.retCode == "0") {
            processServiceManager.clearTask()
            screenNavigator.openSendingReportsScreen(sendWriteOffDataResult)
        } else {
            analyticsHelper.onRetCodeNotEmpty("$sendWriteOffDataResult")
            screenNavigator.openAlertScreen(sendWriteOffDataResult.errorText)
        }
    }

    private fun onConfirmAllDelete() {
        processServiceManager.getWriteOffTask()?.clearTask()
        updateFilter()
        updateCounted()
    }

    private fun handleSuccessPrint(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        screenNavigator.openSuccessPrintMessage()

    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        eanCode.postValue("")
        return false
    }

    private fun updateCounted() {
        processServiceManager.getWriteOffTask()?.let {
            countedGoods.postValue(
                    it.getProcessedProducts()
                            .mapIndexed { index, productInfo ->
                                GoodItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        quantity = "${it.taskRepository.getTotalCountForProduct(productInfo).toStringFormatted()} ${productInfo.uom.name}",
                                        even = index % 2 == 0,
                                        productInfo = productInfo)
                            }
                            .reversed())
        }

        countedSelectionsHelper.clearPositions()

    }

    private fun updateFilter() {
        processServiceManager.getWriteOffTask()?.let { writeOffTask ->
            val writeOffReasons = writeOffTask.taskRepository.getWriteOffReasons().getWriteOffReasons()

            val reasons = writeOffReasons.map {
                it.writeOffReason.code to it
            }.toMap().values.toList()

            (if (reasons.size == 1) {
                mutableListOf()
            } else {
                mutableListOf(sharedStringResourceManager.notSelected())
            }).let {
                it.addAll(reasons.map { taskWriteOffReason -> taskWriteOffReason.writeOffReason.name })
                categories.postValue(it)
            }

            val selectedPosition = selectedCategoryPosition.value ?: -1
            val selectedCategory = reasons.getOrNull(selectedPosition - 1)

            filteredGoods.postValue(
                    mutableListOf<FilterItem>().apply {
                        writeOffReasons
                                .filter {
                                    selectedCategory == null
                                            || selectedCategory.writeOffReason.code == it.writeOffReason.code
                                }
                                .forEachIndexed { index, taskWriteOffReason ->
                                    writeOffTask.taskRepository.getProducts().findProduct(taskWriteOffReason.materialNumber)?.let {
                                        add(FilterItem(
                                                number = index + 1,
                                                name = "${it.getMaterialLastSix()} ${it.description}",
                                                reason = taskWriteOffReason.writeOffReason.name,
                                                quantity = "${taskWriteOffReason.count.toStringFormatted()} ${it.uom.name}",
                                                even = index % 2 == 0,
                                                taskWriteOffReason = taskWriteOffReason,
                                                productInfo = it
                                        )
                                        )
                                    }
                                }
                    }.reversed()
            )
        }

        filteredSelectionsHelper.clearPositions()
    }

    companion object {
        private const val DEFAULT_QUANTITY = 0.0
    }

}


data class GoodItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean,
        val productInfo: ProductInfo
) : Evenable {
    override fun isEven() = even

}

data class FilterItem(
        val number: Int, val name: String,
        val reason: String,
        val quantity: String,
        val even: Boolean,
        val taskWriteOffReason: TaskWriteOffReason,
        val productInfo: ProductInfo
) : Evenable {
    override fun isEven() = even

}