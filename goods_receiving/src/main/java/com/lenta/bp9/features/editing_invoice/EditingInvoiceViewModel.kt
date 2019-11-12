package com.lenta.bp9.features.editing_invoice

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.CommentToVP
import com.lenta.bp9.model.task.revise.InvoiceContentEntry
import com.lenta.bp9.model.task.revise.InvoiceContentEntryRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.InvoiceContentNetRequest
import com.lenta.bp9.requests.network.InvoiceContentRequestParameters
import com.lenta.bp9.requests.network.InvoiceContentRequestResult
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
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val selectedPage = MutableLiveData(0)
    val totalSelectionsHelper = SelectionItemsHelper()
    val delSelectionsHelper = SelectionItemsHelper()
    val addSelectionsHelper = SelectionItemsHelper()
    val notesSelectionsHelper = SelectionItemsHelper()
    val listNotes: MutableLiveData<List<NotesInvoiceItem>> = MutableLiveData()
    private val invoiceNotes: MutableLiveData<List<CommentToVP>> = MutableLiveData()
    val notes: MutableLiveData<String> = MutableLiveData()
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
            pair!!.first.filter { it.isAdded && it.matchesFilter(pair.second) }.mapIndexed { index, invoice ->
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

    val visibilityDelBtn: MutableLiveData<Boolean> = selectedPage.map {
        when (it) {
            0 -> {
                false //totalSelectionsHelper.selectedPositions.value.isNullOrEmpty()
            }
            1 -> {
                delSelectionsHelper.selectedPositions.value.isNullOrEmpty()
            }
            2 -> {
                addSelectionsHelper.selectedPositions.value.isNullOrEmpty()
            }
            else -> notesSelectionsHelper.selectedPositions.value.isNullOrEmpty()
        }
    }

    val enabledDelBtn: MutableLiveData<Boolean> = totalSelectionsHelper.selectedPositions.
            combineLatest(delSelectionsHelper.selectedPositions).
            combineLatest(addSelectionsHelper.selectedPositions).
            combineLatest(notesSelectionsHelper.selectedPositions).map {
        val totalSelected = it?.first
        val delSelected = it?.first?.first
        val addSelected = it?.first?.first?.first
        val notesSelected = it?.first?.first?.first
        true
    }

    val enabledRestoreBtn: MutableLiveData<Boolean> = totalSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = totalSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
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
        Logg.d { "testddi invoiceContents ${result.invoiceContents}" }
        Logg.d { "testddi notes ${result.notes}" }
        viewModelScope.launch {
            repoInMemoryHolder.invoiceContents.value = result.invoiceContents.map {InvoiceContentEntry.from(hyperHive,it)}
            invoiceNotes.value =result.notes.map { CommentToVP.from(it) }
            updateData()
            screenNavigator.hideProgress()
        }
    }

    private fun updateData() {
        repoInMemoryHolder.invoiceContents.value = repoInMemoryHolder.invoiceContents.value //invoiceContents.value

        listNotes.postValue(
                invoiceNotes.value?.mapIndexed { index, commentToVP ->
                    NotesInvoiceItem(
                            number = index + 1,
                            lineNumber = "${commentToVP.lineNumber}",
                            lineText = commentToVP.lineText,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
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
        //todo
        return
    }

    override fun onOkInSoftKeyboard(): Boolean {
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
