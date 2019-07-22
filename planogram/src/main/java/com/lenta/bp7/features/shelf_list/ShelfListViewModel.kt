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
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShelfListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData

    val selectionsHelper = SelectionItemsHelper()

    val shelves: MutableLiveData<List<Shelf>> = MutableLiveData()

    val segmentNumber: MutableLiveData<String> = MutableLiveData()
    val shelfNumber: MutableLiveData<String> = MutableLiveData("")
    private val unfinishedCurrentSegment: MutableLiveData<Boolean> = MutableLiveData()
    private val deletedCurrentSegment: MutableLiveData<Boolean> = MutableLiveData()

    val deleteButtonEnabled: MutableLiveData<Boolean> = shelves.combineLatest(deletedCurrentSegment).map { pair ->
        pair?.first?.isNotEmpty() ?: false && pair?.second == false
    }

    val applyButtonEnabled: MutableLiveData<Boolean> = shelves.combineLatest(unfinishedCurrentSegment).map { pair ->
        pair?.first?.isNotEmpty() ?: false && pair?.second == true &&
                pair.first.find { it.status == ShelfStatus.PROCESSED } != null
    }

    init {
        viewModelScope.launch {
            checkData.let {
                segmentNumber.value = it.getCurrentSegment().number
                shelves.value = it.getCurrentSegment().shelves
                unfinishedCurrentSegment.value = it.getCurrentSegment().status == SegmentStatus.UNFINISHED
                deletedCurrentSegment.value = it.getCurrentSegment().status == SegmentStatus.DELETED
            }
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        createShelf()
        return true
    }

    private fun createShelf() {
        if (shelfNumber.value?.isNotEmpty() == true) {
            checkData.addShelf(shelfNumber.value!!)
            navigator.openGoodListScreen()
        }
    }

    fun onClickDelete() {
        selectionsHelper.let {
            val items = it.selectedPositions.value?.toMutableSet()
            if (items?.isEmpty() == true) {
                // todo ЭКРАН подтверждение удаления данных по сегменту

                // !Перенести на другой экран
                checkData.getCurrentSegment().status = SegmentStatus.DELETED
                navigator.goBack()
            } else {
                // todo ЭКРАН подтверждение удаления полок

                // !Перенести на другой экран
                items!!.forEach { index ->
                    it.revert(index)
                    checkData.setShelfStatusDeletedByIndex(index)
                    shelves.value = checkData.getCurrentSegment().shelves
                }
            }
        }
    }

    fun onClickApply() {
        // todo ЭКРАН сохранить результаты и закрыть для редактирования

        // !Перенести на другой экран
        checkData.getCurrentSegment().status = SegmentStatus.PROCESSED
        navigator.goBack()
    }

    fun onClickBack() {
        if (unfinishedCurrentSegment.value == false) {
            navigator.goBack()
            return
        }

        if (shelves.value?.isEmpty() == true) {
            // todo ЭКРАН предупреждение об удалении пустого сегмента

            // !Перенести на другой экран
            checkData.deleteCurrentSegment()
            navigator.goBack()
        } else {
            // todo ЭКРАН сохранить результаты и закрыть для редактирования

            // !Перенести на другой экран
            checkData.getCurrentSegment().status = SegmentStatus.PROCESSED
            navigator.goBack()
        }
    }

    fun onClickItemPosition(position: Int) {
        checkData.currentShelfIndex = position
        navigator.openGoodListScreen()
    }
}
