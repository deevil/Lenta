package com.lenta.bp14.features.list_of_differences

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListOfDifferencesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager


    val selectionsHelper = SelectionItemsHelper()

    val goods = MutableLiveData<List<Good>>()



    val enabledMissingButton: MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        viewModelScope.launch {
            goods.value = taskManager.getTestGoodList(4)
        }
    }



    fun getTitle(): String? {
        return "???"
    }

    fun onClickSkip() {

    }

    fun onClickMissing() {

    }

    fun onClickItemPosition(position: Int) {

    }


}

data class DifferenceVM(
        val number: Int,
        val name: String
)
