package com.lenta.bp14.fmp.resources;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

//Получение доп.инфо по товару
public class ZmpUtzWkl13V001Rfc {

    public static final String NAME_RESOURCE = "ZMP_UTZ_WKL_13_V001_RFC";
    public static final String NAME_OUT_PARAM_ET_PLAN_DELIV = "ET_PLAN_DELIV";
    public static final String NAME_OUT_PARAM_ET_RETCODE = "ET_RETCODE";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_PLAN_DELIV, Status_ET_PLAN_DELIV> localHelper_ET_PLAN_DELIV;
    public final LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE> localHelper_ET_RETCODE;


    public ZmpUtzWkl13V001Rfc(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_PLAN_DELIV = 
                 new LocalTableResourceHelper<ItemLocal_ET_PLAN_DELIV, Status_ET_PLAN_DELIV>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_PLAN_DELIV, 
                         hyperHive,
                         Status_ET_PLAN_DELIV.class);

        localHelper_ET_RETCODE = 
                 new LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_RETCODE, 
                         hyperHive,
                         Status_ET_RETCODE.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_PLAN_DELIV extends StatusSelectTable<ItemLocal_ET_PLAN_DELIV> {}
    static final class Status_ET_RETCODE extends StatusSelectTable<ItemLocal_ET_RETCODE> {}

    public static class ItemLocal_ET_PLAN_DELIV {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TYPE_DELIV")
        public String typeDeliv;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("STAT_DELIV")
        public String statDeliv;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("MENGE")
        public Double menge;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("ORMNG")
        public Double ormng;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("BSTME")
        public String bstme;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'D', 'format': '2019-09-10'}
        @SerializedName("DATE_PLAN")
        public String datePlan;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'T', 'format': '06:09:21'}
        @SerializedName("TIME_PLAN")
        public String timePlan;


    }

    public static class ItemLocal_ET_RETCODE {
        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("RETCODE")
        public Integer retcode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ERROR_TEXT")
        public String errorText;


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

