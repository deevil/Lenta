package com.lenta.bp7.features.good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.data.model.ShelfStatus
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import javax.inject.Inject

class GoodListViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData

    val goods: MutableLiveData<List<Good>> = MutableLiveData()

    val segmentNumber: MutableLiveData<String> = MutableLiveData()
    val shelfNumber: MutableLiveData<String> = MutableLiveData()
    val goodNumber: MutableLiveData<String> = MutableLiveData("")
    private val unfinishedCurrentShelf: MutableLiveData<Boolean> = MutableLiveData()

    val applyButtonEnabled: MutableLiveData<Boolean> = goods.combineLatest(unfinishedCurrentShelf).map { pair ->
        pair?.first?.isNotEmpty() ?: false && pair?.second == true
    }

    init {
        viewModelScope.launch {
            checkData.getCurrentSegment().let {
                segmentNumber.value = it.number
                shelfNumber.value = it.getCurrentShelf().number
                goods.value = it.getCurrentShelf().goods
                unfinishedCurrentShelf.value = it.getCurrentShelf().status == ShelfStatus.UNFINISHED
            }
        }
    }

    fun createGood() {
        goodNumber.value.let { number ->
            if (number?.isNotEmpty() == true && number.length >= 6) {
                if (number.length == 6) {
                    Logg.d { "Entered SAP-code: $number" }
                    val good = goods.value?.find { it.sapCode == number }
                    checkData.getCurrentSegment().getCurrentShelf().let { currentShelf ->
                        when (good) {
                            null -> {
                                viewModelScope.launch {
                                    currentShelf.addGood(database.getGoodInfoBySapCode("000000000000$number"))
                                }
                            }
                            else -> currentShelf.currentGoodIndex = goods.value?.indexOf(good)
                                    ?: throw IllegalArgumentException("Good with SAP-$number already exist, but not found!")
                        }
                    }
                }

                if (number.length == 12) { // введен sap/bar код
                    Logg.d { "Entered SAP or BAR-code: $number" }
                    // todo ЭКРАН выбора типа введенного кода

                }

                if (number.length > 6) {
                    Logg.d { "Entered BAR-code: $number" }
                    val good = goods.value?.find { it.barCode == number }
                    checkData.getCurrentSegment().getCurrentShelf().let { currentShelf ->
                        when (good) {
                            null -> {
                                viewModelScope.launch {
                                    currentShelf.addGood(database.getGoodInfoByBarCode(number))
                                }
                            }
                            else -> currentShelf.currentGoodIndex = goods.value?.indexOf(good)
                                    ?: throw IllegalArgumentException("Good with BAR-$number already exist, but not found!")
                        }
                    }
                }

                //navigator.openGoodInfoScreen()
            }
        }
    }

    fun onClickApply() {
        // todo ЭКРАН подтверждение завершения сканирования полки

        // !Перенести на другой экран
        checkData.getCurrentSegment().getCurrentShelf().status = ShelfStatus.PROCESSED
        navigator.goBack()
    }

    fun onClickBack() {
        if (unfinishedCurrentShelf.value == false) {
            navigator.goBack()
            return
        }

        if (goods.value?.isEmpty() == true) {
            // todo ЭКРАН полка пуста и будет удалена

            // !Перенести на другой экран
            checkData.getCurrentSegment().deleteCurrentShelf()
            navigator.goBack()
        } else {
            // todo ЭКРАН сохранить результаты и закрыть для редактирования

            // !Перенести на другой экран
            checkData.getCurrentSegment().getCurrentShelf().status = ShelfStatus.PROCESSED
            navigator.goBack()
        }
    }

    fun onClickItemPosition(position: Int) {
        checkData.getCurrentSegment().getCurrentShelf().currentGoodIndex = position
        navigator.openGoodInfoScreen()
    }
}
