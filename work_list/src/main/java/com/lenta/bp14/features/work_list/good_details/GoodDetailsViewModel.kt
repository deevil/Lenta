package com.lenta.bp14.features.work_list.good_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.GoodDetailsTab
import com.lenta.bp14.models.work_list.ScanResult
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import com.lenta.shared.utilities.extentions.dropZeros
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodDetailsViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: WorkListTask


    val shelfLifeSelectionsHelper = SelectionItemsHelper()
    val commentSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val title = MutableLiveData<String>("")

    val good by lazy { task.currentGood }

    val shelfLives: MutableLiveData<List<ItemShelfLifeUi>> by lazy {
        good.map { good ->
            val combinedResults = mutableMapOf<String, ScanResult>()
            good?.scanResults?.map { result ->
                val key = result.getKeyFromDates()
                if (result.productionDate != null || result.expirationDate != null) {
                    combinedResults[key] = if (combinedResults.containsKey(key)) {
                        val totalQuantity = combinedResults[key]!!.quantity.sumWith(result.quantity)
                        combinedResults[key]!!.copy(quantity = totalQuantity)
                    } else result
                }
            }

            combinedResults.values.mapIndexed { index, scanResult ->
                ItemShelfLifeUi(
                        position = (index + 1).toString(),
                        expirationDate = scanResult.getFormattedExpirationDate(),
                        productionDate = scanResult.getFormattedProductionDate(),
                        productionDateVisibility = scanResult.productionDate != null,
                        expirationDateVisibility = scanResult.expirationDate != null,
                        quantity = "${scanResult.quantity.dropZeros()} ${task.currentGood.value!!.units.name}"
                )
            }
        }
    }

    val comments: MutableLiveData<List<ItemCommentUi>> by lazy {
        good.map { good ->
            val combinedResults = mutableMapOf<String, ScanResult>()
            val commentNotSelected = good?.comments?.get(0)?.description
            good?.scanResults?.map { result ->
                val key = result.comment
                if (key != commentNotSelected) {
                    combinedResults[key] = if (combinedResults.containsKey(key)) {
                        val totalQuantity = combinedResults[key]!!.quantity.sumWith(result.quantity)
                        combinedResults[key]!!.copy(quantity = totalQuantity)
                    } else result
                }
            }

            combinedResults.values.mapIndexed { index, scanResult ->
                ItemCommentUi(
                        position = (index + 1).toString(),
                        comment = scanResult.comment,
                        quantity = "${scanResult.quantity.dropZeros()} ${task.currentGood.value!!.units.name}"
                )
            }
        }
    }


    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = selectedPage
            .combineLatest(shelfLifeSelectionsHelper.selectedPositions)
            .combineLatest(commentSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.first?.toInt()
                val shelfLifeSelected = it?.first?.second?.isNotEmpty() == true
                val commentSelected = it?.second?.isNotEmpty() == true
                tab == GoodDetailsTab.SHELF_LIVES.position && shelfLifeSelected || tab == GoodDetailsTab.COMMENTS.position && commentSelected
            }

    val deleteButtonEnabled = selectedItemOnCurrentTab.map { it }

// -----------------------------

    init {
        viewModelScope.launch {
            title.value = task.currentGood.value?.getFormattedMaterialWithName()
        }
    }

// -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {
        when (selectedPage.value) {
            GoodDetailsTab.SHELF_LIVES.position -> {
                shelfLifeSelectionsHelper.selectedPositions.value?.apply {
                    val shelfLivesForDelete = mutableListOf<String>()
                    shelfLives.value!!.mapIndexed { index, itemShelfLifeUi ->
                        if (this.contains(index)) {
                            shelfLivesForDelete.add(itemShelfLifeUi.productionDate + itemShelfLifeUi.expirationDate)
                        }
                    }

                    task.deleteScanResultsByShelfLives(shelfLivesForDelete)
                    shelfLifeSelectionsHelper.clearPositions()
                }
            }
            GoodDetailsTab.COMMENTS.position -> {
                commentSelectionsHelper.selectedPositions.value?.apply {
                    val commentsForDelete = mutableListOf<String>()
                    comments.value!!.mapIndexed { index, itemCommentUi ->
                        if (this.contains(index)) {
                            commentsForDelete.add(itemCommentUi.comment)
                        }
                    }

                    task.deleteScanResultsByComments(commentsForDelete)
                    commentSelectionsHelper.clearPositions()
                }
            }
        }
    }

}

data class ItemShelfLifeUi(
        val position: String,
        val expirationDate: String,
        val productionDate: String,
        val productionDateVisibility: Boolean,
        val expirationDateVisibility: Boolean,
        val quantity: String
)

data class ItemCommentUi(
        val position: String,
        val comment: String,
        val quantity: String
)