package com.lenta.bp16.model.managers

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.AddAttributeProdInfo

interface IAttributeManager {

    val currentAttribute: MutableLiveData<AddAttributeProdInfo>

    fun updateCurrentAttribute(attribute: AddAttributeProdInfo)

}