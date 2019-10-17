package com.lenta.bp9.features.transfer_goods_section

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class TransferGoodsSectionViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val listSection: MutableLiveData<List<TransferGoodsSectionItem>> = MutableLiveData()
    val selectedPage = MutableLiveData(0)

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        return
    }

    fun onClickSave() {
        return
    }

}
