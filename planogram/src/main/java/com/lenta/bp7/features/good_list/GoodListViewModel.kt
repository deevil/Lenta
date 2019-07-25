package com.lenta.bp7.features.good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    val numberFieldEnabled: MutableLiveData<Boolean> = MutableLiveData(false)

    val applyButtonEnabled: MutableLiveData<Boolean> = goods.map {
        it?.isNotEmpty() ?: false && checkData.getCurrentShelf().status == ShelfStatus.UNFINISHED
    }

    init {
        viewModelScope.launch {
            checkData.let {
                segmentNumber.value = it.getCurrentSegment().number
                shelfNumber.value = it.getCurrentShelf().number
                goods.value = it.getCurrentShelf().goods
                numberFieldEnabled.value = it.getCurrentShelf().status == ShelfStatus.UNFINISHED
            }
        }
    }

    fun updateGoodList() {
        goods.value = checkData.getCurrentShelf().goods
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber()
        return true
    }

    private fun checkEnteredNumber() {
        goodNumber.value.let { number ->
            if (number?.isNotEmpty() == true && number.length >= 6) {
                when (number.length) {
                    SAP_LENGTH -> createGoodBySapCode()
                    SAP_OR_BAR_LENGTH -> {
                        // Выбор - Введено 12 знаков. Какой код вы ввели? - SAP-код / Штрихкод
                        navigator.showTwelveCharactersEntered(
                                sapCallback = ::createGoodBySapCode,
                                barCallback = ::createGoodByBarCode)
                    }
                    else -> createGoodByBarCode()
                }
            }
        }
    }

    private fun createGoodBySapCode() {
        goodNumber.value.let { number ->
            Logg.d { "Entered SAP-code: $number" }
            val sapcode = if (number?.length == 6) "000000000000$number" else "000000$number"
            viewModelScope.launch {
                checkData.addGood(database.getGoodInfoBySapCode(sapcode))
                openGoodInfoScreen()
            }
        }
    }

    private fun createGoodByBarCode() {
        goodNumber.value.let { number ->
            Logg.d { "Entered BAR-code: $number" }
            viewModelScope.launch {
                checkData.addGood(database.getGoodInfoByBarCode(number))
                openGoodInfoScreen()
            }
        }
    }

    private fun openGoodInfoScreen() {
        // todo для тестирования разных сценариев проверки. Потом удалить.
        checkData.countFacings = true
        checkData.checkEmptyPlaces = true

        if (checkData.countFacings) {
            navigator.openGoodInfoFacingScreen()
        } else {
            navigator.openGoodInfoScreen()
        }
    }

    fun onClickApply() {
        // Подтверждение - Сохранить результаты сканирования полки и закрыть ее для редактирования - Назад / Да
        navigator.showSaveShelfScanResults(segmentNumber.value!!, shelfNumber.value!!) {
            checkData.getCurrentShelf().status = ShelfStatus.PROCESSED
            navigator.openShelfListScreen()
        }
    }

    fun onClickBack() {
        if (checkData.getCurrentShelf().status != ShelfStatus.UNFINISHED) {
            navigator.goBack()
            return
        }

        if (goods.value?.isEmpty() == true) {
            checkData.deleteCurrentShelf()
            navigator.goBack()
        } else {
            // Подтверждение - Данные полки не будут сохранены - Назад / Подтвердить
            navigator.showShelfDataWillNotBeSaved(segmentNumber.value!!, shelfNumber.value!!) {
                checkData.getCurrentShelf().status = ShelfStatus.DELETED
                navigator.openShelfListScreen()
            }
        }
    }

    fun onClickItemPosition(position: Int) {
        checkData.currentGoodIndex = position
        openGoodInfoScreen()
    }
}
