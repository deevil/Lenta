package com.lenta.bp16.features.task_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.utilities.databinding.PageSelectionListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val selectedPage = MutableLiveData(0)

    val tasks = MutableLiveData<List<String>>(emptyList())

    // -----------------------------

    init {
        viewModelScope.launch {

        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        //selectedPage.value = position
    }

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickRefresh() {
        // Обновить список заданий

    }

}
