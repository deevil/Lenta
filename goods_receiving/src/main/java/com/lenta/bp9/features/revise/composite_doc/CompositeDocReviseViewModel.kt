package com.lenta.bp9.features.revise.composite_doc

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.DeliveryDocumentRevise
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class CompositeDocReviseViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val document: MutableLiveData<DeliveryDocumentRevise> = MutableLiveData()

    val listComplexDoc: MutableLiveData<List<ComplexDocVM>> = MutableLiveData()

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val enabledApplyBtn = listComplexDoc.map {conditions ->
        conditions?.findLast { it.isCheck.value == false } == null
    }

    init {
        launchUITryCatch {
            listComplexDoc.postValue(
                    taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getComplexDocuments()?.filter {
                        it.documentID == document.value?.documentID
                    }?.mapIndexed { index, complexDocRevise ->
                        ComplexDocVM(
                                number = index + 1,
                                documentID = complexDocRevise.documentID,
                                conditionID = complexDocRevise.conditionID,
                                conditionName = complexDocRevise.conditionName,
                                isCheck = MutableLiveData(complexDocRevise.isCheck)
                        )
                    }?.reversed()
            )
        }
    }


    @Suppress("UNUSED_PARAMETER")
    fun checkedChanged(position: Int, checked: Boolean) {
        listComplexDoc.value = listComplexDoc.value
    }

    fun onClickRefusal() {
        screenNavigator.openRejectScreen()
    }

    fun onClickApply() {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.setVerifiedCompositeDocument(document.value?.documentID ?: "")
        screenNavigator.goBack()
    }
}

data class ComplexDocVM(
        val number: Int,
        val documentID: String,
        val conditionID: String,
        val conditionName: String,
        val isCheck: MutableLiveData<Boolean>
)
