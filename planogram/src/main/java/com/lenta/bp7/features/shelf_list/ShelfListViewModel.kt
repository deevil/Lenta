package com.lenta.bp7.features.shelf_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp7.data.model.SegmentStatus
import com.lenta.bp7.data.model.Shelf
import com.lenta.bp7.data.model.ShelfStatus
import com.lenta.bp7.features.other.SendDataViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map

class ShelfListViewModel : SendDataViewModel(), OnOkInSoftKeyboardListener {

    val selectionsHelper = SelectionItemsHelper()

    val shelves: MutableLiveData<List<Shelf>> = MutableLiveData()

    val segmentNumber: MutableLiveData<String> = MutableLiveData()
    val shelfNumber: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToShelfNumber: MutableLiveData<Boolean> = MutableLiveData(true)

    val numberFieldEnabled: MutableLiveData<Boolean> = MutableLiveData(false)

    val deleteShelfButtonEnabled: MutableLiveData<Boolean> = selectionsHelper.selectedPositions.map {
        val segmentIsNotDeleted = checkData.getCurrentSegment()?.getStatus() != SegmentStatus.DELETED
        val notDeletedShelveSelected = selectionsHelper.selectedPositions.value?.find { index ->
            shelves.value?.get(index)?.getStatus() != ShelfStatus.DELETED
        } != null

        segmentIsNotDeleted && notDeletedShelveSelected
    }

    val deleteSegmentButtonEnabled: MutableLiveData<Boolean> = shelves.map { shelves ->
        shelves?.isNotEmpty() ?: false && checkData.getCurrentSegment()?.getStatus() != SegmentStatus.DELETED
    }

    val applyButtonEnabled: MutableLiveData<Boolean> = shelves.map {
        it?.isNotEmpty() ?: false && checkData.getCurrentSegment()?.getStatus() == SegmentStatus.UNFINISHED &&
                it?.find { shelf -> shelf.getStatus() == ShelfStatus.PROCESSED } != null
    }

    init {
        launchUITryCatch {
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
        val shelfNumber = shelfNumber.value?.toIntOrNull() ?: 0
        if (shelfNumber > 0) {
            val shelf = shelves.value?.find { (it.number.toIntOrNull() ?: 0) == shelfNumber }
            if (shelf != null) {
                if (shelf.getStatus() == ShelfStatus.DELETED) {
                    // Выбор - Полка удалена. Открыть просмотр или создать новую? - Назад / Просмотр / Создать
                    navigator.showShelfIsDeleted(
                            reviewCallback = { openExistShelf(shelf) },
                            createCallback = {
                                checkData.currentShelfIndex = shelves.value!!.indexOf(shelf)
                                checkData.setCurrentShelfStatus(ShelfStatus.UNFINISHED)
                                checkData.getCurrentShelf()?.clearGoodsList()

                                // Сообщение - Начата обработка полки
                                navigator.showShelfStarted(
                                        segmentNumber = segmentNumber.value!!,
                                        shelfNumber = shelfNumber.toString()) {
                                    navigator.openGoodListScreen()
                                }
                            })
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

    fun onClickDeleteShelf() {
        selectionsHelper.let {
            val items = it.selectedPositions.value?.toMutableSet()

            var shelfNumbers = ""
            items!!.forEach { index ->
                shelfNumbers += checkData.getCurrentSegment()?.shelves?.get(index)?.number
                if (items.size > 1 && index != items.size - 1) {
                    shelfNumbers += ", "
                }
            }

            // Подтверждение - Удалить данные полки №...? - Назад / Удалить
            navigator.showDeleteShelfData(shelfNumbers) {
                items.forEach { index ->
                    it.revert(index)
                    checkData.setShelfStatusDeletedByIndex(index)
                    updateShelfList()
                }
            }
        }
    }

    fun onClickDeleteSegment() {
        // Подтверждение - Удалить данные по сегменту? - Назад / Удалить
        navigator.showDeleteDataOnSegment(
                storeNumber = checkData.getCurrentSegment()!!.storeNumber,
                segmentNumber = segmentNumber.value!!) {
            checkData.setCurrentSegmentStatus(SegmentStatus.DELETED)
            navigator.openSegmentListScreen()
        }
    }

    fun onClickApply() {
        // Подтверждение - Сохранить результаты сканирования сегмента, закрыть его для редактирования и переслать? - Назад / Да
        navigator.showSaveSegmentScanResults(segmentNumber.value!!) {
            checkData.setCurrentSegmentStatus(SegmentStatus.PROCESSED)
            sendCheckResult()
        }
    }

    fun onClickBack() {
        if (checkData.getCurrentSegment()?.getStatus() != SegmentStatus.UNFINISHED) {
            navigator.openSegmentListScreen()
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
