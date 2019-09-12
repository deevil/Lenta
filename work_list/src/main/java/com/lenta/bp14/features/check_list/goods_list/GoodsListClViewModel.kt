package com.lenta.bp14.features.check_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.data.pojo.Good
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


    val selectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val taskName = MutableLiveData("")

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val goods = MutableLiveData<List<Good>>()

    val deleteButtonEnabled = selectionsHelper.selectedPositions.map { it?.isNotEmpty() ?: false }
    val saveButtonEnabled = goods.map { it?.isNotEmpty() ?: false }

    init {
        viewModelScope.launch {
            taskName.value = "${checkListTaskManager.getTaskType()} // ${checkListTaskManager.getTaskName()}"
            //taskName.value = checkListTask.getDescription().taskName
            //goods.value = taskManager.getTestGoodList(4)
        }
    }

    fun scanQrCode() {

    }

    fun scanBarCode() {

    }

    fun onClickDelete() {

    }

    fun onClickSave() {

    }

    fun onClickItemPosition(position: Int) {
        //taskManager.currentGood = goods.value?.get(position)
        //navigator.openGoodInfoPcScreen()
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

    private fun addGoodByEan(ean: String) {
        Logg.d { "Entered EAN: $ean" }
        /*viewModelScope.launch {
            val goodInfo = database.getGoodInfoByEan(ean)


            if (goodInfo != null) {
                checkData.addGood(goodInfo)
                //openGoodInfoScreen()
            } else {
                if (checkData.getCurrentGood()?.ean == ean) {
                    checkData.addGood(GoodInfo(enteredCode = EnteredCode.EAN, ean = ean))
                    //openGoodInfoScreen()
                } else {
                    // Сообщение - Данный товар не найден в справочнике
                    navigator.showGoodNotFound()
                }
            }
        }*/
    }

    private fun addGoodByMaterial(material: String) {
        Logg.d { "Entered MATERIAL: $material" }
        /*viewModelScope.launch {
            val goodInfo: GoodInfo? = database.getGoodInfoByMaterial("000000000000$material")
            if (goodInfo != null) {
                checkData.addGood(goodInfo)
                //openGoodInfoScreen()
            } else {
                // Сообщение - Данный товар не найден в справочнике
                navigator.showGoodNotFound()
            }
        }*/
    }

    private fun addGoodByMatcode(matcode: String) {
        Logg.d { "Entered MATCODE: $matcode" }
        /*viewModelScope.launch {
            val goodInfo: GoodInfo? = database.getGoodInfoByMatcode(matcode)
            if (goodInfo != null) {
                checkData.addGood(goodInfo)
                //openGoodInfoScreen()
            } else {
                // Сообщение - Данный товар не найден в справочнике
                navigator.showGoodNotFound()
            }
        }*/
    }

}