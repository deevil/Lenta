package com.lenta.bp9.features.transfer_goods_section

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.requests.network.PersonnelNumberNetRequest
import com.lenta.shared.requests.network.TabNumberParams
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TransferGoodsSectionViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val listSections: MutableLiveData<List<TransferGoodsSectionItem>> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val enabledBtnSave: MutableLiveData<Boolean> = listSections.map {
        taskManager.getReceivingTask()?.getProcessedSections()?.filter {sectionInfo ->
            sectionInfo.personnelNumber.isNotEmpty()
        }?.size ?: 0 > 0
    }

    private fun updateTransmitted() {
        listSections.postValue(
                taskManager.getReceivingTask()?.getProcessedSections()?.filter {
                    it.personnelNumber.isNullOrEmpty()
                }?.mapIndexed { index, taskSectionInfo ->
                    TransferGoodsSectionItem(
                            number = index + 1,
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
                            number = index + 1,
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
        screenNavigator.openUnsavedDataDialog(
                yesCallbackFunc = {
                    screenNavigator.openUnlockTaskLoadingScreen()
                }
        )
    }

    fun onResume() {
        moveToPreviousPageIfNeeded()
    }

    private fun moveToPreviousPageIfNeeded() {
        if (selectedPage.value == 0) {
            val isNotEmptyTransferred = taskManager.getReceivingTask()?.getProcessedSections()?.filter {sectionInfo ->
                sectionInfo.personnelNumber.isNotEmpty()
            }?.size ?: 0 > 0
            selectedPage.value = if (isNotEmptyTransferred) 1 else 0
        }
    }

}
