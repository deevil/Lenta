package com.lenta.bp7.features.good_info_facing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.data.model.GoodStatus
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoFacingViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var checkData: CheckData

    val good: MutableLiveData<Good> = MutableLiveData()

    val facings: MutableLiveData<String> = MutableLiveData("")

    val goodIsPresent: MutableLiveData<Boolean> = good.map {
        it?.status == GoodStatus.CREATED || (it?.totalFacings ?: 0 > 0 && it?.status != GoodStatus.CREATED)
    }

    val totalFacings: MutableLiveData<Int> = facings.combineLatest(good).map { pair ->
        val currentFacings = if (pair?.first?.isNotEmpty() == true) pair.first.toInt() else 0
        val totalFacings = if (pair?.second != null) pair.second.totalFacings else 0
        currentFacings + totalFacings
    }

    val facingFieldEnabled: MutableLiveData<Boolean> = good.map { it?.status == GoodStatus.CREATED }

    val missingButtonEnabled: MutableLiveData<Boolean> = facings.combineLatest(good).map { pair ->
        val emptyField = if (pair?.first?.isNotEmpty() == true) pair.first.toInt() == 0 else true
        val existFacings = if (pair?.second != null) pair.second.totalFacings > 0 else false
        checkData.checkEmptyPlaces && emptyField && !existFacings && currentGoodIsCreated()
    }

    val applyButtonEnabled: MutableLiveData<Boolean> = facings.map {
        val isNotEmpty = if (it?.isNotEmpty() == true) it.toInt() > 0 else false
        isNotEmpty && currentGoodIsCreated()
    }

    init {
        viewModelScope.launch {
            good.value = checkData.getCurrentGood()
        }
    }

    private fun currentGoodIsCreated(): Boolean {
        return checkData.getCurrentGood().status == GoodStatus.CREATED
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
        checkData.getCurrentGood().totalFacings += facings.value!!.toInt()
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
