package com.lenta.bp18.data.model

import com.lenta.bp18.data.IPersistCheckResult
import com.lenta.bp18.model.pojo.Good
import com.lenta.bp18.request.pojo.GoodInfo
import com.lenta.shared.models.core.BarcodeInfo
import com.lenta.shared.platform.time.ITimeMonitor
import javax.inject.Inject

class CheckData @Inject constructor(
        private val persistCheckResult: IPersistCheckResult,
        private val timeMonitor: ITimeMonitor
){
    val ean: MutableList<BarcodeInfo> = mutableListOf()
    val good: MutableList<Good> = mutableListOf()

    init {
        val savedResult = persistCheckResult.getSavedCheckResult()
        if (savedResult != null){
            restoreSavedCheckResult(savedResult)
        }
    }

    fun saveCheckResult(){
        persistCheckResult.saveCheckResult(this)
    }

    fun restoreSavedCheckResult(checkResultData: CheckResultData) {
        good.addAll(checkResultData.good)
    }

    fun clearSaveData(){
        persistCheckResult.clearSavedData()
    }

}
