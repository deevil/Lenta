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
        it?.isNotEmpty() ?: false && checkData.getCurrentSegment()?.getStatus() != SegmentStatus.DELETED
    }

    val applyButtonEnabled: MutableLiveData<Boolean> = shelves.map {
        it?.isNotEmpty() ?: false && checkData.getCurrentSegment()?.getStatus() == SegmentStatus.UNFINISHED &&
                it?.find { shelf -> shelf.getStatus() == ShelfStatus.PROCESSED } != null
    }

    init {
        viewModelScope.launch {
            checkData.let {
                segmentNumber.value = it.getCurrentSegment()?.number
                shelves.value = it.getCurrentSegment()?.shelves
                numberFieldEnabled.value = it.getCurrentSegment()?.getStatus() == SegmentStatus.UNFINISHED
            }
        }
    }

    fun updateShelfList() {
        shelves.value = checkData.getCurrentSegment()?.shelves
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkNumber()
        return true
    }

    private fun checkNumber() {
        val shelfNumber = shelfNumber.value?.toInt() ?: 0
        if (shelfNumber > 0) {
            val shelf = shelves.value?.find { it.number.toInt() == shelfNumber }
            if (shelf != null) {
                if (shelf.getStatus() == ShelfStatus.DELETED) {
                    // Выбор - Полка удалена. Открыть просмотр или создать новую? - Назад / Просмотр / Создать
                    navigator.showShelfIsDeleted(
                            reviewCallback = { openExistShelf(shelf) },
                            createCallback = { createShelf(shelfNumber) })
                } else {
                    openExistShelf(shelf)
                }
            } else {
                createShelf(shelfNumber)
            }
        }
    }

    private fun openExistShelf(shelf: Shelf) {
        checkData.currentShelfIndex = shelves.value!!.indexOf(shelf)
        navigator.openGoodListScreen()
    }

    private fun createShelf(shelfNumber: Int) {
        // Сообщение - Начата обработка полки
        navigator.showShelfStarted(
                segmentNumber = segmentNumber.value!!,
                shelfNumber = shelfNumber.toString()) {
            checkData.addShelf(shelfNumber.toString())
            navigator.openGoodListScreen()
        }
    }

    fun onClickDelete() {
        selectionsHelper.let {
            val items = it.selectedPositions.value?.toMutableSet()
            if (items?.isEmpty() == true) {
                // Подтверждение - Удалить данные по сегменту? - Назад / Удалить
                navigator.showDeleteDataOnSegment(
                        storeNumber = checkData.getCurrentSegment()!!.storeNumber,
                        segmentNumber = segmentNumber.value!!) {
                    checkData.setCurrentSegmentStatus(SegmentStatus.DELETED)
                    navigator.openSegmentListScreen()
                }
            } else {
                items!!.forEach { index ->
                    it.revert(index)
                    checkData.setShelfStatusDeletedByIndex(index)
                    updateShelfList()
                }
            }
        }
    }

    fun onClickApply() {
        // Подтверждение - Сохранить результаты сканирования сегмента, закрыть его для редактирования и переслать? - Назад / Да
        navigator.showSaveSegmentScanResults(segmentNumber.value!!) {
            checkData.setCurrentSegmentStatus(SegmentStatus.PROCESSED)
            navigator.openSegmentListScreen()
        }
    }

    fun onClickBack() {
        if (checkData.getCurrentSegment()?.getStatus() != SegmentStatus.UNFINISHED) {
            navigator.goBack()
            return
        }

        if (shelves.value?.isEmpty() == true) {
            // Подтверждение - В сегменте отсутствуют полки для сохранения. Сегмент не будет сохранен - Назад / Подтвердить
            navigator.showNoShelvesInSegmentToSave(
                    segmentNumber = segmentNumber.value!!) {
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
