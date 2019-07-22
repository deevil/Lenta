package com.lenta.bp7.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.data.model.GoodStatus
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var checkData: CheckData

    val good: MutableLiveData<Good> = MutableLiveData()

    init {
        viewModelScope.launch {
            good.value = checkData.getCurrentGood()
        }
    }

    fun onClickMissing() {
        // todo ЭКРАН выбор правильности оформления пустого места

        // !Перенести на другой экран
        checkData.getCurrentGood().status = when ((1..2).random()) {
            1 -> GoodStatus.PRESENT
            else -> GoodStatus.MISSING
        }
        navigator.goBack()
    }

    fun onClickApply() {
        //checkData.getCurrentGood().totalFacings += facings.value!!.toInt()
        navigator.goBack()
    }

    fun onClickBack() {
        checkData.deleteCurrentGood()
        navigator.goBack()
    }

}
