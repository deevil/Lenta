package com.lenta.bp9.features.formed_docs

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDocumentsPrinting
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class FormedDocsViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val listDocs: MutableLiveData<List<FormedDocsItem>> = MutableLiveData()
    val docsSelectionsHelper = SelectionItemsHelper()

    val enabledPrintButton: MutableLiveData<Boolean> = docsSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = docsSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onResume() {
        listDocs.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getDocumentsPrinting()?.getDocumentsPrinting()?.mapIndexed { index, taskDocumentsPrinting ->
                    FormedDocsItem(
                            number = index + 1,
                            name = taskDocumentsPrinting.outputTypeDoc,
                            supplyNumber = taskDocumentsPrinting.name,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
    }

    fun onClickPrint() {
        //todo
    }
}
