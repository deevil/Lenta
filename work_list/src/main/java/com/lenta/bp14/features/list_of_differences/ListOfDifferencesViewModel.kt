package com.lenta.bp14.features.list_of_differences

import com.lenta.bp14.features.common_ui_model.SimpleProductUi
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class ListOfDifferencesViewModel : CoreViewModel() {

    var onClickSkipCallbackID: Int? = null

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager


    private val task by lazy {
        generalTaskManager.getProcessedTask()!!
    }

    val selectionsHelper = SelectionItemsHelper()

    val title by lazy {
        "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    val goods by lazy {
        task.getListOfDifferences().map {
            it?.mapIndexed { index, productInfo ->
                SimpleProductUi(
                        position = index + 1,
                        matNr = productInfo.matNr,
                        name = "${productInfo.matNr.takeLast(6)} ${productInfo.name}"
                )
            }
        }
    }

    val missingButtonEnabled by lazy {
        goods.map { it?.isNotEmpty() }
    }

    fun onClickMissing() {
        selectionsHelper.selectedPositions.value.let { positions ->
            if (positions!!.isEmpty()) {
                selectionsHelper.addAll(goods.value!!)
            }

            task.setMissing(positions.map { goods.value!![it].matNr })
        }

        selectionsHelper.clearPositions()
    }

    fun onClickSkip() {
        navigator.goBackWithResultCode(onClickSkipCallbackID)
    }

    fun onClickItemPosition(position: Int) {

    }

}
