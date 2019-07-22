package com.lenta.bp7.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.data.model.GoodStatus
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var checkData: CheckData

    val good: MutableLiveData<Good> = MutableLiveData()

    val missingGood: MutableLiveData<Boolean> = good.map {
        it?.status == GoodStatus.MISSING_WRONG || it?.status == GoodStatus.MISSING_RIGHT
    }

    val missingButtonEnabled: MutableLiveData<Boolean> = good.map { it?.status == GoodStatus.CREATED && checkData.checkEmptyPlaces }
    val applyButtonEnabled: MutableLiveData<Boolean> = good.map { it?.status == GoodStatus.CREATED }

    init {
        viewModelScope.launch {
            good.value = checkData.getCurrentGood()
        }
    }

    fun onClickMissing() {
        // todo ЭКРАН выбор правильности оформления пустого места

        // !Перенести на другой экран
        checkData.getCurrentGood().status = when ((1..2).random()) {
            1 -> GoodStatus.MISSING_RIGHT
            else -> GoodStatus.MISSING_WRONG
        }
        navigator.goBack()
    }

    fun onClickApply() {
        checkData.getCurrentGood().status = GoodStatus.PROCESSED
        navigator.goBack()
    }

    fun onClickBack() {
        if (checkData.getCurrentGood().status == GoodStatus.CREATED) {
            checkData.deleteCurrentGood()
        }
        navigator.goBack()
    }

}
