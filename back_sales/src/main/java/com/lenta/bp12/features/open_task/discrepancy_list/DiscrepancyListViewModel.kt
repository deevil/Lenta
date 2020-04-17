package com.lenta.bp12.features.open_task.discrepancy_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import javax.inject.Inject

class DiscrepancyListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator


    val selectionsHelper = SelectionItemsHelper()

    val title by lazy {
        "ВПП-328 // Возврат от 10.12.2018 15:20"
    }

    val deleteEnabled = MutableLiveData(false)

    val deleteVisibility = MutableLiveData(false)

    val missingEnabled = MutableLiveData(false)

    val goods by lazy {
        MutableLiveData(List(3) {
            ItemGoodUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}"
            )
        })
    }

    // -----------------------------

    fun onClickDelete() {

    }

    fun onClickMissing() {

    }

    fun onClickSkip() {

    }

    fun onClickItemPosition(position: Int) {

    }

}

data class ItemGoodUi(
        val position: String,
        val name: String
)