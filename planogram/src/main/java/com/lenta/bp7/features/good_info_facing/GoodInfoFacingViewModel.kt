package com.lenta.bp7.features.good_info_facing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.data.model.GoodStatus
import com.lenta.bp7.features.other.AddGoodViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch

class GoodInfoFacingViewModel : AddGoodViewModel(), OnOkInSoftKeyboardListener {

    val good: MutableLiveData<Good> = MutableLiveData()

    val facings: MutableLiveData<String> = MutableLiveData("")

    val goodIsPresent: MutableLiveData<Boolean> = good.map {
        it?.getStatus() == GoodStatus.CREATED || (it?.facings ?: 0 > 0 && it?.getStatus() != GoodStatus.CREATED)
    }

    val totalFacings: MutableLiveData<Int> = facings.map {
        val currentFacings = if (it?.isNotEmpty() == true) it.toInt() else 0
        val previousFacings = if (checkData.isFirstCurrentGood()) checkData.getPreviousSameGoodFacings() else 0
        currentFacings + previousFacings
    }

    val facingFieldEnabled: MutableLiveData<Boolean> = good.map { it?.getStatus() == GoodStatus.CREATED }

    val missingButtonEnabled: MutableLiveData<Boolean> = facings.combineLatest(good).map { pair ->
        val emptyCountField = if (pair?.first?.isNotEmpty() == true) pair.first.toInt() == 0 else true
        val alreadyExistFacings = if (pair?.second != null) pair.second.facings > 0 else false
        emptyCountField && !alreadyExistFacings && currentGoodIsCreated()
    }

    val applyButtonEnabled: MutableLiveData<Boolean> = facings.map {
        val isNotEmpty = if (it?.isNotEmpty() == true) it.toInt() > 0 else false
        isNotEmpty && currentGoodIsCreated()
    }

    init {
        viewModelScope.launch {
            good.value = checkData.getCurrentGood()

            val goodFacings = checkData.getCurrentGood()?.facings
            facings.value = "" + if (goodFacings != 0) goodFacings else 1
        }
    }

    private fun currentGoodIsCreated(): Boolean {
        return checkData.getCurrentGood()?.getStatus() == GoodStatus.CREATED
    }

    override fun onOkInSoftKeyboard(): Boolean {
        onClickApply()
        return true
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
        checkData.getCurrentGood()?.facings = facings.value!!.toInt()
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
            checkData.getCurrentGood()?.facings = facings.value!!.toInt()
            checkData.setCurrentGoodStatus(GoodStatus.PROCESSED)
            addGoodByEan(data)
        }
    }
}
