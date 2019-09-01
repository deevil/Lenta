package com.lenta.bp14.features.task_list.search_filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchFilterTlViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager


    val marketNumber = MutableLiveData<String>("")

    val goodField = MutableLiveData<String>("")
    val sectionField = MutableLiveData<String>("")
    val goodsGroupField = MutableLiveData<String>("")
    val publicationDateField = MutableLiveData<String>("")


    init {
        viewModelScope.launch {
            marketNumber.value = taskManager.marketNumber
        }
    }

    fun onClickFind() {

    }
}
