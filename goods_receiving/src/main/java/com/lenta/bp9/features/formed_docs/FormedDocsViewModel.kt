package com.lenta.bp9.features.formed_docs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDocumentsPrinting
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class FormedDocsViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var docsPrintingNetRequest: DocsPrintingNetRequest
    @Inject
    lateinit var printingDocsNetRequest: PrintingDocsNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo

    val listDocs: MutableLiveData<List<FormedDocsItem>> = MutableLiveData()
    val docsSelectionsHelper = SelectionItemsHelper()

    val enabledPrintButton: MutableLiveData<Boolean> = docsSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            taskManager.getReceivingTask()?.let { task ->
                val params = DocsPrintingParams(
                        taskNumber = task.taskHeader.taskNumber
                )
                docsPrintingNetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: DocsPrintingRestInfo) {
        taskManager.getReceivingTask()?.taskRepository?.getDocumentsPrinting()?.updateDocumentsPrinting(result.listDocumentsPrinting.map { TaskDocumentsPrinting.from(it) })
        updateDocs()
    }

    private fun updateDocs() {
        listDocs.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getDocumentsPrinting()?.getDocumentsPrinting()?.mapIndexed { index, taskDocumentsPrinting ->
                    FormedDocsItem(
                            number = index + 1,
                            name = taskDocumentsPrinting.outputTypeDoc,
                            supplyNumber = taskDocumentsPrinting.productDocNumber,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
    }

    fun onClickPrint() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            taskManager.getReceivingTask()?.let { task ->
                val params = PrintingDocsParams(
                        listDocumentsPrinting = taskManager.getReceivingTask()!!.taskRepository.getDocumentsPrinting().getDocumentsPrinting(),
                        printerName = sessionInfo.printer ?: ""
                )
                printingDocsNetRequest(params).either(::handleFailure) {
                    docsSelectionsHelper.clearPositions()
                    updateDocs()
                    screenNavigator.openInfoDocsSentPScreenrint()
                }
            }
            screenNavigator.hideProgress()
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }
}
