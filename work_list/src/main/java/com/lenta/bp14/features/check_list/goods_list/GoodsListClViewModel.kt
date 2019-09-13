package com.lenta.bp14.features.check_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_list.*
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.getTaskType
import com.lenta.bp14.platform.navigation.IScreenNavigator
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
    lateinit var checkListTaskManager: CheckListTaskManager


    val task by lazy {
        checkListTaskManager.getTask()!!
    }

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
            taskName.value = "${checkListTaskManager.getTaskType()} // ${checkListTaskManager.getTaskName()}"
        }
    }

    fun scanQrCode() {

    }

    fun scanBarCode() {

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
                                barCallback = { addGoodByEan(number) })
                    }
                    else -> addGoodByEan(number)
                }
            }
        }
    }

    private fun addGoodByMaterial(material: String) {
        Logg.d { "Entered MATERIAL: $material" }
        viewModelScope.launch {
            val goodInfo: GoodInfo? = task.getGoodInfoByMaterial(material)
            if (goodInfo != null) {
                addGood(goodInfo)
            } else {
                // Сообщение - Данный товар не найден в справочнике
                navigator.showGoodNotFound()
            }
        }
    }

    private fun addGoodByEan(ean: String) {
        Logg.d { "Entered EAN: $ean" }
        viewModelScope.launch {
            viewModelScope.launch {
                val goodInfo: GoodInfo? = task.getGoodInfoByEan(ean)
                if (goodInfo != null) {
                    addGood(goodInfo)
                } else {
                    // Сообщение - Данный товар не найден в справочнике
                    navigator.showGoodNotFound()
                }
            }
        }
    }

    private fun addGoodByMatcode(matcode: String) {
        Logg.d { "Entered MATCODE: $matcode" }
        viewModelScope.launch {
            viewModelScope.launch {
                val goodInfo: GoodInfo? = task.getGoodInfoByMatcode(matcode)
                if (goodInfo != null) {
                    addGood(goodInfo)
                } else {
                    // Сообщение - Данный товар не найден в справочнике
                    navigator.showGoodNotFound()
                }
            }
        }
    }

    private fun addGood(goodInfo: GoodInfo) {
        // todo Реализовать добавление по весу
        // Наверно в справочнике должен приходить вес по умолчанию?

        val goodsList = goods.value!!.toMutableList()
        val good = goodsList.find { it.ean == goodInfo.ean }
        if (good != null) {
            val index = goodsList.indexOf(good)
            val quantity = goodsList[index].quantity.value!!.toInt()
            goodsList[index].quantity.value = "" + (quantity + 1)
        } else {
            goodsList.add(0, Good(
                    number = goodsList.lastIndex + 2,
                    ean = goodInfo.ean,
                    material = goodInfo.material,
                    name = goodInfo.name + " ${goodsList.lastIndex + 2}"
            ))
        }

        goods.value = goodsList.toList()
        numberField.value = ""
    }

    fun onDigitPressed(digit: Int) {
        numberField.postValue(numberField.value ?: "" + digit)
        requestFocusToNumberField.value = true
    }

}