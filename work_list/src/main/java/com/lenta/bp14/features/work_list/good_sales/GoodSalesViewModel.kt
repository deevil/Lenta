package com.lenta.bp14.features.work_list.good_sales

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.TaskManager
import com.lenta.bp14.models.data.pojo.Good
import com.lenta.bp14.models.data.pojo.SalesStatistics
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodSalesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager


    val good = MutableLiveData<Good>()
    val salesStatistics = MutableLiveData<SalesStatistics>()

    init {
        viewModelScope.launch {
            good.value = taskManager.currentGood
            salesStatistics.value = good.value?.salesStatistics
        }
    }

    fun onClickUpdate() {

    }

}
