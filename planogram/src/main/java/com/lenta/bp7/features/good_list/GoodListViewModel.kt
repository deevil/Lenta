package com.lenta.bp7.features.good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.data.model.ShelfStatus
import com.lenta.bp7.features.other.AddGoodViewModel
import com.lenta.shared.platform.constants.Constants.COMMON_SAP_LENGTH
import com.lenta.shared.platform.constants.Constants.SAP_OR_BAR_LENGTH
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch

class GoodListViewModel : AddGoodViewModel(), OnOkInSoftKeyboardListener {

    val goods: MutableLiveData<List<Good>> = MutableLiveData()

    val segmentNumber: MutableLiveData<String> = MutableLiveData()
    val shelfNumber: MutableLiveData<String> = MutableLiveData()
    val goodNumber: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToGoodNumber: MutableLiveData<Boolean> = MutableLiveData(true)

    val numberFieldEnabled: MutableLiveData<Boolean> = MutableLiveData(false)

    val applyButtonEnabled: MutableLiveData<Boolean> = goods.map {
        it?.isNotEmpty() ?: false && checkData.getCurrentShelf()?.getStatus() == ShelfStatus.UNFINISHED
    }

    init {
        viewModelScope.launch {
            checkData.let {
                segmentNumber.value = it.getCurrentSegment()?.number
                shelfNumber.value = it.getCurrentShelf()?.number
                goods.value = it.getCurrentShelf()?.goods
                numberFieldEnabled.value = it.getCurrentShelf()?.getStatus() == ShelfStatus.UNFINISHED
            }
        }
    }

    fun updateGoodList() {
        goods.value = checkData.getCurrentShelf()?.goods
        goodNumber.value = ""
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(goodNumber.value ?: "")
        return true
    }

    private fun checkEnteredNumber(number: String) {
        number.length.let { length ->
            if (length < COMMON_SAP_LENGTH) {
                // Сообщение - Данный товар не найден в справочнике
                navigator.showGoodNotFound()
                return
            }

            if (length >= COMMON_SAP_LENGTH) {
                when (length) {
                    COMMON_SAP_LENGTH -> addGoodByMaterial(number)
                    SAP_OR_BAR_LENGTH -> {
                        // Выбор - Введено 12 знаков. Какой код вы ввели? - SAP-код / Штрихкод
                        navigator.showTwelveCharactersEntered(
                                sapCallback = { addGoodByMatcode(number) },
                                barCallback = { addGoodByEan(number) })
                    }
                    else -> addGoodByEan(number)
                }
            }
        }
    }

    fun onClickApply() {
        // Подтверждение - Сохранить результаты сканирования полки и закрыть ее для редактирования - Назад / Да
        navigator.showSaveShelfScanResults(
                segmentNumber = segmentNumber.value!!,
                shelfNumber = shelfNumber.value!!) {
            checkData.setCurrentShelfStatus(ShelfStatus.PROCESSED)
            navigator.openShelfListScreen()
        }
    }

    fun onClickBack() {
        if (checkData.getCurrentShelf()?.getStatus() != ShelfStatus.UNFINISHED) {
            navigator.openShelfListScreen()
            return
        }

        // Подтверждение - Данные полки не будут сохранены - Назад / Подтвердить
        navigator.showShelfDataWillNotBeSaved(
                segmentNumber = segmentNumber.value!!,
                shelfNumber = shelfNumber.value!!) {
            if (goods.value?.isEmpty() == true) {
                checkData.deleteCurrentShelf()
                navigator.openShelfListScreen()
            } else {
                checkData.setCurrentShelfStatus(ShelfStatus.DELETED)
                navigator.openShelfListScreen()
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        if (checkData.getCurrentShelf()?.getStatus() == ShelfStatus.UNFINISHED) {
            checkData.currentGoodIndex = position
            openGoodInfoScreen()
        }
    }

    fun onScanResult(data: String) {
        if (numberFieldEnabled.value == true) {
            addGoodByEan(data)
        }
    }
}
