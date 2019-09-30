package com.lenta.bp14.features.check_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_list.*
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.isOnlyInt
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
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
        number.length.let { length ->
            if (length < Constants.COMMON_SAP_LENGTH) {
                // Сообщение - Данный товар не найден в справочнике
                navigator.showGoodNotFound()
                return
            }

            if (length >= Constants.COMMON_SAP_LENGTH) {
                when (length) {
                    Constants.COMMON_SAP_LENGTH -> addGoodByMaterial(number)
                    Constants.SAP_OR_BAR_LENGTH -> {
                        // Выбор - Введено 12 знаков. Какой код вы ввели? - SAP-код / Штрихкод
                        navigator.showTwelveCharactersEntered(
                                sapCallback = { addGoodByMatcode(number) },
                                barCallback = { addGoodByEan(number) }
                        )
                    }
                    else -> addGoodByEan(number)
                }
            }
        }
    }

    private fun addGoodByMaterial(material: String) {
        Logg.d { "Entered MATERIAL: $material" }
        viewModelScope.launch {
            addGood(task.getGoodByMaterial(material))
        }
    }

    private fun addGoodByEan(ean: String) {
        Logg.d { "Entered EAN: $ean" }
        viewModelScope.launch {
            addGood(task.getGoodByEan(ean))
        }
    }

    private fun addGoodByMatcode(matcode: String) {
        Logg.d { "Entered MATCODE: $matcode" }
        viewModelScope.launch {
            addGood(task.getGoodByMatcode(matcode))
        }
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
            val index = goodsList.indexOf(existGood)
            goodsList[index].quantity.value = "" + if (good.uom.isOnlyInt()) {
                existGood.quantity.value!!.toInt() + good.quantity.value!!.toInt()
            } else {
                val goodQuantity = good.quantity.value!!.toBigDecimal()
                val existGoodQuantity = existGood.quantity.value!!.toBigDecimal()
                existGoodQuantity.plus(goodQuantity).toString().dropLastWhile { it == '0' || it == '.' }
            }
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