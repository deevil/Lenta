package com.lenta.bp14.features.work_list.expected_deliveries

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.work_list.Delivery
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.getFormattedDate
import com.lenta.shared.utilities.extentions.getFormattedTime
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExpectedDeliveriesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: WorkListTask


    val title = MutableLiveData<String>("")

    val deliveries: MutableLiveData<List<DeliveriesUi>> by lazy {
        task.currentGood.value?.deliveries!!.map { list: List<Delivery>? ->
            list?.mapIndexed { index, delivery ->
                DeliveriesUi(
                       position = (index + 1).toString(),
                        status = delivery.status.description,
                        info = delivery.info,
                        quantity = delivery.getQuantityWithUnits(),
                        date = delivery.date.getFormattedDate(),
                        time = delivery.date.getFormattedTime()
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            title.value = task.currentGood.value?.getFormattedMaterialWithName()
            onClickUpdate()
        }
    }

    fun onClickUpdate() {
        viewModelScope.launch {
            task.loadDeliveries()
        }
    }

}

data class DeliveriesUi(
        val position: String,
        val status: String,
        val info: String,
        val quantity: String,
        val date: String,
        val time: String
)
