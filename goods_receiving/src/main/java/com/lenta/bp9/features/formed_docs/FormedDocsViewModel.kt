package com.lenta.bp9.features.formed_docs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDocumentsPrinting
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.DocsPrintingNetRequest
import com.lenta.bp9.requests.network.DocsPrintingParams
import com.lenta.bp9.requests.network.DocsPrintingRestInfo
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

    val listDocs: MutableLiveData<List<FormedDocsItem>> = MutableLiveData()
    val docsSelectionsHelper = SelectionItemsHelper()

    val enabledPrintButton: MutableLiveData<Boolean> = docsSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = docsSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
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
        //todo
    }
}
