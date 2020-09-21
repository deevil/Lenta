package com.lenta.bp16.model.managers

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.AddAttributeProdInfo
import javax.inject.Inject

class AttributeManager @Inject constructor(

): IAttributeManager {

    override val currentAttribute = MutableLiveData<AddAttributeProdInfo>()

    override fun updateCurrentAttribute(attribute: AddAttributeProdInfo) {
        currentAttribute.value = attribute
    }
}