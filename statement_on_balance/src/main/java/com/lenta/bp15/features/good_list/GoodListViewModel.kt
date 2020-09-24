package com.lenta.bp15.features.good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class GoodListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        "ПНБ(ТК)-303 / Постановка на баланс"
    }

    val numberField = MutableLiveData("")

    val requestFocusToNumberField = MutableLiveData(false)

    val processingList = MutableLiveData(
            List((3..7).random()) {
                val position = (it + 1).toString()
                ItemGoodUi(
                        position = position,
                        name = "Test name $position",
                        quantity = (1..25).random().toString()
                )
            }
    )

    val processedList = MutableLiveData(
            List((3..7).random()) {
                val position = (it + 1).toString()
                ItemGoodUi(
                        position = position,
                        name = "Test name $position",
                        quantity = (1..25).random().toString()
                )
            }
    )

    override fun onPageSelected(position: Int) {

    }

    override fun onOkInSoftKeyboard(): Boolean {
        return false
    }

    fun onClickItemPosition(position: Int) {

    }

    fun onScanResult(data: String) {

    }

    fun onClickComplete() {

    }

}