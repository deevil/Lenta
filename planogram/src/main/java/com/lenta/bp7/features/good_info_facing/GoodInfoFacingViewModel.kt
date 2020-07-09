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
        isGoodJustCreated() || (it?.facings ?: 0 > 0 && !isGoodJustCreated())
    }

    val totalFacings: MutableLiveData<Int> = facings.map {
        if (isGoodJustCreated()) {
            val currentFacings = it?.toIntOrNull() ?: 0
            val previousFacings = if (checkData.isFirstCurrentGood()) checkData.getPreviousSameGoodFacings() else 0
            currentFacings + previousFacings
        } else {
            good.value?.facings
        }
    }

    val facingFieldEnabled: MutableLiveData<Boolean> = good.map { isGoodJustCreated() }

    val missingButtonEnabled: MutableLiveData<Boolean> = facings.combineLatest(good).map { pair ->
        val emptyCountField = pair?.first?.toIntOrNull() ?: 0 == 0
        val alreadyExistFacings = if (pair?.second != null) pair.second.facings > 0 else false
        emptyCountField && !alreadyExistFacings && isGoodJustCreated()
    }

    val applyButtonEnabled: MutableLiveData<Boolean> = facings.map {
        val isNotEmpty = (it?.toIntOrNull() ?: 0) > 0
        isNotEmpty && isGoodJustCreated()
    }

    init {
        viewModelScope.launch {
            good.value = checkData.getCurrentGood()
            facings.value = "" + if (isGoodJustCreated()) 1 else good.value?.facings
            selectFacingsField.value = true
        }
    }

    private fun isGoodJustCreated(): Boolean {
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
                    material = good.value?.getFormattedMaterial().orEmpty(),
                    name = good.value?.name.orEmpty(),
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
        checkData.getCurrentGood()?.facings = facings.value!!.toIntOrNull() ?: 0
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
            checkData.getCurrentGood()?.facings = facings.value!!.toIntOrNull() ?: 0
            checkData.setCurrentGoodStatus(GoodStatus.PROCESSED)
            addGoodByEan(data)
        }
    }
}
