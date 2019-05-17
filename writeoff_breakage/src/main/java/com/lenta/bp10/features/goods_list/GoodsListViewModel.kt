package com.lenta.bp10.features.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.TaskWriteOffReason
import com.lenta.bp10.models.task.getReport
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.bp10.requests.network.SendWriteOffReportRequest
import com.lenta.bp10.requests.network.WriteOffReportResponse
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.resources.IStringResourceManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager
    @Inject
    lateinit var productInfoDbRequest: ProductInfoDbRequest
    @Inject
    lateinit var stringResourceManager: IStringResourceManager
    @Inject
    lateinit var sendWriteOffReportRequest: SendWriteOffReportRequest

    var selectedPage = MutableLiveData(0)
    val countedGoods: MutableLiveData<List<GoodItem>> = MutableLiveData()
    val filteredGoods: MutableLiveData<List<FilterItem>> = MutableLiveData()
    val categories: MutableLiveData<List<String>> = MutableLiveData()
    val selectedCategoryPosition: MutableLiveData<Int> = MutableLiveData(0)
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val countedSelectionsHelper = SelectionItemsHelper()
    val filteredSelectionsHelper = SelectionItemsHelper()

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


    val onCategoryPositionClickListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedCategoryPosition.value = position
            updateFilter()
        }
    }

    init {
        viewModelScope.launch {
            updateCounted()
            updateFilter()
        }

    }

    fun onResume() {
        updateCounted()
        updateFilter()
    }

    private fun updateCounted() {
        processServiceManager.getWriteOffTask()?.let {
            countedGoods.postValue(
                    it.getProcessedProducts()
                            .mapIndexed { index, productInfo ->
                                GoodItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        quantity = "${it.taskRepository.getTotalCountForProduct(productInfo)} ${productInfo.uom.name}",
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
                mutableListOf(stringResourceManager.notSelected())
            }).let {
                it.addAll(reasons.map { taskWriteOffReason -> taskWriteOffReason.writeOffReason.name })
                categories.postValue(it)
            }

            val selectedCategory = reasons.getOrNull((selectedCategoryPosition.value ?: -1) - 1)

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
                                                quantity = "${taskWriteOffReason.count} ${it.uom.name}",
                                                even = index % 2 == 0,
                                                taskWriteOffReason = taskWriteOffReason,
                                                productInfo = it))
                                    }


                                }
                    }.reversed()
            )
        }

        filteredSelectionsHelper.clearPositions()
    }

    override fun onOkInSoftKeyboard(): Boolean {

        searchCode()

        Logg.d { "processServiceManager taskDescription: ${processServiceManager.getWriteOffTask()?.taskDescription}" }
        return true
    }

    private fun searchCode() {
        viewModelScope.launch {
            eanCode.value?.let {
                productInfoDbRequest(ProductInfoRequestParams(number = it)).either(::handleFailure, ::handleScanSuccess)
            }

        }
    }

    private fun handleScanSuccess(productInfo: ProductInfo) {
        Logg.d { "productInfo: $productInfo" }
        screenNavigator.openGoodInfoScreen(productInfo)
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }


    fun getTitle(): String {
        processServiceManager.getWriteOffTask()?.let {
            return "${it.taskDescription.taskType.code} - ${it.taskDescription.taskName}"
        }
        return ""
    }

    fun onScanResult(data: String) {
        eanCode.value = data
        searchCode()
    }

    fun onClickSave() {
        viewModelScope.launch {
            screenNavigator.showProgress(sendWriteOffReportRequest)
            processServiceManager.getWriteOffTask()?.let {
                sendWriteOffReportRequest(it.getReport()).either(::handleFailure, ::handleSentSuccess)
            }

            screenNavigator.hideProgress()
        }

    }


    private fun handleSentSuccess(writeOffReportResponse: WriteOffReportResponse) {
        Logg.d { "writeOffReportResponse: ${writeOffReportResponse}" }
        if (writeOffReportResponse.retCode.isEmpty() || writeOffReportResponse.retCode == "0") {
            processServiceManager.clearTask()
            screenNavigator.openSendingReportsScreen(writeOffReportResponse)
        } else {
            screenNavigator.openAlertScreen(writeOffReportResponse.errorText)
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
                else -> {
                    with(filteredSelectionsHelper) {
                        if (isSelectedEmpty() && selectedCategoryPosition.value == 0) {
                            screenNavigator.openRemoveTaskConfirmationScreen(writeOffTask.taskDescription.taskName, 0)
                        } else {
                            if (isSelectedEmpty()) {
                                filteredGoods.value?.let {
                                    addAll(it)
                                }
                            }
                            selectedPositions.value?.map { position ->
                                filteredGoods.value!![position].let { filterItem ->
                                    writeOffTask.taskRepository.getWriteOffReasons().deleteWriteOffReason(filterItem.taskWriteOffReason)
                                    if (writeOffTask.getTotalCountOfProduct(filterItem.productInfo) <= 0) {
                                        writeOffTask.deleteProducts(listOf(filterItem.productInfo))
                                    }
                                }
                            }

                        }
                    }


                }


            }
            selectedCategoryPosition.value = 0
            updateCounted()
            updateFilter()

        }

    }

    fun onConfirmAllDelete() {
        processServiceManager.getWriteOffTask()?.clearTask()
        updateFilter()
        updateCounted()
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



