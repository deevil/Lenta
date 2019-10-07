package com.lenta.bp14.features.list_of_differences

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.pojo.Good
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListOfDifferencesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val selectionsHelper = SelectionItemsHelper()

    val title = MutableLiveData<String>("РБС-300//Рабочий список")

    val goods = MutableLiveData<List<Good>>()

    val missingButtonEnabled: MutableLiveData<Boolean> = selectionsHelper.selectedPositions.map { it?.isNotEmpty() }

    init {
        viewModelScope.launch {

        }
    }

    fun onClickMissing() {

    }

    fun onClickSkip() {

    }

    fun onClickItemPosition(position: Int) {

    }

}
