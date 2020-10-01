package com.lenta.bp14.features.work_list.good_info

import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.lenta.bp14.features.work_list.base.BaseGoodViewModel
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.ScanInfoMode
import com.lenta.bp14.models.data.ShelfLifeType
import com.lenta.bp14.models.ui.*
import com.lenta.bp14.models.work_list.AdditionalGoodInfo
import com.lenta.bp14.models.work_list.ScanResult
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.platform.resource.IResourceFormatter
import com.lenta.bp14.platform.resource.IResourceManager
import com.lenta.bp14.requests.pojo.MarkStatus
import com.lenta.bp14.requests.work_list.AdditionalGoodInfoParams
import com.lenta.bp14.requests.work_list.CheckMarkParams
import com.lenta.bp14.requests.work_list.IAdditionalGoodInfoNetRequest
import com.lenta.bp14.requests.work_list.ICheckMarkNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GoodInfoWlViewModel : BaseGoodViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

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

    private val totalQuantityValue: LiveData<Double> by lazy {
        good.switchMap { goodValue ->
            quantity.switchMap { quantityValue ->
                liveData {
                    val totalQuantity = goodValue.getTotalQuantity()
                    val currentQuantity = quantityValue.toDoubleOrNull() ?: 0.0
                    val result = totalQuantity.sumWith(currentQuantity)
                    emit(result)
                }
            }
        }
    }

    val totalQuantity: MutableLiveData<String> by lazy {
        totalQuantityValue.mapSkipNulls {
            it.dropZeros()
        }
    }

    val totalMarks: MutableLiveData<String> by lazy {
        good.mapSkipNulls { good ->
            good.marks.size.toString()
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
                        promoPeriod = additional.promoPeriod,
                        hasZParts = additional.hasZPart
                )
            }
        }
    }

    val stocks: MutableLiveData<List<ItemStockUi>> by lazy {
        good.map { good ->
            good?.additional?.stocks?.mapIndexed { index, stock ->
                val zPartQuantity = if (stock.hasZPart) {
                    "${stock.zPartsQuantity.dropZeros()} ${good.units.name}"
                } else {
                    ""
                }
                ItemStockUi(
                        number = (index + 1).toString(),
                        storage = stock.storage,
                        quantity = "${stock.quantity.dropZeros()} ${good.units.name}",
                        zPartsQuantity = zPartQuantity
                )
            }
        }
    }

    val zParts: LiveData<List<ZPartUi>> by unsafeLazy {
        good.mapSkipNulls { good ->
            good?.additional?.zParts.mapToZPartUiList(good.units.name)
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
        good.mapSkipNulls { good ->
            good.options.run {
                OptionsUi(
                        matrixType = matrixType,
                        goodType = goodType,
                        section = section,
                        healthFood = healthFood,
                        novelty = novelty
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
        launchUITryCatch {
            title.value = good.value?.getFormattedMaterialWithName()
            quantity.value = good.value?.defaultValue.dropZeros()

            loadAdditionalInfo()
        }
    }

    // -----------------------------

    private fun loadAdditionalInfo() {
        launchUITryCatch {
            additionalGoodInfoNetRequest(AdditionalGoodInfoParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    ean = good.value?.ean,
                    matNr = good.value?.material
            )).also {
                loadingIndicatorVisibility.value = false
            }.either(::handleAdditionalInfoFailure, ::updateAdditionalGoodInfo)
        }
    }

    private fun handleAdditionalInfoFailure(failure: Failure) {
        super.handleFailure(failure)
        dataLoadingError.value = if (failure is Failure.SapError) failure.message else resourceManager.serverConnectionError
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
        actionByNumber(
                number = data,
                funcForEan = { ean, _ -> searchCode(ean = ean) },
                funcForMaterial = { material -> searchCode(material = material) },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForPriceQrCode = { qrCode ->
                    priceInfoParser.getPriceInfoFromRawCode(qrCode)?.let {
                        searchCode(ean = it.eanCode)
                    }
                },
                funcForExcise = ::actionForExciseGood,
                funcForShoes = ::actionForMarkedGood,
                funcForCigarettes = ::actionForMarkedGood,
                funcForCigaretteBox = ::actionForMarkedGood,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun searchCode(ean: String? = null, material: String? = null) {
        launchUITryCatch {
            require((ean != null) xor (material != null)) {
                "Only one param allowed - ean: $ean, material: $material"
            }

            navigator.showProgressLoadingData(::handleFailure)

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
                return@launchUITryCatch
            }

            navigator.showGoodNotFound()
        }
    }

    private fun actionForMarkedGood(markNumber: String) {
        good.value?.let { good ->
            if (good.options.goodType != GoodType.MARKED) {
                navigator.showWrongBarcodeFormat()
                return
            }

            if (task.isMarkAlreadyAdded(markNumber)) {
                navigator.showScannedMarkAlreadyAddedToList {
                    task.deleteMark(markNumber)
                }
                return
            }

            launchUITryCatch {
                navigator.showProgressLoadingData(::handleFailure)

                checkMarkNetRequest(CheckMarkParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        material = good.material,
                        markNumber = markNumber,
                        mode = ScanInfoMode.MARKED_GOOD.mode
                )).also {
                    navigator.hideProgress()
                }.either(::handleFailure) {
                    addMarkToList(markNumber, it.markStatus[0])
                }
            }
        }
    }

    private fun actionForExciseGood(exciseMark: String) {
        if (good.value?.options?.goodType != GoodType.EXCISE) {
            navigator.showWrongBarcodeFormat()
            return
        }

        if (task.isMarkAlreadyAdded(exciseMark)) {
            navigator.showScannedMarkAlreadyAddedToList {
                task.deleteMark(exciseMark)
            }
            return
        }

        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            checkMarkNetRequest(CheckMarkParams(
                    tkNumber = sessionInfo.market.orEmpty(),
                    material = good.value?.material.orEmpty(),
                    markNumber = exciseMark,
                    mode = ScanInfoMode.EXCISE_ALCOHOL.mode
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) {
                addMarkToList(exciseMark, it.markStatus[0])
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
        val ean = good.value?.ean
        val enteredDate = enteredDate.value
        val shelfLifeType = shelfLifeTypePosition.value

        val productionDate = enteredDate?.let {
            if (shelfLifeType == ShelfLifeType.PRODUCTION.position) it else null
        }

        val expirationDate = enteredDate?.let {
            if (shelfLifeType == ShelfLifeType.EXPIRATION.position) it else null
        }

        val comment = good.value?.comments?.get(commentsPosition.value ?: 0)

        task.addScanResult(ScanResult(
                quantity = quantity.value?.toDoubleOrNull() ?: 0.0,
                commentCode = comment?.code,
                comment = comment?.description.orEmpty(),
                productionDate = productionDate,
                expirationDate = expirationDate,
                ean = ean
        ))
    }

    private fun isIncorrectEnteredDate(): Boolean {
        return enteredDate.value != null && shelfLifeTypePosition.value == ShelfLifeType.PRODUCTION.position
                && enteredDate.value!!.after(Date())
    }

    override fun onOkInSoftKeyboard(): Boolean {
        Logg.d { "--> Last focus: ${lastFocusField.value}" }

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