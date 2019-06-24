package com.lenta.bp10.models

import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.shared.di.AppScope
import javax.inject.Inject

@AppScope
class StampsCollectorManager @Inject constructor() {

    private var setsStampCollector: StampCollector? = null

    private var componentsStampCollector: StampCollector? = null


    fun newStampCollector(processExciseAlcoProductService: ProcessExciseAlcoProductService) {
        setsStampCollector = StampCollector(
                processExciseAlcoProductService
        )
        componentsStampCollector = StampCollector(
                processExciseAlcoProductService
        )
    }

    fun clearAllStampsCollectors() {
        setsStampCollector?.clear()
        setsStampCollector = null
        componentsStampCollector?.clear()
        componentsStampCollector = null

    }


    fun clearComponentsStampCollector() {
        componentsStampCollector?.clear()
    }

    fun getComponentsStampCollector(): StampCollector? {
        return componentsStampCollector
    }

    fun getSetsStampCollector(): StampCollector? {
        return setsStampCollector
    }

    fun saveStampsToSet() {
        setsStampCollector?.addStampsFrom(componentsStampCollector)
    }

    fun add(materialNumber: String, setMaterialNumber: String, writeOffReason: String, isBadStamp: Boolean): Boolean {
        if (setsStampCollector!!.containsStamp(componentsStampCollector?.getPreparedStampCode()
                        ?: "")) {
            return false
        }
        return componentsStampCollector!!.add(materialNumber, setMaterialNumber, writeOffReason, isBadStamp)

    }
}