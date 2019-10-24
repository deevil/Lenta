package com.lenta.bp9.features.list_goods_transfer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
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
        return sectionInfo.value!!.sectionNumber
    }

    init {
        viewModelScope.launch {
            listGoods.postValue(
                    taskManager.getReceivingTask()?.taskRepository?.getSections()?.findSectionProductsOfSection(sectionInfo.value!!)?.mapIndexed { index, taskSectionProducts ->
                        val productInfo = taskManager.getReceivingTask()?.taskRepository?.getProducts()?.findProduct(taskSectionProducts.materialNumber)
                        ListGoodsTransferItem(
                                number = index + 1,
                                name = "${productInfo?.getMaterialLastSix()} ${productInfo?.description}",
                                quantity = productInfo?.let {
                                    taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo).toString()
                                } ?: "",
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
