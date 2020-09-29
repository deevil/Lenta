package com.lenta.shared.fmp.resources.fast;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz41V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_41_V002";
    public static final String NAME_OUT_PARAM_ET_ALLOW_MATNR = "ET_ALLOW_MATNR";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_ALLOW_MATNR, Status_ET_ALLOW_MATNR> localHelper_ET_ALLOW_MATNR;


    public ZmpUtz41V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_ALLOW_MATNR = 
                 new LocalTableResourceHelper<ItemLocal_ET_ALLOW_MATNR, Status_ET_ALLOW_MATNR>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_ALLOW_MATNR, 
                         hyperHive,
                         Status_ET_ALLOW_MATNR.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_ALLOW_MATNR extends StatusSelectTable<ItemLocal_ET_ALLOW_MATNR> {}

    public static class ItemLocal_ET_ALLOW_MATNR {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TASK_TYPE")
        public String taskType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TASK_CNTRL")
        public String taskCntrl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MTART")
        public String mtart;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("EKGRP")
        public String ekgrp;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MATKL")
        public String matkl;

        /** Флаг – акцизный алкоголь */
        @Nullable
        @SerializedName("IS_EXC")
        public String isExciseAlcohol;


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

