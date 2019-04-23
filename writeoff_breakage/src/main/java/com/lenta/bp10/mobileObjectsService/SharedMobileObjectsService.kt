package com.lenta.bp10.mobileObjectsService

import com.lenta.bp10.mobileObjectsService.models.*
import java.util.*

class SharedMobileObjectsService protected constructor() {

    private var mboService: IMboService? = null

    companion object {
        private val INSTANCE = SharedMobileObjectsService()

        fun instance(): SharedMobileObjectsService {
            return INSTANCE
        }
    }

    init {
        mboService = LocalDbMboService()
    }

    fun getUomInfo(uom: String): MB_S_07 {
        return mboService!!.getUomInfo(uom)
    }

    fun getParametersByParamName(paramName: String): List<MB_S_14> {
        return mboService!!.getParametersByParamName(paramName)
    }

    fun getFirstParameterValueByName(paramName: String): String {
        return mboService!!.getFirstParameterValueByName(paramName)
    }

    fun getWobSpecTaskType(): String {
        return mboService!!.getFirstParameterValueByName("WOB_SPEC_TASK_TYPE")
    }

    fun getSetItemsByMatnrOsn(matnrOsn: String): List<MB_S_22> {
        return mboService!!.getSetItemsByMatnrOsn(matnrOsn)
    }

    fun getBarcodeInfo(barcode: String): MB_S_25 {
        return mboService!!.getBarcodeInfo(barcode)
    }

    fun getAllPrinters(): List<MB_S_26> {
        return mboService!!.getAllPrinters()
    }

    fun getAllPrinters(tkNumber: String): List<MB_S_26> {
        val foundPrinters = ArrayList<MB_S_26>()
        for (i in 0 until mboService!!.getAllPrinters().size) {
            if (mboService!!.getAllPrinters()[i].tkNumber == tkNumber) {
                foundPrinters.add(mboService!!.getAllPrinters()[i])
            }
        }

        return foundPrinters
    }

    fun getProductByMaterialNumber(material: String): MB_S_30 {
        return mboService!!.getProductByMaterialNumber(material)
    }

    fun getWriteOffCause(taskType: String, sectionId: String, matkl: String, ekgrp: String): MB_S_31? {
        return mboService!!.getWriteOffCause(taskType, sectionId, matkl, ekgrp)
    }

    fun getWriteOffCauseByTask(taskType: String): List<MB_S_32> {
        return mboService!!.getWriteOffCauseByTask(taskType)
    }

    fun getStoragesByTaskTypeAndTK(taskType: String, tkNumber: String): List<MB_S_33> {
        return mboService!!.getStoragesByTaskTypeAndTK(taskType, tkNumber)
    }

    fun getProductTypesByTaskType(taskType: String): List<MB_S_34> {
        return mboService!!.getProductTypesByTaskType(taskType)
    }

    fun getGisControlsByTaskCntrl(taskCntrl: String): MB_S_36? {
        return mboService!!.getGisControlsByTaskCntrl(taskCntrl)
    }

    fun setupMboService(newMboService: IMboService?) {
        if (newMboService != null)
            mboService = newMboService
    }
}