package com.lenta.bp9.features.revise.invoice

import android.content.Context
import android.util.Log
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

class InvoiceReviseViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val initialTTNNumber: String by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.numberTTN ?: ""
    }

    val supplier: String by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.supplierName ?: ""
    }

    val inn: String by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.supplierINN ?: ""
    }

    val address: String by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.supplierAddress ?: ""
    }

    val initialTTNDate: String by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.dateTTN ?: ""
    }

    val productsQuantity: String by lazy {
        (taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.quantityPositions ?: 0).toString()
    }

    val quantityST: String by lazy {
        (taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.quantityST ?: 0).toString() + " шт."
    }

    val quantityKG: String by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.quantityKG.toStringFormatted() + " кг."
    }

    val quantityTotal: String by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getInvoiceInfo()?.quantityAll.toStringFormatted()
    }

    val editingAvailable: Boolean by lazy {
        taskManager.getReceivingTask()?.taskDescription?.isAlco != true
    }

    val notes: MutableLiveData<List<InvoiceNoteVM>> = MutableLiveData()

    val numberTTN: MutableLiveData<String> = MutableLiveData("")
    val days: MutableLiveData<String> = MutableLiveData("")
    val months: MutableLiveData<String> = MutableLiveData("")
    val years: MutableLiveData<String> = MutableLiveData("")

    val headerCheck: MutableLiveData<Boolean> = MutableLiveData(false)
    val supplierCheck: MutableLiveData<Boolean> = MutableLiveData(false)
    val detailsCheck: MutableLiveData<Boolean> = MutableLiveData(false)
    val nextPossible: MutableLiveData<Boolean> = combineLatest(headerCheck, supplierCheck, detailsCheck).map {
        it?.first == true && it?.second == true && it?.third == true
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onResume() {
        val notesData = taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.getInvoiceNotes()
        notesData?.let {
            notes.value = notesData.mapIndexed { index, note ->
                InvoiceNoteVM(notesData.size - index, note.lineText)
            }
        }
        numberTTN.value = initialTTNNumber
        val date = DateTimeUtil.getDateFromString(initialTTNDate, Constants.DATE_FORMAT_yyyy_mm_dd)
        days.value = DateTimeUtil.formatDate(date, Constants.DATE_FORMAT_dd)
        months.value = DateTimeUtil.formatDate(date, Constants.DATE_FORMAT_mm)
        years.value = DateTimeUtil.formatDate(date, Constants.DATE_FORMAT_yy)
    }

    fun onClickReject() {
        screenNavigator.openRejectScreen()
    }

    fun onClickEdit() {
        Logg.d { "Invoice details edit screen" }
    }

    fun onClickNext() {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.changeInvoiceStatus()
        screenNavigator.goBack()
    }

    private fun transformDate(): String {
        val fullString = (days.value ?: "") + "." + (months.value ?: "") + "." + (years.value ?: "")
        val date = DateTimeUtil.getDateFromString(fullString, Constants.DATE_FORMAT_ddmmyy)
        return DateTimeUtil.formatDate(date, Constants.DATE_FORMAT_yyyy_mm_dd)
    }

    fun onBackPressed() {
        val newDate = transformDate()
        if (newDate == initialTTNDate && numberTTN.value == initialTTNNumber) {
            screenNavigator.goBack()
        } else {
            screenNavigator.openConfirmationUnsavedData {
                screenNavigator.goBack()
            }
        }
    }

    // TODO: Implement the ViewModel
}

data class InvoiceNoteVM(
        val position: Int,
        val text: String
)