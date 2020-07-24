package com.lenta.bp14.fmp.resources;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;


//Получение планируемых поставок товара
public class ZmpUtzWkl14V001Rfc {

    public static final String NAME_RESOURCE = "ZMP_UTZ_WKL_14_V001_RFC";
    public static final String NAME_OUT_PARAM_ET_RETCODE = "ET_RETCODE";
    public static final String NAME_OUT_PARAM_ET_SALE_MATNR = "ET_SALE_MATNR";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE> localHelper_ET_RETCODE;
    public final LocalTableResourceHelper<ItemLocal_ET_SALE_MATNR, Status_ET_SALE_MATNR> localHelper_ET_SALE_MATNR;


    public ZmpUtzWkl14V001Rfc(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_RETCODE = 
                 new LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_RETCODE, 
                         hyperHive,
                         Status_ET_RETCODE.class);

        localHelper_ET_SALE_MATNR = 
                 new LocalTableResourceHelper<ItemLocal_ET_SALE_MATNR, Status_ET_SALE_MATNR>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_SALE_MATNR, 
                         hyperHive,
                         Status_ET_SALE_MATNR.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_RETCODE extends StatusSelectTable<ItemLocal_ET_RETCODE> {}
    static final class Status_ET_SALE_MATNR extends StatusSelectTable<ItemLocal_ET_SALE_MATNR> {}

    public static class ItemLocal_ET_RETCODE {
        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("RETCODE")
        public Integer retcode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ERROR_TEXT")
        public String errorText;


    }

    public static class ItemLocal_ET_SALE_MATNR {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'D', 'format': '2019-09-10'}
        @SerializedName("LAST_SALE_DATE")
        public String lastSaleDate;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'T', 'format': '06:09:21'}
        @SerializedName("LAST_SALE_TIME")
        public String lastSaleTime;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("LAST_SALE_QNT")
        public Double lastSaleQnt;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("SALE_WEEK")
        public Double saleWeek;


    }


    public interface Params extends CustomParameter {}


    public static class LimitedScalarParameter extends ScalarParameter {
        @SuppressWarnings("unchecked")
        public LimitedScalarParameter(String name, Object value) {
            super(name, value);
        }

        public static LimitedScalarParameter IV_MATNR(String value) {
            return new LimitedScalarParameter("IV_MATNR", value);
        }

        public static LimitedScalarParameter IV_WERKS(String value) {
            return new LimitedScalarParameter("IV_WERKS", value);
        }

    }
}

