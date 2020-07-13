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
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel экрана печати паллетной ведомости */
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
    lateinit var printDocumentsNetRequest: PrintDocumentsNetRequest

    val docsSelectionHelper = SelectionItemsHelper()

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val docList by unsafeLazy { MutableLiveData(listOf<DocumentsToPrintDocument>()) }

    val docsItemList by unsafeLazy {
        docList.switchMap { docList ->
            liveData {
                val docMappedList = docList.mapIndexed { index, doc ->
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
                        eoGeQuantity = docsListValue.size,
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
                    screenNavigator.showProgress(printDocumentsNetRequest)
                    val params = PrintDocumentsParams(
                            taskNum = taskManager.getTask().number,
                            docList = selectedDocs,
                            printerName = taskManager.getPrinterName()
                    )
                    val either = printDocumentsNetRequest(params)
                    either.either({ failure ->
                        screenNavigator.hideProgress()
                        screenNavigator.openAlertScreen(failure)
                    }, {
                        screenNavigator.hideProgress()
                        screenNavigator.openTaskEoMergePrintedDialog()
                    })
                }
            } ?: Logg.e {
                "List of selected docs is null"
            } ?: Logg.e {
                "docList is null"
            }
        }
    }

        override fun onOkInSoftKeyboard(): Boolean = true

    }
