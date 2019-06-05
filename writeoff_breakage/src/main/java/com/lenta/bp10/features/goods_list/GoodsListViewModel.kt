package com.lenta.bp10.features.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.fmp.resources.dao_ext.isChkOwnpr
import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.TaskWriteOffReason
import com.lenta.bp10.models.task.getPrinterTask
import com.lenta.bp10.models.task.getReport
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.bp10.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.isNormal
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var hyperHive: HyperHive
    val zmpUtz29V001: ZmpUtz29V001Rfc by lazy {
        ZmpUtz29V001Rfc(hyperHive)
    }

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager
    @Inject
    lateinit var productInfoDbRequest: ProductInfoDbRequest
    @Inject
    lateinit var productInfoNetRequest: ProductInfoNetRequest
    @Inject
    lateinit var sharedStringResourceManager: ISharedStringResourceManager
    @Inject
    lateinit var sendWriteOffReportRequest: SendWriteOffReportRequest
    @Inject
    lateinit var printTaskNetRequest: PrintTaskNetRequest
    @Inject
    lateinit var permissionToWriteoffNetRequest: PermissionToWriteoffNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo

    private val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
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

    private val msgGoodsNotForTask: MutableLiveData<String> = MutableLiveData()
    fun setMsgGoodsNotForTask(string: String) {
        this.msgGoodsNotForTask.value = string
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

    override fun onOkInSoftKeyboard(): Boolean {

        searchCodeFromDb()

        Logg.d { "processServiceManager taskDescription: ${processServiceManager.getWriteOffTask()?.taskDescription}" }
        return true
    }

    private fun searchCodeFromDb() {
        viewModelScope.launch {
            eanCode.value?.let {
                productInfoDbRequest(ProductInfoRequestParams(number = it)).either(::handleFailureSearchFromDb, ::handlePermissionToWriteOffSuccess)
            }

        }
    }

    private fun searchCodeFromServer() {
        viewModelScope.launch {
            eanCode.value?.let {
                screenNavigator.showProgress(productInfoNetRequest)
                productInfoNetRequest(ProductInfoRequestParams(number = it)).either(::handleFailureNetRequest, ::handlePermissionToWriteOffSuccess)
                screenNavigator.hideProgress()
            }

        }
    }

    private fun handlePermissionToWriteOffSuccess(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
        viewModelScope.launch {
            if (zmpUtz29V001.isChkOwnpr(processServiceManager.getWriteOffTask()?.taskDescription!!.taskType.code)) {
                screenNavigator.showProgress(permissionToWriteoffNetRequest)
                permissionToWriteoffNetRequest(PermissionToWriteoffPrams(matnr = productInfo.materialNumber, werks = sessionInfo.market!!)).either(::handleFailure, ::handleSearchProductSuccess)
                screenNavigator.hideProgress()
            } else {
                searchProduct()
            }
        }
    }

    private fun handleSearchProductSuccess(permissionToWriteoff: PermissionToWriteoffRestInfo) {
        if (permissionToWriteoff.ownr.isNullOrEmpty()) {
            screenNavigator.openAlertScreen("Не разрешено списание в производство")
        } else searchProduct()
    }

    private fun searchProduct() {

        val goodsForTask: MutableLiveData<Boolean> = MutableLiveData(false)
        processServiceManager.getWriteOffTask()?.taskDescription!!.materialTypes.forEachIndexed { index, taskMatType ->
            if (taskMatType == productInfo.value!!.materialType) goodsForTask.value = true
        }

        if (!goodsForTask.value!!) {
            screenNavigator.openAlertScreen(msgGoodsNotForTask.value!!)
            return
        }

        goodsForTask.value = false
        if (productInfo.value!!.type == ProductType.ExciseAlcohol || productInfo.value!!.type == ProductType.NonExciseAlcohol) {
            processServiceManager.getWriteOffTask()?.taskDescription!!.gisControls.forEach {
                if (it == "A") goodsForTask.value = true
            }
            if (!goodsForTask.value!!) {
                screenNavigator.openAlertScreen(msgGoodsNotForTask.value!!)
                return
            }
        }

        productInfo.value?.matrixType?.let { matrixType ->
            if (!matrixType.isNormal()) {
                screenNavigator.openMatrixAlertScreen(matrixType = matrixType, codeConfirmation = requestCodeAddProduct)
                return
            }
        }

        openGoodInfoScreen()

    }

    private fun openGoodInfoScreen() {
        when (productInfo.value!!.type) {
            ProductType.General -> screenNavigator.openGoodInfoScreen(productInfo.value!!)
            ProductType.ExciseAlcohol -> {
                if (productInfo.value!!.isSet) {
                    screenNavigator.openSetsInfoScreen(productInfo.value!!)
                    return
                } else
                    screenNavigator.openAlertScreen("Поддержка данного типа товара в процессе разработки")
            }
            else -> screenNavigator.openAlertScreen("Поддержка данного типа товара в процессе разработки")
        }
    }

    private fun handleFailureSearchFromDb(failure: Failure) {
        if (failure is Failure.GoodNotFound) {
            searchCodeFromServer()
            return
        }
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleFailureNetRequest(failure: Failure) {
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
        searchCodeFromDb()
    }

    fun onClickSave(){
        if (sessionInfo.personnelNumber.isNullOrEmpty()) {
            screenNavigator.openSelectionPersonnelNumberScreen(codeConfirmation = 1)
        } else {
            onSave()
        }
    }

    fun onSave() {
        viewModelScope.launch {
            screenNavigator.showProgress(sendWriteOffReportRequest)
            processServiceManager.getWriteOffTask()?.let {
                sendWriteOffReportRequest(it.getReport()).either(::handleFailureSearchFromDb, ::handleSentSuccess)
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
                            screenNavigator
                                    .openRemoveLinesConfirmationScreen(
                                            taskDescription = writeOffTask.taskDescription.taskName,
                                            count = filteredGoods.value?.size ?: 0,
                                            codeConfirmation = requestCodeDelete)
                        } else {
                            if (isSelectedEmpty()) {
                                filteredGoods.value?.let {
                                    addAll(it)
                                }
                            }
                            selectedPositions.value?.map { position ->
                                filteredGoods.value!![position].let { filterItem ->
                                    writeOffTask.deleteTaskWriteOffReason(filterItem.taskWriteOffReason)
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

    fun onResult(code: Int?) {
        when (code) {
            requestCodeDelete -> onConfirmAllDelete()
            requestCodeAddProduct -> openGoodInfoScreen()
        }
    }

    private fun onConfirmAllDelete() {
        processServiceManager.getWriteOffTask()?.clearTask()
        updateFilter()
        updateCounted()
    }

    fun onClickPrint() {
        processServiceManager.getWriteOffTask()?.let {
            viewModelScope.launch {
                screenNavigator.showProgress(printTaskNetRequest)
                printTaskNetRequest(it.getPrinterTask()).either(::handleFailureNetRequest, ::handleSuccessPrint)
                screenNavigator.hideProgress()
            }
        }

    }

    private fun handleSuccessPrint(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        screenNavigator.openSuccessPrintMessage()

    }

    fun onDoubleClickPosition(position: Int) {
        if (selectedPage.value == 0) {
            countedGoods.value?.getOrNull(position)?.productInfo
        } else {
            filteredGoods.value?.getOrNull(position)?.productInfo
        }?.let {
            screenNavigator.openGoodInfoScreen(it)
        }
    }

    companion object {
        const val requestCodeDelete = 100
        const val requestCodeAddProduct = 101

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



