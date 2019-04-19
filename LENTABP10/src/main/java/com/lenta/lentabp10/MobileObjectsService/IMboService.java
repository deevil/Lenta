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

public interface IMboService {

    //region MB_S_07
    MB_S_07 getUomInfo(String uom);
    //endregion

    //region MB_S_14
    List<MB_S_14> getParametersByParamName(String paramName);
    String getFirstParameterValueByName(String paramName);
    //endregion

    //region MB_S_22
    List<MB_S_22> getSetItemsByMatnrOsn(String matnrOsn);
    //endregion

    //region MB_S_25
    MB_S_25 getBarcodeInfo(String barcode);
    //endregion

    //region MB_S_26
    List<MB_S_26> getAllPrinters();
    //endregion

    //region MB_S_30
    //MB_S_30 getByMaterialNumber(string material);
    MB_S_30 getProductByMaterialNumber(String material);
    //endregion

    //region MB_S_31
    MB_S_31 getWriteOffCause(String taskType, String sectionId, String matkl, String ekgrp);
    //endregion

    //region MB_S_32
    List<MB_S_32> getWriteOffCauseByTask(String taskType);
    //endregion

    //region MB_S_33
    List<MB_S_33> getStoragesByTaskTypeAndTK(String taskType, String tkNumber);
    //endregion

    //region MB_S_34
    List<MB_S_34> getProductTypesByTaskType(String taskType);
    //endregion

    //region MB_S_36
    MB_S_36 getGisControlsByTaskCntrl(String taskCntrl);
    //endregion
}
