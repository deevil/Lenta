package com.lenta.bp14.features.check_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_list.Good
import com.lenta.bp14.models.check_list.ICheckListTask
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListClViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: ICheckListTask

    val selectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val taskName = MutableLiveData("")

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val goods = MutableLiveData<List<Good>>(listOf())

    val deleteButtonEnabled = selectionsHelper.selectedPositions.map { it?.isNotEmpty() ?: false }
    val saveButtonEnabled = goods.map { it?.isNotEmpty() ?: false }

    init {
        viewModelScope.launch {
            requestFocusToNumberField.value = true
            taskName.value = "${task.getTaskType().taskType} // ${task.getTaskName()}"
        }
    }

    fun onClickDelete() {
        val goodsList = goods.value!!.toMutableList()

        selectionsHelper.selectedPositions.value?.apply {
            val eans = goods.value?.filterIndexed { index, _ ->
                this.contains(index)
            }?.map { it.ean }?.toSet() ?: emptySet()

            goodsList.removeAll { eans.contains(it.ean) }
        }

        for (index in goodsList.lastIndex downTo 0) {
            goodsList[index].number = goodsList.lastIndex + 1 - index
        }

        selectionsHelper.clearPositions()
        goods.value = goodsList.toList()
    }

    fun onClickSave() {
        // Подтверждение - Перевести задание в статус "Подсчитано" и закрыть его для редактирования? - Назад / Да
        navigator.showSetTaskToStatusCalculated {
            task.openToEdit = false

            // todo Реализовать сохранение задания?
            task.saveScannedGoodList(goods.value!!)
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(numberField.value ?: "")
        return true
    }

    private fun checkEnteredNumber(number: String) {
        analyseCode(
                code = number,
                funcForEan = { eanCode ->
                    addGood(task.getGoodByEan(eanCode))
                },
                funcForMatNr = { matNr ->
                    addGood(task.getGoodByMaterial(matNr))
                },
                funcForPriceQrCode = { qrCode ->
                    //getGoodByEan(matNr)
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun addGood(good: Good?) {
        if (good == null) {
            // Сообщение - Данный товар не найден в справочнике
            navigator.showGoodNotFound()
            return
        }

        val goodsList = goods.value!!.toMutableList()
        val existGood = goodsList.find { it.ean == good.ean }
        if (existGood != null) {
            val existQuantity = existGood.quantity.value!!.toDoubleOrNull()
            val quantity = good.quantity.value!!.toDoubleOrNull()
            goodsList[goodsList.indexOf(existGood)].quantity.value = existQuantity.sumWith(quantity).dropZeros()
        } else {
            goodsList.add(0, good)
        }

        goods.value = goodsList.toList()
        numberField.value = ""
    }

    fun onDigitPressed(digit: Int) {
        numberField.postValue(numberField.value ?: "" + digit)
        requestFocusToNumberField.value = true
    }

    fun onClickVideo() {
        navigator.openVideoScanProductScreen()

    }

    fun onScanResult(data: String) {

    }

}