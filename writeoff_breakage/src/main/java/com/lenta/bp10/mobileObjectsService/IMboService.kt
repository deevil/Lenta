package com.lenta.bp10.mobileObjectsService

import com.lenta.bp10.mobileObjectsService.models.*

interface IMboService {

    //region MB_S_07
    fun getUomInfo(uom: String): MB_S_07
    //endregion

    //region MB_S_14
    fun getParametersByParamName(paramName: String): List<MB_S_14>
    fun getFirstParameterValueByName(paramName: String): String
    //endregion

    //region MB_S_22
    fun getSetItemsByMatnrOsn(matnrOsn: String): List<MB_S_22>
    //endregion

    //region MB_S_25
    fun getBarcodeInfo(barcode: String): MB_S_25
    //endregion

    //region MB_S_26
    fun getAllPrinters(): List<MB_S_26>
    //endregion

    //region MB_S_30
    //MB_S_30 getByMaterialNumber(string material);
    fun getProductByMaterialNumber(material: String): MB_S_30
    //endregion

    //region MB_S_31
    fun getWriteOffCause(taskType: String, sectionId: String, matkl: String, ekgrp: String): MB_S_31?
    //endregion

    //region MB_S_32
    fun getWriteOffCauseByTask(taskType: String): List<MB_S_32>
    //endregion

    //region MB_S_33
    fun getStoragesByTaskTypeAndTK(taskType: String, tkNumber: String): List<MB_S_33>
    //endregion

    //region MB_S_34
    fun getProductTypesByTaskType(taskType: String): List<MB_S_34>
    //endregion

    //region MB_S_36
    fun getGisControlsByTaskCntrl(taskCntrl: String): MB_S_36?
    //endregion
}