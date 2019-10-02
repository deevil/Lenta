package com.lenta.bp14.features.work_list.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.ShelfLifeType
import com.lenta.bp14.models.work_list.Provider
import com.lenta.bp14.models.work_list.ScanResult
import com.lenta.bp14.models.work_list.Stock
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
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


    val selectedPage = MutableLiveData(0)

    val showProgress = MutableLiveData<Boolean>(true)

    val commentsPosition = MutableLiveData(0)
    val shelfLifeTypePosition = MutableLiveData(0)

    val good by lazy { task.currentGood }

    val title = MutableLiveData<String>("")

    val quantity = MutableLiveData<String>()
    val totalQuantity: MutableLiveData<String> = quantity.map { quantity ->
        var total = quantity?.toDoubleOrNull() ?: 0.0
        for (scanResult in good.value!!.scanResults.value!!) {
            total = total.sumWith(scanResult.quantity)
        }
        "${total.dropZeros()} ${task.currentGood.value!!.getUnits()}"
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

    val daysLeft: MutableLiveData<Int> = enteredDate.combineLatest(shelfLifeTypePosition).map { pair ->
        val enteredDate = pair?.first
        val shelfLifeType = pair?.second
        val daysLeft: Int? = if (enteredDate != null && shelfLifeType != null) {
            val expirationDate = if (shelfLifeType == ShelfLifeType.PRODUCTION.position){
                enteredDate.time + good.value!!.getShelfLifeInMills()
            } else enteredDate.time

            TimeUnit.DAYS.convert(expirationDate - Date().time, TimeUnit.MILLISECONDS).toInt()
        } else null

        daysLeft
    }

    val shelfLifeDaysFieldVisibility = daysLeft.map { it != null }

    val shelfLifeTypeList = MutableLiveData<List<String>>()

    val commentsList: MutableLiveData<List<String>> by lazy { task.currentGood.value!!.comments }
    val comment = MutableLiveData<String>("")

    val additional: MutableLiveData<AdditionalInfoUi> by lazy {
        task.currentGood.value!!.additional.map { additional ->
            AdditionalInfoUi(
                    storagePlaces = additional?.storagePlaces ?: "Not found!",
                    minStock = "${additional?.minStock?.dropZeros()} ${task.currentGood.value!!.getUnits()}",
                    inventory = additional?.movement?.inventory ?: "Not found!",
                    arrival = additional?.movement?.arrival ?: "Not found!",
                    commonPrice = "${additional?.price?.commonPrice?.dropZeros()}р.",
                    discountPrice = "${additional?.price?.discountPrice?.dropZeros()}р.",
                    promoName = additional?.promo?.name ?: "Not found!",
                    promoPeriod = additional?.promo?.period ?: "Not found!"
            )
        }
    }

    val stocks: MutableLiveData<List<ItemStockUi>> by lazy {
        task.getGoodStocks().map { list: List<Stock>? ->
            list?.mapIndexed { index, stock ->
                ItemStockUi(
                        number = (index + 1).toString(),
                        storage = stock.storage,
                        quantity = "${stock.quantity} ${task.currentGood.value!!.getUnits()}"
                )
            }
        }
    }

    val providers: MutableLiveData<List<ItemProviderUi>> by lazy {
        task.getGoodProviders().map { list: List<Provider>? ->
            list?.mapIndexed { index, provider ->
                ItemProviderUi(
                        number = (index + 1).toString(),
                        code = provider.code,
                        name = provider.name,
                        period = "${provider.kipStart.getFormattedDate()} - ${provider.kipEnd.getFormattedDate()}"
                )
            }
        }
    }

    val options: MutableLiveData<OptionsUi> by lazy {
        task.getGoodOptions().map { options ->
            OptionsUi(
                    matrixType = options?.matrixType ?: MatrixType.Unknown,
                    goodType = options?.goodType ?: GoodType.COMMON,
                    section = options?.section ?: "",
                    healthFood = options?.healthFood ?: false,
                    novelty = options?.novelty ?: false
            )
        }
    }

    val applyButtonEnabled = quantity.combineLatest(daysLeft).map { pair ->
        val quantityNotNull = pair?.first?.toIntOrNull() ?: 0 != 0
        val shelfLifeIsEntered = pair?.second != null
        quantityNotNull && shelfLifeIsEntered
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            title.value = good.value?.getFormattedMaterialWithName()

            quantity.value = good.value?.common?.defaultQuantity?.dropZeros()
            comment.value = commentsList.value?.get(0)

            viewModelScope.launch {
                task.loadAdditionalGoodInfo()
                showProgress.value = false
            }
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    val onSelectComment = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            commentsPosition.value = position
            comment.value = commentsList.value?.get(position)
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
        addScanResult()
        task.moveGoodToProcessedList()
        navigator.closeAllScreen()
        navigator.openGoodsListWlScreen()
    }

    private fun addScanResult() {
        val shelfLifeDate = enteredDate.value
        val shelfLifeType = shelfLifeTypePosition.value

        val productionDate = if (shelfLifeDate != null && shelfLifeType == ShelfLifeType.PRODUCTION.position) {
            shelfLifeDate
        } else null

        val expirationDate = if (shelfLifeDate != null && shelfLifeType == ShelfLifeType.PRODUCTION.position) {
            Date(shelfLifeDate.time + good.value!!.getShelfLifeInMills())
        } else shelfLifeDate

        task.addScanResult(ScanResult(
                quantity = quantity.value?.toDoubleOrNull() ?: 0.0,
                comment =  if (comment.value.isNullOrEmpty()) task.currentGood.value?.comments?.value?.get(0) ?: "" else comment.value!!,
                productionDate = productionDate,
                expirationDate = expirationDate
        ))
    }

    private fun resetGoodFields() {
        quantity.value = good.value?.common?.defaultQuantity?.dropZeros()

        shelfLifeTypePosition.value = 0
        comment.value = commentsList.value?.get(0)

        day.value = ""
        month.value = ""
        year.value = ""
    }

    fun onScanResult(data: String) {

    }

}


data class OptionsUi(
        val matrixType: MatrixType,
        val goodType: GoodType,
        val section: String,
        val healthFood: Boolean,
        val novelty: Boolean
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