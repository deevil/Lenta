package com.lenta.bp9.features.transfer_goods_section

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class TransferGoodsSectionViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val listSections: MutableLiveData<List<TransferGoodsSectionItem>> = MutableLiveData()
    val selectedPage = MutableLiveData(0)

    fun onResume() {
        if (selectedPage.value == 0) {
            updateTransmitted()
        } else {
            updateTransferred()
        }
    }

    private fun updateTransmitted() {
        listSections.postValue(
                taskManager.getReceivingTask()?.getProcessedSections()?.filter {
                    it.personnelNumber == "00000000"
                }?.mapIndexed { index, taskSectionInfo ->
                    TransferGoodsSectionItem(
                            number = index + 1,
                            condition = taskSectionInfo.sectionNumber,
                            representative = "",
                            ofGoods = taskSectionInfo.quantitySectionProducts,
                            sectionInfo = taskSectionInfo,
                            even = index % 2 == 0)
                }?.reversed())
    }

    private fun updateTransferred() {
        listSections.postValue(
                taskManager.getReceivingTask()?.getProcessedSections()?.filter {
                    it.personnelNumber != "00000000"
                }?.mapIndexed { index, taskSectionInfo ->
                    TransferGoodsSectionItem(
                            number = index + 1,
                            condition = taskSectionInfo.sectionNumber,
                            representative = taskSectionInfo.personnelNumber,
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
        onResume()
    }

    fun onClickItemPosition(position: Int) {
        screenNavigator.openListGoodsTransferScreen(listSections.value!![position].sectionInfo)
    }

    fun onClickSave() {
        return
    }

}
