package com.lenta.bp14.features.list_of_differences

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.data.pojo.Good
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListOfDifferencesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager

    private val task by lazy {
        generalTaskManager.getProcessedTask()!!
    }


    val selectionsHelper = SelectionItemsHelper()

    val title by lazy {
        "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    val goods = MutableLiveData<List<Good>>()

    val missingButtonEnabled: MutableLiveData<Boolean> = selectionsHelper.selectedPositions.map { it?.isNotEmpty() }



    fun onClickMissing() {

    }

    fun onClickSkip() {

    }

    fun onClickItemPosition(position: Int) {

    }

}
