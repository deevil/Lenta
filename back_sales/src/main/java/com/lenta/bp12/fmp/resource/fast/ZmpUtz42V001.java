package com.lenta.bp12.fmp.resource.fast;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz42V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_42_V001";
    public static final String NAME_OUT_PARAM_ET_EXCLUDE_MATNR = "ET_EXCLUDE_MATNR";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_EXCLUDE_MATNR, Status_ET_EXCLUDE_MATNR> localHelper_ET_EXCLUDE_MATNR;


    public ZmpUtz42V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_EXCLUDE_MATNR = 
                 new LocalTableResourceHelper<ItemLocal_ET_EXCLUDE_MATNR, Status_ET_EXCLUDE_MATNR>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_EXCLUDE_MATNR, 
                         hyperHive,
                         Status_ET_EXCLUDE_MATNR.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_EXCLUDE_MATNR extends StatusSelectTable<ItemLocal_ET_EXCLUDE_MATNR> {}

    public static class ItemLocal_ET_EXCLUDE_MATNR {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TASK_TYPE")
        public String taskType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TASK_CNTRL")
        public String taskCntrl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MTART")
        public String mtart;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("EKGRP")
        public String ekgrp;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATKL")
        public String matkl;


    }


    public interface Params extends CustomParameter {}


    public static class LimitedScalarParameter extends ScalarParameter {
        @SuppressWarnings("unchecked")
        public LimitedScalarParameter(String name, Object value) {
            super(name, value);
        }

        public static LimitedScalarParameter IV_NODEPLOY(String value) {
            return new LimitedScalarParameter("IV_NODEPLOY", value);
        }

    }
}

