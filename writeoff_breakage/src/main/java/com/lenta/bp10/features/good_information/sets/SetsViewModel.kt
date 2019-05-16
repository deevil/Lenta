package com.lenta.bp10.features.good_information.sets

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class SetsViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val writeOffReasonTitles: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData()
    val suffix: MutableLiveData<String> = MutableLiveData()
    val totalCount: MutableLiveData<String> = MutableLiveData()

    fun onClickClean() {
        screenNavigator.openAlertScreen("onClickClean")
    }

    fun onClickDetails() {
        screenNavigator.openAlertScreen("onClickDetails")
    }

    fun onClickAdd() {
        screenNavigator.openAlertScreen("onClickAdd")
    }

    fun onClickApply() {
        screenNavigator.openAlertScreen("onClickApply")
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

}
