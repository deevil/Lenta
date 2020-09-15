package com.lenta.bp9.features.editing_invoice

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.CommentToVP
import com.lenta.bp9.model.task.revise.CommentToVPRestData
import com.lenta.bp9.model.task.revise.InvoiceContentEntry
import com.lenta.bp9.model.task.revise.InvoiceContentEntryRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
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
    @Inject
    lateinit var context: Context

    val totalSelectionsHelper = SelectionItemsHelper()
    val delSelectionsHelper = SelectionItemsHelper()
    val addSelectionsHelper = SelectionItemsHelper()
    val notesSelectionsHelper = SelectionItemsHelper()
    val listNotes: MutableLiveData<List<NotesInvoiceItem>> = MutableLiveData()
    val notes: MutableLiveData<String> = MutableLiveData("")
    val filterTotal = MutableLiveData("")
    val filterDel = MutableLiveData("")
    val filterAdd = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val editingAvailable: Boolean by lazy {
        !(taskManager.getReceivingTask()?.taskDescription?.isAlco == true || taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.isEDO == true)
    }

    val listTotal by lazy {
        repoInMemoryHolder.invoiceContents.combineLatest(filterTotal).map { pair ->
            pair!!.first.filter { (it.isPresent || it.isAdded) && !it.isDeleted && it.matchesFilter(pair.second) }.mapIndexed { index, invoice ->
                EditingInvoiceItem(
                        number = index + 1,
                        name = "${invoice.getMaterialLastSix()} ${invoice.description}",
                        quantity = invoice.originalQuantity.toStringFormatted(),
                        uom = invoice.uom,
                        invoiceContent = invoice,
                        enabled = editingAvailable,
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
                        enabled = editingAvailable,
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
                        enabled = editingAvailable,
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
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
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
        launchUITryCatch {
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

    fun getDescription(): String {
        return if (editingAvailable) context.getString(R.string.delivery_note_correction) else context.getString(R.string.view_delivery_invoice)
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

    private val isClickSave: MutableLiveData<Boolean> = MutableLiveData(false) //чтобы при нажатии на Save и если был по какому-то товару превышен лимит, то будет вызыван диалог и выход из Save, после вызова диалога и нажатия Да опять вызывается Save, но уже при этом данный товар будет помечен как разрешенный на превышение количества
    fun onClickSave() {
        launchUITryCatch {
            isClickSave.value = true
            repoInMemoryHolder
                    .invoiceContents.value
                    ?.map {invoice ->
                        listTotal.value
                                ?.findLast {item ->
                                    item.invoiceContent.materialNumber == invoice.materialNumber
                                }
                                ?.let {
                                    if (isShownDialog.value == true) { //если при нажатии на Save был по какому-то товару превышен лимит и открылся диалог, то выходим из Save, если на диалоге будет нажата кнопка Да, то Save опять будет вызыван и во второй раз уже пойдет сохранение
                                        return@launchUITryCatch
                                    } else {
                                        invoice.originalQuantity = it.quantity.toDouble()
                                    }
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

            screenNavigator.showProgressLoadingData(::handleFailure)
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
                if (!it.isPresent) {
                    it.isAdded = true
                    it.originalQuantity = it.registeredQuantity
                    updateData()
                }
                return true
            }
            screenNavigator.openAlertGoodsNotInInvoiceScreen()
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
        requestFocusToNumberField.value = true
    }

    private val isShownDialog: MutableLiveData<Boolean> = MutableLiveData(false) //чтобы диалог не вызывался два раза при смене фокуса
    fun finishedInput(position: Int) {
        launchUITryCatch {
            if (isShownDialog.value == false) {
                val editInvoice = repoInMemoryHolder.invoiceContents.value!!.findLast {
                    it.materialNumber == listTotal.value!![position].invoiceContent.materialNumber
                }
                if (listTotal.value!![position].quantity.toDouble() > ((editInvoice?.quantityInOrder ?: 0.0) - (editInvoice?.registeredQuantity ?: 0.0))) {
                    isShownDialog.value = true
                    screenNavigator.openOrderQuantityEexceededDialog(
                            noCallbackFunc = {
                                isShownDialog.value = false
                                onNoClick(position, editInvoice?.originalQuantity.toStringFormatted())
                            },
                            yesCallbackFunc = {
                                isShownDialog.value = false
                                onYesClick(position, listTotal.value!![position].quantity.toDouble().toStringFormatted())
                            }
                    )
                }
            }
        }
    }

    private fun onNoClick(position: Int, origQuantity: String) {
        isClickSave.value = false
        listTotal.value!![position].quantity = origQuantity
    }

    private fun onYesClick(position: Int, newQuantity: String) {
        listTotal.value!![position].quantity = newQuantity
        if (isClickSave.value == true) {
            onClickSave()
        }
    }

}
