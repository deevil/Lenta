package com.lenta.bp16.features.reprint_label

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class ReprintLabelViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo


    val selectionsHelper = SelectionItemsHelper()

    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val labels by lazy {
        MutableLiveData(List(3){
            ReprintLabelUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}",
                    order = "555",
                    time = "12.12.2020 12:12",
                    quantity = "${(1..123).random()} кг"
            )
        })
    }

    val printEnabled by lazy {
        selectionsHelper.selectedPositions.map { it?.isNotEmpty() }
    }

    // -----------------------------

    fun onClickItemPosition(position: Int) {

    }

    fun onClickPrint() {

    }

}

data class ReprintLabelUi(
        val position: String,
        val name: String,
        val order: String,
        val time: String,
        val quantity: String
)