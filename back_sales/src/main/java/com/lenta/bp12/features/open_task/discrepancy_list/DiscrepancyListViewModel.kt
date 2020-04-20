package com.lenta.bp12.features.open_task.discrepancy_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class DiscrepancyListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: IOpenTaskManager


    val selectionsHelper = SelectionItemsHelper()

    val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { task ->
            "${task?.properties?.type}-${task?.number} // ${task?.name}"
        }
    }

    val goods by lazy {
        MutableLiveData(List(3) {
            ItemGoodUi(
                    position = "${it + 1}",
                    name = "Test name ${it + 1}"
            )
        })
    }

    val deleteEnabled = MutableLiveData(false)

    val deleteVisibility = MutableLiveData(false)

    val missingEnabled = MutableLiveData(false)

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