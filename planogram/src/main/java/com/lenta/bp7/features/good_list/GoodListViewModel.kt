package com.lenta.bp7.features.good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.SapCodeType
import com.lenta.bp7.data.model.*
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData

    companion object {
        const val SAP_LENGTH = 6
        const val SAP_OR_BAR_LENGTH = 12
    }

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
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber()
        return true
    }

    private fun checkEnteredNumber() {
        goodNumber.value.let { number ->
            if (number?.isNotEmpty() == true && number.length >= SAP_LENGTH) {
                when (number.length) {
                    SAP_LENGTH -> addGoodBySapCode()
                    SAP_OR_BAR_LENGTH -> {
                        // Выбор - Введено 12 знаков. Какой код вы ввели? - SAP-код / Штрихкод
                        navigator.showTwelveCharactersEntered(
                                sapCallback = ::addGoodBySapCode,
                                barCallback = ::addGoodByBarCode)
                    }
                    else -> addGoodByBarCode()
                }
            }
        }
    }

    private fun addGoodBySapCode() {
        goodNumber.value.let { number ->
            Logg.d { "Entered SAP-code: $number" }
            viewModelScope.launch {
                val goodInfo: GoodInfo? = when (number!!.length) {
                    SAP_LENGTH -> database.getGoodInfoBySapCode("000000000000$number", SapCodeType.MATERIAL)
                    else -> database.getGoodInfoBySapCode(number, SapCodeType.MATCODE)
                }
                if (goodInfo != null) {
                    checkData.addGood(goodInfo)
                    openGoodInfoScreen()
                } else {
                    // Сообщение - Данный товар не найден в справочнике
                    navigator.showGoodNotFound()
                }
            }
        }
    }

    private fun addGoodByBarCode() {
        goodNumber.value.let { number ->
            Logg.d { "Entered BAR-code: $number" }
            viewModelScope.launch {
                val goodInfo = database.getGoodInfoByBarCode(number!!)
                if (goodInfo != null) {
                    checkData.addGood(goodInfo)
                    openGoodInfoScreen()
                } else {
                    if (checkData.getCurrentGood()?.barCode == number) {
                        checkData.addGood(GoodInfo(barCode = number))
                        openGoodInfoScreen()
                    } else {
                        // Подтверждение - Неизвестный штрихкод. Товар определить не удалось. Все равно использовать этот штрихкод? - Назад / Да
                        navigator.showUnknownGoodBarcode(
                                barCode = number) {
                            checkData.addGood(GoodInfo(barCode = number))
                            openGoodInfoScreen()
                        }
                    }
                }
            }
        }
    }

    private fun openGoodInfoScreen() {
        // TODO Раскомментировать для тестирования разных сценариев проверки
        /*checkData.countFacings = true
        checkData.checkEmptyPlaces = true*/

        if (checkData.countFacings) {
            navigator.openGoodInfoFacingScreen()
        } else {
            navigator.openGoodInfoScreen()
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
}
