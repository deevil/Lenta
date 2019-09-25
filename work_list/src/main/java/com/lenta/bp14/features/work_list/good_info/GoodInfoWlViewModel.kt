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
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getFormattedDate
import com.lenta.shared.utilities.extentions.map
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

    val good by lazy { task.currentGood }

    val title = MutableLiveData<String>("")

    val quantity = MutableLiveData<String>()
    val totalQuantity: MutableLiveData<Int> = quantity.map { quantity ->
        val total = quantity?.toIntOrNull() ?: 0
        good.value?.scanResults?.map { total + it.quantity }
        total
    }

    val commentsPosition = MutableLiveData(0)
    val shelfLifePosition = MutableLiveData(0)

    val day = MutableLiveData<String>("")
    val month = MutableLiveData<String>("")
    val year = MutableLiveData<String>("")

    val shelfLifeStart = MutableLiveData<Date>()
    val shelfLifeEnd = MutableLiveData<Date>()

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

    val daysLeft: MutableLiveData<Int> = enteredDate.combineLatest(shelfLifePosition).map {
        val shelfLifeDays = good.value?.common?.shelfLifeDays
        val enteredDate = it?.first
        val shelfLifeType = it?.second

        var daysLeft: Int? = null
        if (enteredDate != null && shelfLifeDays != null && shelfLifeType != null) {
            val shelfLifeEnd = when (shelfLifeType) {
                ShelfLifeType.PRODUCED.position -> enteredDate.time + shelfLifeDays * 24 * 60 * 60 * 1000
                else -> enteredDate.time
            }

            daysLeft = TimeUnit.DAYS.convert(shelfLifeEnd - Date().time, TimeUnit.MILLISECONDS).toInt()
        }

        daysLeft
    }

    val shelfLifeDaysFieldVisibility = daysLeft.map { it != null }

    val shelfLifeTypeList = MutableLiveData<List<String>>()

    val commentsList: MutableLiveData<List<String>> by lazy { task.comments }
    val comment = MutableLiveData<String>("")

    val stocks: MutableLiveData<List<ItemStockUi>> by lazy {
        task.getGoodStocks().map { list: List<Stock>? ->
            list?.mapIndexed { index, stock ->
                ItemStockUi(
                        number = (index + 1).toString(),
                        storage = stock.storage,
                        quantity = "${stock.quantity} шт."
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

            quantity.value = "1"
            comment.value = commentsList.value?.get(0)

            viewModelScope.launch {
                task.loadAdditionalGoodInfo()
                showProgress.value = false
            }
        }
    }

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
            shelfLifePosition.value = position
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


    }

    private fun saveScanResult() {
        task.currentGood.value?.scanResults?.add(
                ScanResult(
                        quantity = quantity.value?.toInt() ?: 0,
                        comment = comment.value ?: "",
                        shelfLifeStart = shelfLifeStart.value,
                        shelfLifeEnd = shelfLifeEnd.value
                )
        )
    }

}


data class OptionsUi(
        val matrixType: MatrixType,
        val goodType: GoodType,
        val section: String,
        val healthFood: Boolean,
        val novelty: Boolean
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