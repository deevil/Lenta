package com.lenta.bp10.models

import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.shared.di.AppScope
import javax.inject.Inject

@AppScope
class StampsCollectorManager @Inject constructor() {

    private var setsStampAlcoCollector: StampAlcoCollector? = null

    private var componentsStampAlcoCollector: StampAlcoCollector? = null


    fun newStampCollector(processExciseAlcoProductService: ProcessExciseAlcoProductService) {
        setsStampAlcoCollector = StampAlcoCollector(
                processExciseAlcoProductService
        )
        componentsStampAlcoCollector = StampAlcoCollector(
                processExciseAlcoProductService
        )
    }

    fun clearAllStampsCollectors() {
        setsStampAlcoCollector?.clear()
        componentsStampAlcoCollector?.clear()

    }


    fun clearComponentsStampCollector() {
        componentsStampAlcoCollector?.clear()
    }

    fun getComponentsStampCollector(): StampAlcoCollector? {
        return componentsStampAlcoCollector
    }

    fun getSetsStampCollector(): StampAlcoCollector? {
        return setsStampAlcoCollector
    }

    fun saveStampsToSet() {
        setsStampAlcoCollector?.moveStampsFrom(componentsStampAlcoCollector)
    }

    fun addStampToComponentsStampCollector(materialNumber: String, setMaterialNumber: String, writeOffReason: String, isBadStamp: Boolean): Boolean {
        if (setsStampAlcoCollector!!.containsStamp(componentsStampAlcoCollector?.getPreparedStampCode().orEmpty())) {
            return false
        }
        return componentsStampAlcoCollector!!.add(materialNumber, setMaterialNumber, writeOffReason, isBadStamp)

    }
}