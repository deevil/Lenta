package com.lenta.bp12.features.open_task.category_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class CategoryDetailsViewModel : CoreViewModel() {

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

    val categories by lazy {
        MutableLiveData(List(3) {
            ItemCategoryUi(
                    position = "${it + 1}",
                    type = "Test category ${it + 1}",
                    quantity = (1..15).random().toString()
            )
        })
    }

    val deleteEnabled = MutableLiveData(false)

    // -----------------------------

    fun onClickDelete() {

    }

}


data class ItemCategoryUi(
        val position: String,
        val type: String,
        val quantity: String
)
