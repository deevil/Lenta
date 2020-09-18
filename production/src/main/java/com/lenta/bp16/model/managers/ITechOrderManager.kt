package com.lenta.bp16.model.managers

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ingredients.ui.TechOrderDataInfoUI

interface ITechOrderManager {

    val currentTechOrder: MutableLiveData<List<TechOrderDataInfoUI>>

    fun updateCurrentTechOrder(techOrder: List<TechOrderDataInfoUI>)

}