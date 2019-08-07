package com.lenta.bp7.features.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.data.model.GoodStatus
import com.lenta.bp7.features.other.AddGoodViewModel
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch

class GoodInfoViewModel : AddGoodViewModel() {

    val good: MutableLiveData<Good> = MutableLiveData()

    val missingGood: MutableLiveData<Boolean> = good.map {
        it?.getStatus() == GoodStatus.MISSING_WRONG || it?.getStatus() == GoodStatus.MISSING_RIGHT
    }

    val missingButtonEnabled: MutableLiveData<Boolean> = good.map { it?.getStatus() == GoodStatus.CREATED }
    val applyButtonEnabled: MutableLiveData<Boolean> = good.map { it?.getStatus() == GoodStatus.CREATED }

    init {
        viewModelScope.launch {
            good.value = checkData.getCurrentGood()
        }
    }

    fun onClickMissing() {
        if (checkData.checkEmptyPlaces) {
            // Выбор - Пустое место оформлено правильно? - Назад / Нет / Да
            navigator.showIsEmptyPlaceDecoratedCorrectly(
                    sapCode = good.value?.getFormattedSapCode() ?: "Not found!",
                    name = good.value?.name ?: "Not found!",
                    segmentNumber = checkData.getCurrentSegment()!!.number,
                    shelfNumber = checkData.getCurrentShelf()!!.number,
                    noCallback = {
                        checkData.setCurrentGoodStatus(GoodStatus.MISSING_WRONG)
                        navigator.openGoodListScreen()
                    },
                    yesCallback = {
                        checkData.setCurrentGoodStatus(GoodStatus.MISSING_RIGHT)
                        navigator.openGoodListScreen()
                    })
        } else {
            // Пустое место всегда оформлено правильно
            checkData.setCurrentGoodStatus(GoodStatus.MISSING_RIGHT)
            navigator.openGoodListScreen()
        }
    }

    fun onClickApply() {
        checkData.setCurrentGoodStatus(GoodStatus.PROCESSED)
        navigator.openGoodListScreen()
    }

    fun onClickBack() {
        if (checkData.getCurrentGood()?.getStatus() == GoodStatus.CREATED) {
            checkData.deleteCurrentGood()
        }
        navigator.openGoodListScreen()
    }

    fun onScanResult(data: String) {
        if (applyButtonEnabled.value == true) {
            checkData.setCurrentGoodStatus(GoodStatus.PROCESSED)
            addGoodByBarCode(data)
        }
    }
}
