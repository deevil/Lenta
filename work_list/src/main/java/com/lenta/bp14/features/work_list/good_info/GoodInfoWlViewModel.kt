package com.lenta.bp14.features.work_list.good_info

import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.ShelfLifeType
import com.lenta.bp14.models.work_list.AdditionalGoodInfo
import com.lenta.bp14.models.work_list.ScanResult
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.platform.resource.IResourceManager
import com.lenta.bp14.requests.pojo.MarkStatus
import com.lenta.bp14.requests.work_list.AdditionalGoodInfoParams
import com.lenta.bp14.requests.work_list.CheckMarkParams
import com.lenta.bp14.requests.work_list.IAdditionalGoodInfoNetRequest
import com.lenta.bp14.requests.work_list.ICheckMarkNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GoodInfoWlViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: WorkListTask
    @Inject
    lateinit var additionalGoodInfoNetRequest: IAdditionalGoodInfoNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var priceInfoParser: IPriceInfoParser
    @Inject
    lateinit var checkMarkNetRequest: ICheckMarkNetRequest
    @Inject
    lateinit var resourceManager: IResourceManager


    val selectedPage = MutableLiveData(0)

    val loadingIndicatorVisibility = MutableLiveData<Boolean>(true)

    val dataLoadingErrorVisibility = MutableLiveData<Boolean>(false)

    val dataLoadingError = MutableLiveData<String>("")

    val dataVisibility = loadingIndicatorVisibility.combineLatest(dataLoadingErrorVisibility).map {
        it?.first == false && it.second == false
    }

    val commentsPosition = MutableLiveData(0)
    val shelfLifeTypePosition = MutableLiveData(0)

    val good by lazy { task.currentGood }

    val title = MutableLiveData<String>("")

    val quantity = MutableLiveData<String>("")

    private val totalQuantityValue: MutableLiveData<Double> by lazy {
        good.combineLatest(quantity).map {
            it?.first?.getTotalQuantity().sumWith(it?.second?.toDoubleOrNull() ?: 0.0)
        }
    }

    val totalQuantity: MutableLiveData<String> by lazy {
        totalQuantityValue.map {
            it?.dropZeros()
        }
    }

    val totalMarks: MutableLiveData<String> by lazy {
        good.map { good ->
            good?.marks?.size.toString()
        }
    }

    val day = MutableLiveData<String>("")
    val month = MutableLiveData<String>("")
    val year = MutableLiveData<String>("")

    var dateFields: List<EditText> = emptyList()
    val lastFocusField = MutableLiveData<EditText?>(null)

    private val enteredDate: MutableLiveData<Date> = day.combineLatest(month).combineLatest(year).map {
        val day = it?.first?.first?.toIntOrNull()
        val month = it?.first?.second?.toIntOrNull()
        val year = it?.second?.toIntOrNull()

        var parseDate: Date? = null
        if (day != null && month != null && year != null) {
            try {
                parseDate = SimpleDateFormat("dd MM yy", Locale.getDefault()).parse("$day $month $year")
            } catch (e: Exception) {
                Logg.d { "Date parse exception!" }
            }
        }

        parseDate
    }

    val shelfLifeTypeList: MutableLiveData<List<String>> by lazy {
        good.map { good ->
            good?.shelfLifeTypes?.map { it.description }
        }
    }

    val commentsList: MutableLiveData<List<String>> by lazy {
        good.map { good ->
            good?.comments?.map { it.description }
        }
    }

    val common: MutableLiveData<CommonInfoUi> by lazy {
        good.map {
            it?.let { good ->
                CommonInfoUi(
                        shelfLife = "${good.shelfLife} сут.",
                        remainingShelfLife = "${good.remainingShelfLife} сут.",
                        eanWithUnits = good.getEanWithUnits(),
                        groups = good.getGroups()
                )
            }
        }
    }

    val additional: MutableLiveData<AdditionalInfoUi> by lazy {
        good.map { good ->
            good?.additional?.let { additional ->
                AdditionalInfoUi(
                        storagePlaces = additional.storagePlaces,
                        minStock = additional.minStock,
                        inventory = additional.inventory,
                        arrival = additional.arrival,
                        commonPrice = "${additional.commonPrice.dropZeros()}р.",
                        discountPrice = "${additional.discountPrice.dropZeros()}р.",
                        promoName = additional.promoName,
                        promoPeriod = additional.promoPeriod
                )
            }
        }
    }

    val stocks: MutableLiveData<List<ItemStockUi>> by lazy {
        good.map { good ->
            good?.additional?.stocks?.mapIndexed { index, stock ->
                ItemStockUi(
                        number = (index + 1).toString(),
                        storage = stock.storage,
                        quantity = "${stock.quantity.dropZeros()} ${good.units.name}"
                )
            }
        }
    }

    val providers: MutableLiveData<List<ItemProviderUi>> by lazy {
        good.map { good ->
            good?.additional?.providers?.mapIndexed { index, provider ->
                ItemProviderUi(
                        number = (index + 1).toString(),
                        code = provider.code,
                        name = provider.name,
                        period = provider.period
                )
            }
        }
    }

    val options: MutableLiveData<OptionsUi> by lazy {
        good.map { good ->
            good?.options?.let { options ->
                OptionsUi(
                        matrixType = options.matrixType,
                        goodType = options.goodType,
                        section = options.section,
                        healthFood = options.healthFood,
                        novelty = options.novelty
                )
            }
        }
    }

    private val quantityCondition: MutableLiveData<Boolean> by lazy {
        quantity.map {
            it?.toDoubleOrNull() ?: 0.0 != 0.0
        }
    }

    private val commentCondition: MutableLiveData<Boolean> by lazy {
        commentsPosition.map {
            it ?: 0 != 0
        }
    }

    private val dateCondition: MutableLiveData<Boolean> by lazy {
        enteredDate.map {
            it != null
        }
    }

    private val markCondition: MutableLiveData<Boolean> by lazy {
        good.map { good ->
            good?.marks?.size ?: 0 > 0
        }
    }

    val applyButtonEnabled by lazy {
        quantityCondition.combineLatest(commentCondition).combineLatest(dateCondition)
                .combineLatest(markCondition).map {
                    val quantity = it?.first?.first?.first ?: false
                    val comment = it?.first?.first?.second ?: false
                    val date = it?.first?.second ?: false
                    val mark = it?.second ?: false

                    quantity || comment || date || mark
                }
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            title.value = good.value?.getFormattedMaterialWithName()
            quantity.value = good.value?.defaultValue.dropZeros()

            loadAdditionalInfo()
        }
    }

    // -----------------------------

    private fun loadAdditionalInfo() {
        viewModelScope.launch {
            additionalGoodInfoNetRequest(AdditionalGoodInfoParams(
                    tkNumber = sessionInfo.market ?: "Not Found!",
                    ean = good.value?.ean,
                    matNr = good.value?.material
            )).also {
                loadingIndicatorVisibility.value = false
            }.either(::handleAdditionalInfoFailure, ::updateAdditionalGoodInfo)
        }
    }

    private fun handleAdditionalInfoFailure(failure: Failure) {
        super.handleFailure(failure)
        dataLoadingError.value = if (failure is Failure.SapError) failure.message else resourceManager.serverConnectionError()
        dataLoadingErrorVisibility.value = true
    }

    private fun updateAdditionalGoodInfo(result: AdditionalGoodInfo) {
        Logg.d { "AdditionalGoodInfo: $result" }
        task.updateAdditionalGoodInfo(result)
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    val onSelectComment = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            commentsPosition.value = position
        }
    }

    val onSelectShelfLifeType = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            shelfLifeTypePosition.value = position
        }
    }

    fun openGoodDetails() {
        navigator.openGoodDetailsScreen()
    }

    fun openGoodDeliveries() {
        navigator.openExpectedDeliveriesScreen()
    }

    fun openGoodSales() {
        navigator.openGoodSalesScreen()
    }

    fun onClickApply() {
        Logg.d { "--> Max task positions: ${task.getMaxTaskPositions()}" }
        if (task.isReachLimitPositions()) {
            navigator.showMaxCountProductAlert()
            return
        }

        if (isIncorrectEnteredDate()) {
            navigator.showIncorrectProductionDate {
                day.value = ""
                month.value = ""
                year.value = ""
            }
            return
        }

        saveScanResult()

        task.setCurrentGoodProcessed()
        navigator.goBack()
    }

    fun onScanResult(data: String) {
        analyseCode(
                code = data,
                funcForEan = { ean ->
                    searchCode(ean = ean)
                },
                funcForMatNr = { material ->
                    searchCode(material = material)
                },
                funcForPriceQrCode = { qrCode ->
                    priceInfoParser.getPriceInfoFromRawCode(qrCode)?.let {
                        searchCode(ean = it.eanCode)
                        return@analyseCode
                    }
                },
                funcForMarkCode = { mark ->
                    searchGoodMark(mark)
                },
                funcForExciseCode = { excise ->
                    searchExciseMark(excise)
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun searchCode(ean: String? = null, material: String? = null) {
        viewModelScope.launch {
            require((ean != null) xor (material != null)) {
                "Only one param allowed - ean: $ean, material: $material"
            }

            navigator.showProgressLoadingData()

            when {
                !ean.isNullOrBlank() -> task.getGoodByEan(ean)
                !material.isNullOrBlank() -> task.getGoodByMaterial(material)
                else -> null
            }.also {
                navigator.hideProgress()
            }?.let { good ->
                if (applyButtonEnabled.value == true) {
                    saveScanResult()
                    task.setCurrentGoodProcessed()
                }
                task.addGoodToList(good)
                navigator.openGoodInfoWlScreen(popLast = true)
                return@launch
            }

            navigator.showGoodNotFound()
        }
    }

    private fun searchGoodMark(goodMark: String) {
        if (good.value!!.options.goodType != GoodType.MARKED) {
            navigator.showWrongBarcodeFormat()
            return
        }

        if (task.isMarkAlreadyAdded(goodMark)) {
            navigator.showScannedMarkAlreadyAddedToList {
                task.deleteMark(goodMark)
            }
        } else {
            viewModelScope.launch {
                navigator.showProgressLoadingData()

                checkMarkNetRequest(CheckMarkParams(
                        tkNumber = sessionInfo.market ?: "Not Found!",
                        material = good.value!!.material,
                        markNumber = goodMark,
                        mode = "2"
                )).also {
                    navigator.hideProgress()
                }.either(::handleFailure) {
                    addMarkToList(goodMark, it.markStatus[0])
                }
            }
        }
    }

    private fun searchExciseMark(exciseMark: String) {
        if (good.value!!.options.goodType != GoodType.EXCISE) {
            navigator.showWrongBarcodeFormat()
            return
        }

        if (task.isMarkAlreadyAdded(exciseMark)) {
            navigator.showScannedMarkAlreadyAddedToList {
                task.deleteMark(exciseMark)
            }
        } else {
            viewModelScope.launch {
                navigator.showProgressLoadingData()

                checkMarkNetRequest(CheckMarkParams(
                        tkNumber = sessionInfo.market ?: "Not Found!",
                        material = good.value!!.material,
                        markNumber = exciseMark,
                        mode = "1"
                )).also {
                    navigator.hideProgress()
                }.either(::handleFailure) {
                    addMarkToList(exciseMark, it.markStatus[0])
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    private fun addMarkToList(markNumber: String, markStatus: MarkStatus) {
        when (markStatus.status) {
            "1" -> task.addMark(markNumber)
            "2" -> {
                navigator.openAddMarkToList(
                        message = markStatus.description,
                        nextCallback = { task.addMark(markNumber) })
            }
            "3" -> navigator.openInfoScreen(markStatus.description)
        }
    }

    private fun saveScanResult() {
        val enteredDate = enteredDate.value
        val shelfLifeType = shelfLifeTypePosition.value

        val productionDate = enteredDate?.let {
            if (shelfLifeType == ShelfLifeType.PRODUCTION.position) it else null
        }

        val expirationDate = enteredDate?.let {
            if (shelfLifeType == ShelfLifeType.EXPIRATION.position) it else null
        }

        val comment = good.value!!.comments[commentsPosition.value ?: 0]

        task.addScanResult(ScanResult(
                quantity = quantity.value?.toDoubleOrNull() ?: 0.0,
                commentCode = comment.code,
                comment = comment.description,
                productionDate = productionDate,
                expirationDate = expirationDate
        ))
    }

    private fun isIncorrectEnteredDate(): Boolean {
        return enteredDate.value != null && shelfLifeTypePosition.value == ShelfLifeType.PRODUCTION.position
                && enteredDate.value!!.after(Date())
    }

    override fun onOkInSoftKeyboard(): Boolean {
        Logg.d { "--> Last focus: ${lastFocusField.value?.id}" }

        val day = dateFields[0]
        val month = dateFields[1]
        val year = dateFields[2]

        if (lastFocusField.value == day) {
            month.requestFocus()
            return true
        } else if (lastFocusField.value == month) {
            year.requestFocus()
            return true
        }

        return false
    }

}


data class OptionsUi(
        val matrixType: MatrixType,
        val goodType: GoodType,
        val section: String,
        val healthFood: Boolean,
        val novelty: Boolean
)

data class CommonInfoUi(
        val shelfLife: String,
        val remainingShelfLife: String,
        val eanWithUnits: String,
        val groups: String
)

data class AdditionalInfoUi(
        val storagePlaces: String,
        val minStock: String,
        val inventory: String,
        val arrival: String,
        val commonPrice: String,
        val discountPrice: String,
        val promoName: String,
        val promoPeriod: String
)

data class ItemProviderUi(
        val number: String,
        val code: String,
        val name: String,
        val period: String
)

data class ItemStockUi(
        val number: String,
        val storage: String,
        val quantity: String
)