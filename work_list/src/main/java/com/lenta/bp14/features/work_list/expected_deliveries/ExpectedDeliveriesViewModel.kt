package com.lenta.bp14.features.work_list.expected_deliveries

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.data.model.Delivery
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExpectedDeliveriesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager


    val good = MutableLiveData<Good>()

    val deliveries = MutableLiveData<List<Delivery>>()

    init {
        viewModelScope.launch {
            good.value = taskManager.currentGood
            deliveries.value = good.value?.deliveries
        }
    }

    fun onClickUpdate() {
        // Запрашиваем более свежий список поставок для данного товара
        // ...

    }

}
