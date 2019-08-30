package com.lenta.bp14.features.work_list.good_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.GoodDetailsTab
import com.lenta.bp14.data.TaskManager
import com.lenta.bp14.data.model.Comment
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.data.model.ShelfLife
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
    lateinit var taskManager: TaskManager


    val shelfLifeSelectionsHelper = SelectionItemsHelper()
    val commentSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val good = MutableLiveData<Good>()
    val formattedGoodName = MutableLiveData<String>()

    val shelfLives = MutableLiveData<List<ShelfLife>>()
    val comments = MutableLiveData<List<Comment>>()

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

    init {
        viewModelScope.launch {
            good.value = taskManager.getTestGoodList(1)[0]
            shelfLives.value = good.value?.shelfLives
            comments.value = good.value?.comments

            formattedGoodName.value = good.value?.getFormattedMaterialWithName()
        }
    }

    fun onClickDelete() {

    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }
}