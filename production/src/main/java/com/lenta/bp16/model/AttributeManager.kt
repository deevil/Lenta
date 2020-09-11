package com.lenta.bp16.model

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class AttributeManager @Inject constructor(

): IAttributeManager {

    override val currentAttribute = MutableLiveData<AddAttributeProdInfo>()

    override fun updateCurrentAttribute(attribute: AddAttributeProdInfo?) {
        currentAttribute.value = attribute
    }
}