package com.lenta.bp9.features.editing_invoice

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.CommentToVP
import com.lenta.bp9.model.task.revise.CommentToVPRestData
import com.lenta.bp9.model.task.revise.InvoiceContentEntry
import com.lenta.bp9.model.task.revise.InvoiceContentEntryRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditingInvoiceViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var invoiceContentNetRequest: InvoiceContentNetRequest
    @Inject
    lateinit var invoiceContentSaveNetRequest: InvoiceContentSaveNetRequest
    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val selectedPage = MutableLiveData(0)
    val totalSelectionsHelper = SelectionItemsHelper()
    val delSelectionsHelper = SelectionItemsHelper()
    val addSelectionsHelper = SelectionItemsHelper()
    val notesSelectionsHelper = SelectionItemsHelper()
    val listNotes: MutableLiveData<List<NotesInvoiceItem>> = MutableLiveData()
    val notes: MutableLiveData<String> = MutableLiveData("")
    val filterTotal = MutableLiveData("")
    val filterDel = MutableLiveData("")
    val filterAdd = MutableLiveData("")

    val listTotal by lazy {
        repoInMemoryHolder.invoiceContents.combineLatest(filterTotal).map { pair ->
            pair!!.first.filter { (it.orderPositionNumber.isNotEmpty() || it.isAdded) && !it.isDeleted && it.matchesFilter(pair.second) }.mapIndexed { index, invoice ->
                EditingInvoiceItem(
                        number = index + 1,
                        name = "${invoice.getMaterialLastSix()} ${invoice.description}",
                        quantity = invoice.originalQuantity.toStringFormatted(),
                        uom = invoice.uom,
                        invoiceContent = invoice,
                        even = index % 2 == 0
                )
            }
        }
    }

    val listDelItem by lazy {
        repoInMemoryHolder.invoiceContents.combineLatest(filterDel).map { pair ->
            pair!!.first.filter { it.isDeleted && it.matchesFilter(pair.second) }.mapIndexed { index, invoice ->
                EditingInvoiceItem(
                        number = index + 1,
                        name = "${invoice.getMaterialLastSix()} ${invoice.description}",
                        quantity = invoice.originalQuantity.toStringFormatted(),
                        uom = invoice.uom,
                        invoiceContent = invoice,
                        even = index % 2 == 0
                )
            }
        }
    }

    val listAddItem by lazy {
        repoInMemoryHolder.invoiceContents.combineLatest(filterAdd).map { pair ->
            pair!!.first.filter { it.isAdded && !it.isDeleted && it.matchesFilter(pair.second) }.mapIndexed { index, invoice ->
                EditingInvoiceItem(
                        number = index + 1,
                        name = "${invoice.getMaterialLastSix()} ${invoice.description}",
                        quantity = invoice.originalQuantity.toStringFormatted(),
                        uom = invoice.uom,
                        invoiceContent = invoice,
                        even = index % 2 == 0
                )
            }
        }
    }

    val enabledRestoreDelBtn: MutableLiveData<Boolean> = selectedPage
            .combineLatest(totalSelectionsHelper.selectedPositions)
            .combineLatest(delSelectionsHelper.selectedPositions)
            .combineLatest(addSelectionsHelper.selectedPositions)
            .combineLatest(notesSelectionsHelper.selectedPositions)
            .map {
                val selectedTabPos = it?.first?.first?.first?.first ?: 0
                val totalSelected = it?.first?.first?.first?.second
                val delSelected = it?.first?.first?.second
                val addSelected = it?.first?.second
                val notesSelected = it?.second
                when (selectedTabPos) {
                    0 -> {
                        totalSelected?.isNotEmpty() ?: false
                    }
                    1 -> {
                        delSelected?.isNotEmpty() ?: false
                    }
                    2 -> {
                        addSelected?.isNotEmpty() ?: false
                    }
                    else -> notesSelected?.isNotEmpty() ?: false
                }
            }

    init {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            taskManager.getReceivingTask()?.let { task ->
                val params = InvoiceContentRequestParameters(
                        taskNumber = task.taskHeader.taskNumber
                )
                invoiceContentNetRequest(params).either(::handleFailure, ::handleSuccess)
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.hideProgress()
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: InvoiceContentRequestResult) {
        Logg.d { "invoiceContents server ${result.invoiceContents}" }
        Logg.d { "notes server ${result.notes}" }
        viewModelScope.launch {
            repoInMemoryHolder.invoiceContents.value = result.invoiceContents.map { InvoiceContentEntry.from(hyperHive, it) }
            listNotes.postValue(
                    result.notes.mapIndexed { index, commentToVPRestData ->
                        NotesInvoiceItem(
                                number = index + 1,
                                lineNumber = commentToVPRestData.lineNumber,
                                lineText = commentToVPRestData.lineText,
                                isDel = false,
                                even = index % 2 == 0
                        )
                    }.reversed()
            )
            updateData()
            screenNavigator.hideProgress()
        }
    }

    private fun updateData() {
        repoInMemoryHolder.invoiceContents.value = repoInMemoryHolder.invoiceContents.value

        val invoiceNotesSize = (listNotes.value?.size ?: 0) + 1
        val newListNotes: ArrayList<NotesInvoiceItem> = ArrayList()
        if (notes.value!!.isNotEmpty()) {
            newListNotes.add(
                    NotesInvoiceItem(
                            number = invoiceNotesSize,
                            lineNumber = "",
                            lineText = notes.value!!,
                            isDel = false,
                            even = invoiceNotesSize % 2 == 0
                    )
            )
        }
        listNotes.value?.filter {
            !it.isDel
        }?.mapIndexed { index, notesInvoiceItem ->
            NotesInvoiceItem(
                    number = index + 1,
                    lineNumber = notesInvoiceItem.lineNumber,
                    lineText = notesInvoiceItem.lineText,
                    isDel = false,
                    even = index % 2 == 0
            )
        }?.reversed()?.let {
            newListNotes.addAll(it)
        }
        listNotes.value = newListNotes
        notes.value = ""

        totalSelectionsHelper.clearPositions()
        delSelectionsHelper.clearPositions()
        addSelectionsHelper.clearPositions()
        notesSelectionsHelper.clearPositions()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onClickRefusal() {
        screenNavigator.openRejectScreen()
    }

    fun onClickDelete() {
        when (selectedPage.value) {
            0 -> {
                totalSelectionsHelper.selectedPositions.value?.map { position ->
                    repoInMemoryHolder.invoiceContents.value!!.findLast {
                        it.materialNumber == listTotal.value!![position].invoiceContent.materialNumber
                    }.let {
                        it!!.isDeleted = true
                    }
                }
            }
            2 -> {
                addSelectionsHelper.selectedPositions.value?.map { position ->
                    repoInMemoryHolder.invoiceContents.value!!.findLast {
                        it.materialNumber == listAddItem.value!![position].invoiceContent.materialNumber
                    }.let {
                        it!!.isDeleted = true
                    }
                }
            }
            3 -> {
                notesSelectionsHelper.selectedPositions.value?.map { position ->
                    listNotes.value!![position].isDel = true
                }
            }
        }
        updateData()
    }

    fun onClickRestore() {
        delSelectionsHelper.selectedPositions.value?.map { position ->
            repoInMemoryHolder.invoiceContents.value!!.findLast {
                it.materialNumber == listDelItem.value!![position].invoiceContent.materialNumber
            }.let {
                it!!.isDeleted = false
            }
        }
        updateData()
    }

    fun onClickSave() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()

            repoInMemoryHolder.invoiceContents.value!!.map {invoice ->
                listTotal.value?.findLast {item ->
                    item.invoiceContent.materialNumber == invoice.materialNumber
                }?.let {
                    invoice.originalQuantity = it.quantity.toDouble()
                }
            }

            val invoiceNotes: ArrayList<CommentToVP> = ArrayList()
            listNotes.value?.filter {notes ->
                !notes.isDel
            }?.map {
                invoiceNotes.add(
                        CommentToVP(
                                lineNumber = null,
                                lineText = it.lineText
                        )
                )
            }

            taskManager.getReceivingTask()?.let { task ->
                val params = InvoiceContentSaveRequestParameters(
                        taskNumber = task.taskHeader.taskNumber,
                        invoiceNumber = task.taskRepository.getReviseDocuments().getInvoiceInfo()?.numberTTN ?: "",
                        invoiceDate = task.taskRepository.getReviseDocuments().getInvoiceInfo()?.dateTTN ?: "",
                        invoiceContents = repoInMemoryHolder.invoiceContents.value?.map { InvoiceContentEntryRestData.from(it) },
                        notes = invoiceNotes.map { CommentToVPRestData.from(it) }
                )
                invoiceContentSaveNetRequest(params).either(::handleFailureSave, ::handleSuccessSave)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleFailureSave(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccessSave(result: InvoiceContentSaveRequestResult) {
        Logg.d { "InvoiceContents save  ${result}" }
        screenNavigator.goBack()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        var matnr = ""

        when (selectedPage.value) {
            0 -> matnr = filterTotal.value ?: ""
            1 -> matnr = filterDel.value ?: ""
            2 -> matnr = filterAdd.value ?: ""
        }

        if (selectedPage.value == 3) {
            updateData()
        } else {
            repoInMemoryHolder.invoiceContents.value!!.findLast {
                it.getMaterialLastSix() == if (matnr.length > 6) matnr.substring(matnr.length - 6) else matnr
            }?.let {
                it.isAdded = true
                it.originalQuantity = it.registeredQuantity
                updateData()
            }
        }

        return true
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onDigitPressed(digit: Int) {
        when (selectedPage.value) {
            0 -> filterTotal.value ?: "" + digit
            1 -> filterDel.value ?: "" + digit
            2 -> filterAdd.value ?: "" + digit
        }
    }

}
