package com.lenta.bp14.features.work_list.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.model.Good
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GoodInfoWlViewModel : CoreViewModel(), PageSelectionListener {

    val selectedPage = MutableLiveData(0)

    val good: MutableLiveData<Good> = MutableLiveData()

    val quantity: MutableLiveData<String> = MutableLiveData()
    val totalQuantity: MutableLiveData<Int> = quantity.map {
        val currentQuantity = if (it?.isNotEmpty() == true) it.toInt() else 0
        val goodQuantity = if(good.value != null) good.value!!.total else 0
        currentQuantity + goodQuantity
    }

    val day: MutableLiveData<String> = MutableLiveData("")
    val month: MutableLiveData<String> = MutableLiveData("")
    val year: MutableLiveData<String> = MutableLiveData("")

    val shelfLifePosition: MutableLiveData<Int> = MutableLiveData(0)

    val shelfLifeDaysLeft: MutableLiveData<Int> = day.combineLatest(month).combineLatest(year).map {
        val format = SimpleDateFormat("dd MM yy", Locale.getDefault())

        val day = it?.first?.first?.toInt()
        val month = it?.first?.second?.toInt()
        val year = it?.second?.toInt()
        var diff = 0

        try {
            val enteredDate = format.parse("$day $month $year")
            diff = TimeUnit.DAYS.convert(Date().time - enteredDate.time, TimeUnit.MILLISECONDS).toInt()

        } catch (e: Exception) {
            Logg.d { "Date parse exception!" }
        }

        diff
    }

    init {
        viewModelScope.launch {
            good.value = Good(
                    id = 0,
                    material = "000000000000254128",
                    name = "Кукуруза",
                    total = 5
            )

            quantity.value = "1"
        }
    }


    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickBtn1() {

    }

    fun onClickBtn2() {

    }

    fun onClickBtn3() {

    }

    fun onClickBtn4() {

    }

    fun onClickBtn5() {

    }

    fun onClickBtn6() {

    }

    fun onClickBtn7() {

    }

}
