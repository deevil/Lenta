package com.lenta.bp7.features.good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.*
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
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

    override fun onOkInSoftKeyboard(): Boolean {
        createGood()
        return true
    }

    private fun createGood() {
        goodNumber.value.let { number ->
            if (number?.isNotEmpty() == true && number.length >= 6) {
                if (number.length == SAP_LENGTH) {
                    Logg.d { "Entered SAP-code: $number" }
                    val good = goods.value?.find { it.sapCode == "000000000000$number" }
                    checkData.let {
                        when (good) {
                            null -> {
                                viewModelScope.launch {
                                    it.addGood(database.getGoodInfoBySapCode("000000000000$number"))
                                    openInfoScreen()
                                }
                            }
                            else -> {
                                it.currentGoodIndex = goods.value?.indexOf(good)
                                        ?: throw IllegalArgumentException("Good with SAP-$number exist, but not found!")
                                openInfoScreen()
                            }
                        }
                    }
                }

                if (number.length == SAP_OR_BAR_LENGTH) {
                    Logg.d { "Entered SAP or BAR-code: $number" }
                    // todo ЭКРАН выбора типа введенного кода

                }

                if (number.length > SAP_LENGTH) {
                    Logg.d { "Entered BAR-code: $number" }
                    val good = goods.value?.find { it.barCode == number }
                    checkData.let {
                        when (good) {
                            null -> {
                                viewModelScope.launch {
                                    it.addGood(database.getGoodInfoByBarCode(number))
                                    openInfoScreen()
                                }
                            }
                            else -> {
                                it.currentGoodIndex = goods.value?.indexOf(good)
                                        ?: throw IllegalArgumentException("Good with BAR-$number exist, but not found!")
                                openInfoScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openInfoScreen() {
        // todo для тестирования разных сценариев проверки. Потом удалить.
        checkData.countFacings = false
        checkData.checkEmptyPlaces = true

        if (checkData.countFacings) {
            navigator.openGoodInfoFacingScreen()
        } else {
            navigator.openGoodInfoScreen()
        }
    }

    fun onClickApply() {
        // todo ЭКРАН сохранить результаты сканирования полки и закрыть для редактирования

        // !Перенести на другой экран
        checkData.getCurrentShelf().status = ShelfStatus.PROCESSED
        navigator.goBack()
    }

    fun onClickBack() {
        if (checkData.getCurrentShelf().status != ShelfStatus.UNFINISHED) {
            navigator.goBack()
            return
        }

        // В одном месте есть инфо о том, что полка не будет сохранена
        // В другом, что она может иметь статус незавершенной
        // todo узнать какое поведение правильное

        // Предположительное поведение...
        if (goods.value?.isEmpty() == true) {
            // todo ЭКРАН полка пуста и будет удалена

            // !Перенести на другой экран
            checkData.deleteCurrentShelf()
            navigator.openSegmentListScreen()
        } else {
            // todo ЭКРАН сохранить результаты сканирования полки и закрыть для редактирования

            // !Перенести на другой экран
            checkData.getCurrentShelf().status = ShelfStatus.PROCESSED
            navigator.openSegmentListScreen()
        }
    }

    fun onClickItemPosition(position: Int) {
        checkData.currentGoodIndex = position
        openInfoScreen()
    }
}
