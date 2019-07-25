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

    val numberFieldEnabled: MutableLiveData<Boolean> = MutableLiveData(false)

    val deleteButtonEnabled: MutableLiveData<Boolean> = shelves.map {
        it?.isNotEmpty() ?: false && checkData.getCurrentSegment().status != SegmentStatus.DELETED
    }

    val applyButtonEnabled: MutableLiveData<Boolean> = shelves.map {
        it?.isNotEmpty() ?: false && checkData.getCurrentSegment().status == SegmentStatus.UNFINISHED &&
                it?.find { shelf -> shelf.status == ShelfStatus.PROCESSED } != null
    }

    init {
        viewModelScope.launch {
            checkData.let {
                segmentNumber.value = it.getCurrentSegment().number
                shelves.value = it.getCurrentSegment().shelves
                numberFieldEnabled.value = it.getCurrentSegment().status == SegmentStatus.UNFINISHED
            }
        }
    }

    fun updateShelfList() {
        shelves.value = checkData.getCurrentSegment().shelves
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
                // Подтверждение - Удалить данные по сегменту? - Назад / Удалить
                navigator.showDeleteDataOnSegment(checkData.getCurrentSegment().storeNumber, segmentNumber.value!!) {
                    checkData.getCurrentSegment().status = SegmentStatus.DELETED
                    navigator.openSegmentListScreen()
                }
            } else {
                items!!.forEach { index ->
                    it.revert(index)
                    checkData.setShelfStatusDeletedByIndex(index)
                    shelves.value = checkData.getCurrentSegment().shelves
                }
            }
        }
    }

    fun onClickApply() {
        // Подтверждение - Сохранить результаты сканирования сегмента, закрыть его для редактирования и переслать? - Назад / Да
        navigator.showSaveSegmentScanResults(segmentNumber.value!!) {
            checkData.getCurrentSegment().status = SegmentStatus.PROCESSED

            // TODO сюда добавить логику отправки сегмента на сервер

            navigator.openSegmentListScreen()
        }
    }

    fun onClickBack() {
        if (checkData.getCurrentSegment().status != SegmentStatus.UNFINISHED) {
            navigator.goBack()
            return
        }

        if (shelves.value?.isEmpty() == true) {
            // Подтверждение - В сегменте отсутствуют полки для сохранения. Сегмент не будет сохранен - Назад / Подтвердить
            navigator.showNoShelvesInSegmentToSave(segmentNumber.value!!) {
                checkData.deleteCurrentSegment()
                navigator.openSegmentListScreen()
            }
        } else {
            // Сегмен остается незавершенным
            navigator.openSegmentListScreen()
        }
    }

    fun onClickItemPosition(position: Int) {
        checkData.currentShelfIndex = position
        navigator.openGoodListScreen()
    }
}
