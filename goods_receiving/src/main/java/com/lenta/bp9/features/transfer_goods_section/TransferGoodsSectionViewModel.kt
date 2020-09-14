package com.lenta.bp9.features.transfer_goods_section

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class TransferGoodsSectionViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val listSections: MutableLiveData<List<TransferGoodsSectionItem>> = MutableLiveData()
    val enabledBtnSave by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskRepository?.getSections()?.getSignModification() == true)
    }

    private fun updateTransmitted() {
        listSections.postValue(
                taskManager.getReceivingTask()?.getProcessedSections()?.filter {
                    it.personnelNumber.isEmpty()
                }?.mapIndexed { index, taskSectionInfo ->
                    TransferGoodsSectionItem(
                            number = (index + 1).toString(),
                            condition = "${taskSectionInfo.sectionNumber}-${taskSectionInfo.sectionName}",
                            representative = "",
                            ofGoods = taskSectionInfo.quantitySectionProducts,
                            sectionInfo = taskSectionInfo,
                            even = index % 2 == 0)
                }?.reversed())
    }

    private fun updateTransferred() {
        listSections.postValue(
                taskManager.getReceivingTask()?.getProcessedSections()?.filter {
                    it.personnelNumber.isNotEmpty()
                }?.mapIndexed { index, taskSectionInfo ->
                    TransferGoodsSectionItem(
                            number = (index + 1).toString(),
                            condition = "${taskSectionInfo.sectionNumber}-${taskSectionInfo.sectionName}",
                            representative = taskSectionInfo.employeeName,
                            ofGoods = taskSectionInfo.quantitySectionProducts,
                            sectionInfo = taskSectionInfo,
                            even = index % 2 == 0)
                }?.reversed())
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
        if (selectedPage.value == 0) {
            updateTransmitted()
        } else {
            updateTransferred()
        }
    }

    fun onClickItemPosition(position: Int) {
        screenNavigator.openListGoodsTransferScreen(listSections.value!![position].sectionInfo)
    }

    fun onClickSave() {
        screenNavigator.openSubmittedLoadingScreen()
    }

    fun onBackPressed() {
        if (enabledBtnSave.value == true) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        screenNavigator.openUnlockTaskLoadingScreen()
                    }
            )
        } else {
            screenNavigator.openUnlockTaskLoadingScreen()
        }
    }

    fun onResume() {
        moveToPreviousPageIfNeeded()
    }

    private fun moveToPreviousPageIfNeeded() {
        val isNotEmptyTransmitted = taskManager.getReceivingTask()?.getProcessedSections()?.filter {sectionInfo ->
            sectionInfo.personnelNumber.isEmpty()
        }?.size ?: 0 > 0
        selectedPage.value = if (isNotEmptyTransmitted) 0 else 1
    }

}
