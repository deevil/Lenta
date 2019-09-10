package com.lenta.bp14.fmp.resources;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ForbiddenScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.models.StatusSelectTable;

//Справочник WKL – типы принтеров

public class ZfmpUtz50V001 {

    public static final String NAME_RESOURCE = "ZFMP_UTZ_50_V001";
    public static final String NAME_OUT_PARAM_ET_PRINTER_TYPE = "ET_PRINTER_TYPE";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_PRINTER_TYPE, Status_ET_PRINTER_TYPE> localHelper_ET_PRINTER_TYPE;


    public ZfmpUtz50V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_PRINTER_TYPE = 
                 new LocalTableResourceHelper<ItemLocal_ET_PRINTER_TYPE, Status_ET_PRINTER_TYPE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_PRINTER_TYPE, 
                         hyperHive,
                         Status_ET_PRINTER_TYPE.class);

    }

    public RequestBuilder<Params, ForbiddenScalarParameter> newRequest() { return new RequestBuilder<Params, ForbiddenScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_PRINTER_TYPE extends StatusSelectTable<ItemLocal_ET_PRINTER_TYPE> {}

    public static class ItemLocal_ET_PRINTER_TYPE {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PRINTER_CODE")
        public String printerCode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PRINTER_NAME")
        public String printerName;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_MOBILE")
        public String isMobile;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_STATIC")
        public String isStatic;


    }


    public interface Params extends CustomParameter {}


}

