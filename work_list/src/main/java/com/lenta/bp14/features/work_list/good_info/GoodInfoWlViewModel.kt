package com.lenta.bp14.features.work_list.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.ShelfLifeType
import com.lenta.bp14.models.work_list.AdditionalGoodInfo
import com.lenta.bp14.models.work_list.ScanResult
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.work_list.AdditionalGoodInfoParams
import com.lenta.bp14.requests.work_list.IAdditionalGoodInfoNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GoodInfoWlViewModel : CoreViewModel(), PageSelectionListener {

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


    val selectedPage = MutableLiveData(0)

    val showProgress = MutableLiveData<Boolean>(true)

    val commentsPosition = MutableLiveData(0)
    val shelfLifeTypePosition = MutableLiveData(0)

    val good by lazy { task.currentGood }

    val title = MutableLiveData<String>("")

    val quantity = MutableLiveData<String>("")
    val totalQuantity: MutableLiveData<String> by lazy {
        good.combineLatest(quantity).map {
            it?.first?.getTotalQuantity().sumWith(it?.second?.toDoubleOrNull() ?: 0.0).dropZeros()
        }
    }

    val quantityFieldEnabled by lazy {
        good.map { good ->
            good?.isNotMarkedGood()
        }
    }

    val day = MutableLiveData<String>("")
    val month = MutableLiveData<String>("")
    val year = MutableLiveData<String>("")

    private val enteredDate = day.combineLatest(month).combineLatest(year).map {
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

    private val daysLeft: MutableLiveData<Int> = enteredDate.combineLatest(shelfLifeTypePosition).map {
        val enteredDate = it?.first
        val shelfLifeType = it?.second
        val shelfLifeTimeMills = good.value!!.getShelfLifeInMills()

        val daysLeft: Int? = if (enteredDate != null && shelfLifeType != null && shelfLifeTimeMills != 0L) {
            val expirationDate = if (shelfLifeType == ShelfLifeType.PRODUCTION.position) {
                enteredDate.time + good.value!!.getShelfLifeInMills()
            } else enteredDate.time

            TimeUnit.DAYS.convert(expirationDate - Date().time, TimeUnit.MILLISECONDS).toInt()
        } else null

        daysLeft
    }

    val remainingDaysFieldVisibility = daysLeft.map { it != null }

    val shelfLifeTypeList = MutableLiveData<List<String>>()

    val commentsList: MutableLiveData<List<String>> by lazy {
        good.map { good ->
            good?.comments
        }
    }

    val common: MutableLiveData<CommonInfoUi> by lazy {
        good.map {
            it?.let { good ->
                CommonInfoUi(
                        shelfLife = "${good.shelfLife} сут.",
                        remainingDays = "${daysLeft.value} сут.",
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

    private val quantityCondition: MutableLiveData<Boolean> = quantity.map {
        it?.toDoubleOrNull() ?: 0.0 != 0.0
    }

    private val commentCondition: MutableLiveData<Boolean> = commentsPosition.map {
        it ?: 0 != 0
    }

    private val dateCondition: MutableLiveData<Boolean> = enteredDate.map {
        it != null
    }

    val applyButtonEnabled = quantityCondition.combineLatest(commentCondition).combineLatest(dateCondition).map {
        val quantity = it?.first?.first ?: false
        val comment = it?.first?.second ?: false
        val date = it?.second ?: false

        quantity || comment || date
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            title.value = good.value?.getFormattedMaterialWithName()
            quantity.value = good.value?.defaultValue.dropZeros()

            viewModelScope.launch {
                additionalGoodInfoNetRequest(AdditionalGoodInfoParams(
                        tkNumber = sessionInfo.market ?: "Not Found!",
                        ean = good.value?.ean,
                        matNr = good.value?.material
                )).also {
                    showProgress.value = false
                }.either(::handleFailure, ::updateAdditionalGoodInfo)
            }
        }
    }

    // -----------------------------

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
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
        saveScanResult()
        task.setCurrentGoodProcessed()
        navigator.openGoodsListWlScreen()
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
                    addMarkNumber(mark)
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

    private fun addMarkNumber(mark: String) {

    }

    private fun saveScanResult() {
        val enteredDate = enteredDate.value
        val shelfLifeType = shelfLifeTypePosition.value

        val productionDate = if (enteredDate != null && shelfLifeType == ShelfLifeType.PRODUCTION.position) {
            enteredDate
        } else null

        val expirationDate = if (enteredDate != null && shelfLifeType == ShelfLifeType.PRODUCTION.position) {
            Date(enteredDate.time + good.value!!.getShelfLifeInMills())
        } else enteredDate

        task.addScanResult(ScanResult(
                quantity = quantity.value?.toDoubleOrNull() ?: 0.0,
                comment = good.value!!.comments[commentsPosition.value ?: 0],
                productionDate = productionDate,
                expirationDate = expirationDate
        ))
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
        val remainingDays: String,
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