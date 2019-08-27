package com.lenta.bp14.features.work_list.goods_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.data.model.TaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodsListWlViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: TaskManager


    val selectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val taskName = MutableLiveData("Рабочий список от 23.07.19 23:15")

    val processingGoods = MutableLiveData<List<Good>>(getTestItems())
    val processedGoods = MutableLiveData<List<Good>>(getTestItems())
    val searchGoods = MutableLiveData<List<Good>>(getTestItems())

    val deleteButtonEnabled = processingGoods.map {
        it?.isNotEmpty() ?: false
    }

    val saveButtonEnabled = processingGoods.map {
        it?.isNotEmpty() ?: false
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun getTestItems(): List<Good>? {
        return List(3) {
            Good(
                    id = it + 1,
                    material = "000000000000" + (111111..999999).random(),
                    name = "Товар ${it + (1..99).random()}",
                    uom = Uom.DEFAULT
            )
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return false
    }

    fun scanQrCode() {

    }

    fun scanBarCode() {

    }

    fun onClickSave() {

    }

    fun onClickDelete() {

    }

    fun onClickFilter() {
        navigator.openSearchFilterWlScreen()
    }

    fun onClickItemPosition(position: Int) {
        taskManager.currentGood = getGoodByPosition(position)
        navigator.openGoodInfoWlScreen()
    }

    private fun getGoodByPosition(position: Int): Good? {
        return when (selectedPage.value) {
            0 -> processingGoods.value?.get(position)
            1 -> processedGoods.value?.get(position)
            else -> searchGoods.value?.get(position)
        }
    }
}
