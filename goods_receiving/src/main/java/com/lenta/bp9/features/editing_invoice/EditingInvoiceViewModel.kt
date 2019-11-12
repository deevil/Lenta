package com.lenta.bp9.features.editing_invoice

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.CommentToVP
import com.lenta.bp9.model.task.revise.InvoiceContentEntry
import com.lenta.bp9.model.task.revise.InvoiceContentEntryRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.InvoiceContentNetRequest
import com.lenta.bp9.requests.network.InvoiceContentRequestParameters
import com.lenta.bp9.requests.network.InvoiceContentRequestResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
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

    val selectedPage = MutableLiveData(0)
    val totalSelectionsHelper = SelectionItemsHelper()
    val delSelectionsHelper = SelectionItemsHelper()
    val addSelectionsHelper = SelectionItemsHelper()
    val notesSelectionsHelper = SelectionItemsHelper()
    val restoreSelectionsHelper = SelectionItemsHelper()
    val listTotal: MutableLiveData<List<EditingInvoiceItem>> = MutableLiveData()
    val listDelItem: MutableLiveData<List<EditingInvoiceItem>> = MutableLiveData()
    val listAddItem: MutableLiveData<List<EditingInvoiceItem>> = MutableLiveData()
    val listNotes: MutableLiveData<List<NotesInvoiceItem>> = MutableLiveData()
    private val invoiceContents: MutableLiveData<List<InvoiceContentEntry>> = MutableLiveData()
    private val invoiceNotes: MutableLiveData<List<CommentToVP>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val visibilityDelBtn: MutableLiveData<Boolean> = selectedPage.map {
        it == 0
    }

    val enabledDelBtn: MutableLiveData<Boolean> = totalSelectionsHelper.selectedPositions.map {
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
            invoiceContents.value = result.invoiceContents.map {InvoiceContentEntry.from(hyperHive,it)}
            invoiceNotes.value =result.notes.map { CommentToVP.from(it) }
            updateData()
            screenNavigator.hideProgress()
        }
    }

    private fun updateData() {
        listTotal.postValue(
                invoiceContents.value?.mapIndexed { index, invoice ->
                    EditingInvoiceItem(
                            number = index + 1,
                            name = "${invoice.getMaterialLastSix()} ${invoice.description}",
                            quantity = invoice.originalQuantity.toStringFormatted(),
                            uom = invoice.uom,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )

        listDelItem.postValue(
                invoiceContents.value?.filter {
                    it.isDeleted
                }?.mapIndexed { index, invoice ->
                    EditingInvoiceItem(
                            number = index + 1,
                            name = "${invoice.getMaterialLastSix()} ${invoice.description}",
                            quantity = invoice.originalQuantity.toStringFormatted(),
                            uom = invoice.uom,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )

        listAddItem.postValue(
                invoiceContents.value?.filter {
                    it.isAdded
                }?.mapIndexed { index, invoice ->
                    EditingInvoiceItem(
                            number = index + 1,
                            name = "${invoice.getMaterialLastSix()} ${invoice.description}",
                            quantity = invoice.originalQuantity.toStringFormatted(),
                            uom = invoice.uom,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )

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
                    invoiceContents.value!![invoiceContents.value!!.size - position - 1].isDeleted =  true
                }
            }
        }
        updateData()
    }

    fun onClickRestore() {
        //todo
        Logg.d { "testddi onClickRestore" }
    }

    fun onClickSave() {
        //todo
        return
    }

    override fun onOkInSoftKeyboard(): Boolean {
        /**eanCode.value?.let {
            searchProductDelegate.searchCode(it, fromScan = false)
        }*/
        return true
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

}
