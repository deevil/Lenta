package com.lenta.bp12.features.open_task.good_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodDetailsOpenViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager


    val selectionsHelper = SelectionItemsHelper()

    val task by lazy {
        manager.currentTask
    }

    val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        good.map { good ->
            good?.getNameWithMaterial()
        }
    }

    val items by lazy {
        MutableLiveData(List(3) {
            ItemConsignmentUi(
                    position = "${it + 1}",
                    date = "Test date ${it + 1}",
                    provider = "Test provider ${it + 1}",
                    quantity = (1..15).random().toString()
            )
        })
    }

    val deleteEnabled = selectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    // -----------------------------

    fun onClickDelete() {

    }

}


data class ItemConsignmentUi(
        val position: String,
        val date: String,
        val provider: String,
        val quantity: String
)
