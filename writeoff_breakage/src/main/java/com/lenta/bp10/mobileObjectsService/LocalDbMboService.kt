package com.lenta.bp10.mobileObjectsService

import com.lenta.bp10.mobileObjectsService.models.*

class LocalDbMboService : IMboService {
    override fun getUomInfo(uom: String): MB_S_07 {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        /**val uomInfo = PR_WOB.MB_S_07.FindByPrimaryKey(uom)

        return if (uomInfo == null)
            null
        else
            MB_S_07(uomInfo!!.UOM, uomInfo!!.NAME, uomInfo!!.DECAN)*/
    }

    override fun getParametersByParamName(paramName: String): List<MB_S_14> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFirstParameterValueByName(paramName: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSetItemsByMatnrOsn(matnrOsn: String): List<MB_S_22> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBarcodeInfo(barcode: String): MB_S_25 {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllPrinters(): List<MB_S_26> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProductByMaterialNumber(material: String): MB_S_30 {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getWriteOffCause(taskType: String, sectionId: String, matkl: String, ekgrp: String): MB_S_31 {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getWriteOffCauseByTask(taskType: String): List<MB_S_32> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStoragesByTaskTypeAndTK(taskType: String, tkNumber: String): List<MB_S_33> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProductTypesByTaskType(taskType: String): List<MB_S_34> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGisControlsByTaskCntrl(taskCntrl: String): MB_S_36 {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}