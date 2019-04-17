package com.lenta.lentabp10.MobileObjectsService;

import com.lenta.lentabp10.MobileObjectsService.models.MB_S_07;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_14;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_22;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_25;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_26;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_30;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_31;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_32;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_33;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_34;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_36;

import java.util.List;

public class LocalDbMboService implements IMboService{
    @Override
    public MB_S_07 getUomInfo(String uom) {
        return null;
    }

    @Override
    public List<MB_S_14> getParametersByParamName(String paramName) {
        return null;
    }

    @Override
    public String getFirstParameterValueByName(String paramName) {
        return null;
    }

    @Override
    public List<MB_S_22> getSetItemsByMatnrOsn(String matnrOsn) {
        return null;
    }

    @Override
    public MB_S_25 getBarcodeInfo(String barcode) {
        return null;
    }

    @Override
    public List<MB_S_26> getAllPrinters() {
        return null;
    }

    @Override
    public MB_S_30 getProductByMaterialNumber(String material) {
        return null;
    }

    @Override
    public MB_S_31 getWriteOffCause(String taskType, String sectionId, String matkl, String ekgrp) {
        return null;
    }

    @Override
    public List<MB_S_32> getWriteOffCauseByTask(String taskType) {
        return null;
    }

    @Override
    public List<MB_S_33> getStoragesByTaskTypeAndTK(String taskType, String tkNumber) {
        return null;
    }

    @Override
    public List<MB_S_34> getProductTypesByTaskType(String taskType) {
        return null;
    }

    @Override
    public MB_S_36 getGisControlsByTaskCntrl(String taskCntrl) {
        return null;
    }
}
