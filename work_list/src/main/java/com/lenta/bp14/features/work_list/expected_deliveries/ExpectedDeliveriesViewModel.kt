package com.lenta.bp14.features.work_list.expected_deliveries

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.work_list.Delivery
import com.lenta.bp14.models.work_list.IWorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.work_list.ExpectedDeliveriesParams
import com.lenta.bp14.requests.work_list.ExpectedDeliveriesResult
import com.lenta.bp14.requests.work_list.IExpectedDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import javax.inject.Inject

class ExpectedDeliveriesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var task: IWorkListTask

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var expectedDeliveriesNetRequest: IExpectedDeliveriesNetRequest


    val title = MutableLiveData("")

    val deliveries: MutableLiveData<List<DeliveriesUi>> by lazy {
        task.currentGood.value?.deliveries!!.map { list: List<Delivery>? ->
            list?.mapIndexed { index, delivery ->
                DeliveriesUi(
                        position = (index + 1).toString(),
                        status = delivery.status,
                        info = delivery.type,
                        quantity = "${delivery.quantity.dropZeros()} ${task.currentGood.value!!.units.name}",
                        date = delivery.date.getFormattedDate(),
                        time = delivery.date.getFormattedTime()
                )
            }
        }
    }

    init {
        launchUITryCatch {
            title.value = task.currentGood.value?.getFormattedMaterialWithName()
            onClickUpdate()
        }
    }

    fun onClickUpdate() {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            expectedDeliveriesNetRequest(
                    ExpectedDeliveriesParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            material = task.currentGood.value?.material.orEmpty()
                    )
            ).either(::handleFailure, ::updateDeliveries)
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.hideProgress()
        navigator.openAlertScreen(failure)
    }

    private fun updateDeliveries(result: ExpectedDeliveriesResult) {
        Logg.d { "ExpectedDeliveriesResult: $result" }
        launchUITryCatch {
            task.currentGood.value?.deliveries?.value = result.deliveries.map { delivery ->
                Delivery(
                        status = delivery.status,
                        type = delivery.type,
                        quantity = delivery.quantityInOrder,
                        date = "${delivery.date}_${delivery.time}".getDate(Constants.DATE_TIME_ONE)
                )
            }
            navigator.hideProgress()
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
