package com.lenta.bp9.features.shipment_control_cargo_units

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class ShipmentControlCargoUnitsViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

}
