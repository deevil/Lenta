package com.lenta.bp7.features.shelf_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.SegmentStatus
import com.lenta.bp7.data.model.Shelf
import com.lenta.bp7.data.model.ShelfStatus
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShelfListViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData

    val selectionsHelper = SelectionItemsHelper()

    val segmentNumber: MutableLiveData<String> = MutableLiveData()
    val shelfNumber: MutableLiveData<String> = MutableLiveData("")
    val shelves: MutableLiveData<List<Shelf>> = MutableLiveData()

    val deleteButtonEnabled: MutableLiveData<Boolean> = shelves.map { it?.isNotEmpty() ?: false && !isDeletedCurrentSegment() }
    val applyButtonEnabled: MutableLiveData<Boolean> = shelves.map { it?.isNotEmpty() ?: false && isExistProcessedShelf() }

    init {
        viewModelScope.launch {
            checkData.let {
                segmentNumber.value = it.getCurrentSegment().number
                shelves.value = it.getCurrentSegment().shelves
            }
        }
    }

    private fun isExistProcessedShelf(): Boolean {
        for (shelf in shelves.value!!) {
            if (shelf.status == ShelfStatus.PROCESSED) {
                return true
            }
        }
        //shelves.value?.find {it.status == ShelfStatus.PROCESSED }

        return false
    }

    private fun isDeletedCurrentSegment(): Boolean {
        return checkData.getCurrentSegment().status == SegmentStatus.DELETED
    }

    fun createShelf() {
        checkData.getCurrentSegment().addShelf(shelfNumber.value!!)
        navigator.openGoodListScreen()
    }

    fun onClickBack() {
        if (shelves.value?.isEmpty()!!) {
            // todo ЭКРАН предупреждение об удалении пустого сегмента

            // Перенести в логику указанного экрана
            checkData.deleteCurrentSegment()
            navigator.goBack()
        } else {
            checkData.let {
                if (it.getCurrentSegment().status == SegmentStatus.CREATED) {
                    it.getCurrentSegment().status = SegmentStatus.UNFINISHED
                    it.isExistUnfinishedSegment = true
                }
            }
            navigator.goBack()
        }
    }

    fun onClickDelete() {
        selectionsHelper.let {
            val items = it.selectedPositions.value?.toMutableSet()
            if (items?.isEmpty()!!) {
                // todo ЭКРАН подтверждение удаления данных по сегменту

                // Перенести на экран удаления данных по сегменту
                checkData.getCurrentSegment().status = SegmentStatus.DELETED
                navigator.goBack()
            } else {
                // todo ЭКРАН подтверждение удаления полок

                for (index in items) {
                    it.revert(index) // todo почему не снимается выделение?
                    checkData.getCurrentSegment().changeShelfStatusByIndex(index, ShelfStatus.DELETED)
                }
            }
        }
    }

    fun onClickApply() {
        // todo ЭКРАН подтверждение завершения сканирования сегмента

    }

    fun onClickItemPosition(position: Int) {
        checkData.getCurrentSegment().currentShelfIndex = position
        navigator.openGoodListScreen()
    }
}
