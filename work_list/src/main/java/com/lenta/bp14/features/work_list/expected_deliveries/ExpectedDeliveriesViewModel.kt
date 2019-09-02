package com.lenta.bp14.features.work_list.expected_deliveries

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch

class ExpectedDeliveriesViewModel : CoreViewModel() {

    val deliveries: MutableLiveData<List<DeliveriesUi>> = MutableLiveData()

    init {
        viewModelScope.launch {
            deliveries.value = getTestData()
        }
    }

    private fun getTestData(): List<DeliveriesUi>? {
        return List(100) {
            DeliveriesUi(
                    number = it,
                    status1 = "В пути",
                    status2 = "ПП",
                    quantity = "10 кор",
                    date = "01.08.19",
                    time = "12.15"
            )
        }
    }

    fun getTitle(): String {
        return "???"
    }

    fun onClickUpdate() {


    }

}

data class DeliveriesUi(
        val number: Int,
        val status1: String,
        val status2: String,
        val quantity: String,
        val date: String,
        val time: String
)
