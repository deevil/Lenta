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
    @Inject
    lateinit var personnelNumberNetRequest: PersonnelNumberNetRequest

    val listSections: MutableLiveData<List<TransferGoodsSectionItem>> = MutableLiveData()
    private val tmpPersonnelNumberInfo: ArrayList<SectionPersonnelNumberInfo> = ArrayList()
    private val listPersonnelNumberInfo: MutableLiveData<List<SectionPersonnelNumberInfo>> = MutableLiveData()
    val selectedPage = MutableLiveData(0)

    private fun updateTransmitted() {
        listSections.postValue(
                taskManager.getReceivingTask()?.getProcessedSections()?.filter {
                    it.personnelNumber.isNullOrEmpty()
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
                    it.personnelNumber.isNotEmpty()
                }?.mapIndexed { index, taskSectionInfo ->
                    TransferGoodsSectionItem(
                            number = index + 1,
                            condition = taskSectionInfo.sectionNumber,
                            representative = getFio(taskSectionInfo.personnelNumber),
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
        updatePersonnelNumberInfo()
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
        //todo
        return
    }

    private fun updatePersonnelNumberInfo() {
        viewModelScope.launch {
            screenNavigator.showProgress(personnelNumberNetRequest)
            tmpPersonnelNumberInfo.clear()
            taskManager.getReceivingTask()?.getProcessedSections()?.map {
                addPersonnelNumberInfo(it)
            }
            listPersonnelNumberInfo.postValue(tmpPersonnelNumberInfo)
            screenNavigator.hideProgress()
        }
    }

    private suspend fun addPersonnelNumberInfo(sectionInfo: TaskSectionInfo) {
        personnelNumberNetRequest(TabNumberParams(tabNumber = sectionInfo.personnelNumber)).either({
            ""
        }, {
            tmpPersonnelNumberInfo.add(SectionPersonnelNumberInfo(
                    sectionNumber = sectionInfo.sectionNumber,
                    personnelNumber = sectionInfo.personnelNumber,
                    fio = it.name.toString()
            ))
        })
    }

    private fun getFio(personnelNumber: String) : String {
        return listPersonnelNumberInfo.value?.first {
            it.personnelNumber == personnelNumber
        }?.fio ?: ""
    }

}
