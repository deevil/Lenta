package com.lenta.bp9.features.transport_marriage.goods_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.goods_details.GoodsDetailsCategoriesItem
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

class TransportMarriageGoodsDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var dataBase: IDataBaseRepo

    val cargoUnitNumber: MutableLiveData<String> = MutableLiveData()
    val materialNumber: MutableLiveData<String> = MutableLiveData()
    val materialName: MutableLiveData<String> = MutableLiveData()
    val goodsDetails: MutableLiveData<List<GoodsDetailsCategoriesItem>> = MutableLiveData()
    private val discrepanciesName: MutableLiveData<String> = MutableLiveData()

    fun getTitle() : String {
        val materialLastSix = materialNumber.value?.let {
            if (it.length > 6)
                it.substring(it.length - 6)
            else
                it
        }
        return "$materialLastSix ${materialName.value}"
    }

    init {
        launchUITryCatch {
            dataBase.getQualityInfoTransportMarriage()?.map {
                discrepanciesName.value = it.name
                return@map
            }
            goodsDetails.postValue(
                    taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.getTransportMarriage()?.filter {itFilter ->
                        itFilter.cargoUnitNumber == cargoUnitNumber.value && itFilter.materialNumber == materialNumber.value
                    }?.mapIndexed { index, taskTransportMarriageInfo ->
                        GoodsDetailsCategoriesItem(
                                number = index + 1,
                                name = discrepanciesName.value ?: "",
                                nameBatch = "",
                                visibilityNameBatch = false,
                                quantityWithUom = "${taskTransportMarriageInfo.quantity.toStringFormatted()} ${taskTransportMarriageInfo.uom.name}",
                                isNormDiscrepancies = false,
                                typeDiscrepancies = "",
                                materialNumber = materialNumber.value ?: "",
                                batchDiscrepancies = null,
                                even = index % 2 == 0
                        )
                    }?.reversed()
            )
        }

    }
}
