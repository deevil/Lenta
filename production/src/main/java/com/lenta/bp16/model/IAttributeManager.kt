package com.lenta.bp16.model

import androidx.lifecycle.MutableLiveData

interface IAttributeManager {

    val currentAttribute: MutableLiveData<AddAttributeProdInfo>

    fun updateCurrentAttribute(attribute: AddAttributeProdInfo?)

}