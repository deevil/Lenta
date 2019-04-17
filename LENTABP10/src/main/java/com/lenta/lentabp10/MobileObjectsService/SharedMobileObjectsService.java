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

import java.util.ArrayList;
import java.util.List;

public class SharedMobileObjectsService {

    private IMboService mboService;

    //region Singleton
    protected SharedMobileObjectsService()
    {
        mboService = new LocalDbMboService();
    }

    private static final SharedMobileObjectsService INSTANCE = new SharedMobileObjectsService();

    public static SharedMobileObjectsService instance() {
        return INSTANCE;
    }
    //endregion

    //region MB_S_07
    public MB_S_07 getUomInfo(String uom)
    {
        return mboService.getUomInfo(uom);
    }
    //endregion

    //region MB_S_14
    public List<MB_S_14> getParametersByParamName(String paramName)
    {
        return mboService.getParametersByParamName(paramName);
    }

    public String getFirstParameterValueByName(String paramName)
    {
        return mboService.getFirstParameterValueByName(paramName);
    }

    public String getWobSpecTaskType()
    {
        return mboService.getFirstParameterValueByName("WOB_SPEC_TASK_TYPE");
    }
    //endregion

    //region MB_S_22
    public List<MB_S_22> getSetItemsByMatnrOsn(String matnrOsn)
    {
        return mboService.getSetItemsByMatnrOsn(matnrOsn);
    }
    //endregion

    //region MB_S_25
    public MB_S_25 getBarcodeInfo(String barcode)
    {
        return mboService.getBarcodeInfo(barcode);
    }
    //endregion

    //region MB_S_26
    public List<MB_S_26> getAllPrinters()
    {
        return mboService.getAllPrinters();
    }

    public List<MB_S_26> getAllPrinters(String tkNumber)
    {
        List<MB_S_26> foundPrinters = new ArrayList<>();
        for(int i=0; i<mboService.getAllPrinters().size(); i++) {
            if ( mboService.getAllPrinters().get(i).getTkNumber().equals(tkNumber) ) {
                foundPrinters.add(mboService.getAllPrinters().get(i));
            }
        }

        return foundPrinters;
    }
    //endregion

    //#region MB_S_29

    //public List<MB_S_29> getAllTaskTypes()
    //{
    //    return mboService.getAllTaskTypes();
    //}

    //public MB_S_29 getTaskByTaskType(string taskType)
    //{
    //    return mboService.getTaskByTaskType(taskType);
    //}

    //#endregion

    //region MB_S_30
    public MB_S_30 getProductByMaterialNumber(String material)
    {
        return mboService.getProductByMaterialNumber(material);
    }
    //endregion

    //region MB_S_31
    public MB_S_31 getWriteOffCause(String taskType, String sectionId, String matkl, String ekgrp)
    {
        return mboService.getWriteOffCause(taskType, sectionId, matkl, ekgrp);
    }
    //endregion

    //region MB_S_32
    public List<MB_S_32> getWriteOffCauseByTask(String taskType)
    {
        return mboService.getWriteOffCauseByTask(taskType);
    }
    //endregion

    //region MB_S_33
    public List<MB_S_33> getStoragesByTaskTypeAndTK(String taskType, String tkNumber)
    {
        return mboService.getStoragesByTaskTypeAndTK(taskType, tkNumber);
    }
    //endregion

    //region MB_S_34
    public List<MB_S_34> getProductTypesByTaskType(String taskType)
    {
        return mboService.getProductTypesByTaskType(taskType);
    }
    //endregion

    //region MB_S_35
    //public List<MB_S_35> getGISControlsTypesByTaskType(string taskType)
    //{
    //    return mboService.getGISControlsTypesByTaskType(taskType);
    //}
    //endregion

    //region MB_S_36
    //public List<MB_S_36> getGISControlsByTaskType(string taskType)
    //{
    //    return mboService.getGISControlsByTaskType(taskType);
    //}

    public MB_S_36 getGisControlsByTaskCntrl(String taskCntrl)
    {
        return mboService.getGisControlsByTaskCntrl(taskCntrl);
    }
    //endregion

    public void setupMboService(IMboService newMboService)
    {
        if (newMboService != null)
            mboService = newMboService;
    }
}
