package com.lenta.movement.features.task.eo.formedDocs

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.requests.network.PrintDocumentsNetRequest
import com.lenta.movement.requests.network.models.documentsToPrint.DocumentsToPrintDocument
import com.lenta.movement.requests.network.models.printDocuments.PrintDocumentsParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskEOMergeFormedDocsViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var formatter: IFormatter

    @Inject
    lateinit var printDocumetnsNetRequest: PrintDocumentsNetRequest

    val docsSelectionHelper = SelectionItemsHelper()

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val docList by unsafeLazy { MutableLiveData(listOf<DocumentsToPrintDocument>()) }

    val docsItemList by unsafeLazy {
        docList.switchMap { docList ->
            liveData {
                val docMappedList = docList.mapIndexed { index, doc ->
                    val number = index + 1
                    SimpleListItem(
                            number = index + 1,
                            title = doc.docName.orEmpty(),
                            countWithUom = "",
                            isClickable = true)
                }
                emit(docMappedList)
            }
        }
    }

    fun getTitle(): String {
        return "${taskManager.getTask().taskType.shortName} // ${taskManager.getTask().name}"
    }

    fun onBackPressed() {
        screenNavigator.goBack()
    }

    fun onPrintBtnClick() {
        viewModelScope.launch {
            docList.value?.let { docsListValue ->
                screenNavigator.openTaskEoMergePrintConfirmationDialog(
                        docsListValue.size,
                        yesCallbackFunc = {
                            printDocs()
                        }
                )
            }

        }
    }

    private fun printDocs() {
        docList.value?.let { docListValue ->
            docsSelectionHelper.selectedPositions.value?.let { setOfSelected ->
                val selectedDocs = if (setOfSelected.isNotEmpty()) {
                    setOfSelected.mapTo(mutableListOf()) {
                        docListValue[it]
                    }
                } else {
                    docListValue
                }
                viewModelScope.launch {
                    screenNavigator.showProgress(printDocumetnsNetRequest)
                    val params = PrintDocumentsParams(
                            taskNum = taskManager.getTask().number,
                            docList = selectedDocs,
                            printerName = taskManager.getPrinterName()
                    )
                    val either = printDocumetnsNetRequest(params)
                    either.either({ failure ->
                        screenNavigator.hideProgress()
                        screenNavigator.openAlertScreen(failure)
                    }, {
                        screenNavigator.hideProgress()
                        screenNavigator.openTaskEoMergePrintedDialog()
                    })
                }
            } ?: Logg.e {
                "Список документов пуст"
            }
        }
    }

    override fun onOkInSoftKeyboard(): Boolean = true

}
