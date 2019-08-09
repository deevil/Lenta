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

    val facings: MutableLiveData<String> = MutableLiveData()

    val selectFacingsField: MutableLiveData<Boolean> = MutableLiveData()

    val goodIsPresent: MutableLiveData<Boolean> = good.map {
        goodJustCreated() || (it?.facings ?: 0 > 0 && !goodJustCreated())
    }

    val totalFacings: MutableLiveData<Int> = facings.map {
        if (goodJustCreated()) {
            val currentFacings = if (it?.isNotEmpty() == true) it.toInt() else 0
            val previousFacings = if (checkData.isFirstCurrentGood()) checkData.getPreviousSameGoodFacings() else 0
            currentFacings + previousFacings
        } else {
            good.value?.facings
        }
    }

    val facingFieldEnabled: MutableLiveData<Boolean> = good.map { goodJustCreated() }

    val missingButtonEnabled: MutableLiveData<Boolean> = facings.combineLatest(good).map { pair ->
        val emptyCountField = if (pair?.first?.isNotEmpty() == true) pair.first.toInt() == 0 else true
        val alreadyExistFacings = if (pair?.second != null) pair.second.facings > 0 else false
        emptyCountField && !alreadyExistFacings && goodJustCreated()
    }

    val applyButtonEnabled: MutableLiveData<Boolean> = facings.map {
        val isNotEmpty = if (it?.isNotEmpty() == true) it.toInt() > 0 else false
        isNotEmpty && goodJustCreated()
    }

    init {
        viewModelScope.launch {
            good.value = checkData.getCurrentGood()
            facings.value = "" + if (goodJustCreated()) 1 else checkData.getCurrentGood()?.facings
            selectFacingsField.value = true
        }
    }

    private fun goodJustCreated(): Boolean {
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
                    material = good.value?.getFormattedMaterial() ?: "Not found!",
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
