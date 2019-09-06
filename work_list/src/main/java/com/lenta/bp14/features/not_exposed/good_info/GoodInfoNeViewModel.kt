package com.lenta.bp14.features.not_exposed.good_info

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.data.model.Stock
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.databinding.PageSelectionListener
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

    init {
        viewModelScope.launch {
            good.value = taskManager.currentGood
            stocks.value = good.value?.stocks
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

}
