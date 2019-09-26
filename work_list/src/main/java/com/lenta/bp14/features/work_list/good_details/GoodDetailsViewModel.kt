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

    val good by lazy { task.currentGood }

    val title = MutableLiveData<String>("")


    val shelfLives: MutableLiveData<List<ItemShelfLifeUi>> by lazy {
        task.currentGood.value!!.scanResults.map { list: List<ScanResult>? ->
            list?.mapIndexed { index, scanResult ->
                ItemShelfLifeUi(
                        position = (index + 1).toString(),
                        expirationDate = scanResult.getFormattedExpirationDate(),
                        productionDate = scanResult.getFormattedProductionDate(),
                        productionDateVisibility = scanResult.productionDate != null,
                        quantity = "${scanResult.quantity} ${task.currentGood.value!!.getUnits()}"
                )
            }
        }
    }

    val comments: MutableLiveData<List<ItemCommentUi>> by lazy {
        task.currentGood.value!!.scanResults.map { list: List<ScanResult>? ->
            list?.let { scanResults ->
                val comments = mutableSetOf<String>()
                scanResults.map { comments.add(it.comment) }

                val combinedResults = mutableMapOf<String, ScanResult>()

                for (scanResult in scanResults) {
                    if (combinedResults.containsKey(scanResult.comment)) {
                        combinedResults[scanResult.comment]!!.quantity += scanResult.quantity
                    } else {
                        combinedResults[scanResult.comment] = scanResult
                    }
                }

                combinedResults.toList().mapIndexed { index, scanResult ->
                    ItemCommentUi(
                            position = (index + 1).toString(),
                            comment = scanResult.second.comment,
                            quantity = "${scanResult.second.quantity} ${task.currentGood.value!!.getUnits()}"
                    )
                }
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
            title.value = good.value?.getFormattedMaterialWithName()


        }
    }

// -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {
        if (selectedPage.value == GoodDetailsTab.SHELF_LIVES.position) {
            task.deleteSelectedScanResults(shelfLifeSelectionsHelper.selectedPositions.value)
            shelfLifeSelectionsHelper.clearPositions()
        }


        /* val goodsList = goods.value!!.toMutableList()

         selectionsHelper.selectedPositions.value?.apply {
             val eans = goods.value?.filterIndexed { index, _ ->
                 this.contains(index)
             }?.map { it.ean }?.toSet() ?: emptySet()

             goodsList.removeAll { eans.contains(it.ean) }
         }

         for (index in goodsList.lastIndex downTo 0) {
             goodsList[index].number = goodsList.lastIndex + 1 - index
         }

         selectionsHelper.clearPositions()
         goods.value = goodsList.toList()*/


        /*when (selectedPage.value) {
            1 -> shelfLifeSelectionsHelper
            2 -> commentSelectionsHelper
            else -> null
        }?.let { selectionHelper ->
            selectionHelper.selectedPositions.value?.apply {
                task.removeCheckResultsByMatNumbers(
                        if (selectionHelper === processedSelectionsHelper) {
                            processedGoods
                        } else {
                            searchGoods
                        }.value?.filterIndexed { index, _ ->
                            this.contains(index)
                        }?.map { it.matNr }?.toSet()
                                ?: emptySet()
                )
            }
            selectionHelper.clearPositions()
        }*/
    }

}

data class ItemShelfLifeUi(
        val position: String,
        val expirationDate: String,
        val productionDate: String,
        val productionDateVisibility: Boolean,
        val quantity: String
)

data class ItemCommentUi(
        val position: String,
        val comment: String,
        val quantity: String
)