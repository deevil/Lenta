package com.lenta.bp10.models

import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.shared.di.AppScope
import javax.inject.Inject

@AppScope
class StampsCollectorManager @Inject constructor() {

    private var setsAlcoholStampCollector: AlcoholStampCollector? = null

    private var componentsAlcoholStampCollector: AlcoholStampCollector? = null

    fun newStampCollector(processExciseAlcoProductService: ProcessExciseAlcoProductService) {
        setsAlcoholStampCollector = AlcoholStampCollector(
                processExciseAlcoProductService
        )
        componentsAlcoholStampCollector = AlcoholStampCollector(
                processExciseAlcoProductService
        )
    }

    fun clearAllStampsCollectors() {
        setsAlcoholStampCollector?.clear()
        componentsAlcoholStampCollector?.clear()
    }

    fun clearComponentsStampCollector() {
        componentsAlcoholStampCollector?.clear()
    }

    fun getComponentsStampCollector(): AlcoholStampCollector? {
        return componentsAlcoholStampCollector
    }

    fun getSetsStampCollector(): AlcoholStampCollector? {
        return setsAlcoholStampCollector
    }

    fun saveStampsToSet() {
        setsAlcoholStampCollector?.moveStampsFrom(componentsAlcoholStampCollector)
    }

    fun addStampToComponentsStampCollector(materialNumber: String, setMaterialNumber: String, writeOffReason: String, isBadStamp: Boolean): Boolean {
        if (setsAlcoholStampCollector!!.containsStamp(componentsAlcoholStampCollector?.getPreparedStampCode().orEmpty())) {
            return false
        }
        return componentsAlcoholStampCollector!!.add(materialNumber, setMaterialNumber, writeOffReason, isBadStamp)
    }

}