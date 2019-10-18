package com.lenta.bp9.features.formed_docs

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
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
        listDocs.postValue(listOf(FormedDocsItem(
                number = 1,
                name = "qwerty",
                supplyNumber = "274534",
                even = true
        )))
    }

    fun onClickPrint() {
        //todo
    }
}
