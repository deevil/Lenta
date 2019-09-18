package com.lenta.bp14.features.work_list.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.ShelfLifeType
import com.lenta.bp14.models.work_list.Good
import com.lenta.bp14.models.work_list.Provider
import com.lenta.bp14.models.work_list.Stock
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
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

    val good = MutableLiveData<Good>()

    val quantity = MutableLiveData<String>()
    val totalQuantity: MutableLiveData<Int> = quantity.map {
        val currentQuantity = if (it?.isNotEmpty() == true) it.toInt() else 0
        val goodQuantity = if (good.value != null) good.value!!.common.quantity else 0
        currentQuantity + goodQuantity
    }

    val day = MutableLiveData<String>("")
    val month = MutableLiveData<String>("")
    val year = MutableLiveData<String>("")

    val commentsPosition = MutableLiveData(0)
    val shelfLifePosition = MutableLiveData(0)

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
                ShelfLifeType.PRODUCED.position -> enteredDate.time  + shelfLifeDays * 24 * 60 * 60 * 1000
                else -> enteredDate.time
            }

            daysLeft = TimeUnit.DAYS.convert(shelfLifeEnd - Date().time, TimeUnit.MILLISECONDS).toInt()
        }

        daysLeft
    }

    val shelfLifeDaysFieldVisibility = daysLeft.map { it != null }

    val commentsList = MutableLiveData<List<String>>()
    val shelfLifeTypeList = MutableLiveData<List<String>>()

    val stocks = MutableLiveData<List<Stock>>()
    val providers = MutableLiveData<List<Provider>>()

    init {
        viewModelScope.launch {
            good.value = task.currentGood
            stocks.value = task.currentGood?.stocks
            providers.value = task.currentGood?.providers

            quantity.value = task.currentGood?.common?.quantity.toString()
        }
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

}
