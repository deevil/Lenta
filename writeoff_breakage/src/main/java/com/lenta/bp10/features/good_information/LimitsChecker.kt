package com.lenta.bp10.features.good_information

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LimitsChecker(private val limit: Double, private val observer: () -> Unit, private val countLiveData: LiveData<Double>, private val viewModelScope: () -> CoroutineScope) {

    fun check() {
        val value = countLiveData.value ?: 0.0
        if (limit != 0.0 && value > limit) {
            viewModelScope().launch {
                delay(200)
                observer()
            }
        }

    }
}



