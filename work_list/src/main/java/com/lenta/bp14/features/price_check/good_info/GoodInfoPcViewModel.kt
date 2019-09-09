package com.lenta.bp14.features.price_check.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoPcViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager

    val good = MutableLiveData<Good>()

    init {
        viewModelScope.launch {
            good.value = taskManager.currentGood
        }
    }
}
