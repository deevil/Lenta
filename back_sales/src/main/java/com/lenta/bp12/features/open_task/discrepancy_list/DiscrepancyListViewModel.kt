package com.lenta.bp12.features.open_task.discrepancy_list

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
        task.map { task ->
            val itemList = mutableListOf<SimpleItemGood>()

            task?.goods?.map { good ->
                good.positions.filter { !it.isDelete && !it.isCounted }.map { position ->
                    itemList.add(SimpleItemGood(
                            name = good.getNameWithMaterial(),
                            material = good.material,
                            providerCode = position.provider.code
                    ))
                }
            }

            itemList.mapIndexed { index, simpleItemGood ->
                ItemGoodUi(
                        position = "${index + 1}",
                        name = simpleItemGood.name,
                        material = simpleItemGood.material,
                        providerCode = simpleItemGood.providerCode
                )
            }
        }
    }

    // -----------------------------

    fun onClickItemPosition(position: Int) {
        goods.value?.get(position)?.let {
            manager.preparePositionToOpen(it.material, it.providerCode)
            navigator.openGoodInfoOpenScreen()
        }
    }

    private fun isListEmpty() = goods.value?.isNullOrEmpty() == true

    fun onClickDelete() {
        if (isListEmpty()) {
            return
        }

        selectionsHelper.selectedPositions.value?.let {

        }
    }

    fun onClickMissing() {
        if (isListEmpty()) {
            return
        }


    }

    fun onClickSkip() {

    }

}


data class SimpleItemGood(
        val name: String,
        val material: String,
        val providerCode: String
)

data class ItemGoodUi(
        val position: String,
        val name: String,
        val material: String,
        val providerCode: String
)