package com.lenta.bp16.model.managers

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ingredients.ui.TechOrderDataInfoUI
import javax.inject.Inject

class TechOrderManager @Inject constructor(

): ITechOrderManager {

    override val currentTechOrder = MutableLiveData<List<TechOrderDataInfoUI>>()

    override fun updateCurrentTechOrder(techOrder: List<TechOrderDataInfoUI>) {
        currentTechOrder.value = techOrder
    }
}