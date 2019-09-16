package com.lenta.bp14.features.not_exposed.good_info

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.TaskManager
import com.lenta.bp14.models.data.pojo.Good
import com.lenta.bp14.models.data.pojo.Stock
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoNeViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager


    val selectedPage = MutableLiveData(0)

    val good: MutableLiveData<Good> = MutableLiveData()

    val stocks = MutableLiveData<List<Stock>>()

    val quantityField = MutableLiveData<String>("0")

    val frameType = MutableLiveData<String>()
    private val frameTypeSelected = frameType.map { !it.isNullOrEmpty() }

    val cancelButtonEnabled = frameTypeSelected.map { it == true }

    val framedButtonEnabled = frameTypeSelected.combineLatest(quantityField).map {
        it?.first == false && it.second.toIntOrNull() ?: 0 == 0
    }
    val notFramedButtonEnabled = frameTypeSelected.combineLatest(quantityField).map {
        it?.first == false && it.second.toIntOrNull() ?: 0 == 0
    }

    init {
        viewModelScope.launch {
            good.value = taskManager.currentGood
            stocks.value = good.value?.stocks

            frameType.value = ""
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickCancel() {
        frameType.value = ""
    }

    fun onClickFramed() {
        frameType.value = "Оформлено"
    }

    fun onClickNotFramed() {
        frameType.value = "Не оформлено"
    }

    fun onClickApply() {

    }

}
