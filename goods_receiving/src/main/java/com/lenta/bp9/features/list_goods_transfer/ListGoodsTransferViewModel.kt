package com.lenta.bp9.features.list_goods_transfer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListGoodsTransferViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val sectionInfo: MutableLiveData<TaskSectionInfo> = MutableLiveData()
    val listGoods: MutableLiveData<List<ListGoodsTransferItem>> = MutableLiveData()

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun getDescription(): String {
        return "${sectionInfo.value!!.sectionNumber}-${sectionInfo.value!!.sectionName}"
    }

    init {
        viewModelScope.launch {
            listGoods.postValue(
                    taskManager.getReceivingTask()?.taskRepository?.getSections()?.findSectionProductsOfSection(sectionInfo.value!!)?.mapIndexed { index, taskSectionProducts ->
                        ListGoodsTransferItem(
                                number = index + 1,
                                name = "${taskSectionProducts.materialNumber.takeLast(6)} ${taskSectionProducts.materialName}",
                                quantity = "${taskSectionProducts.quantity.toStringFormatted()} ${taskSectionProducts.uom.name}",
                                even = index % 2 == 0
                        )
                    }?.reversed()
            )
        }
    }

    fun onClickNext() {
        screenNavigator.openRepresPersonNumEntryScreen(sectionInfo.value!!)
    }
}
