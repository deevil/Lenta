package com.lenta.shared.fmp.resources.slow;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz30V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_30_V001";
    public static final String NAME_OUT_PARAM_ET_MATERIALS = "ET_MATERIALS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_MATERIALS, Status_ET_MATERIALS> localHelper_ET_MATERIALS;


    public ZmpUtz30V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_MATERIALS = 
                 new LocalTableResourceHelper<ItemLocal_ET_MATERIALS, Status_ET_MATERIALS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_MATERIALS, 
                         hyperHive,
                         Status_ET_MATERIALS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_MATERIALS extends StatusSelectTable<ItemLocal_ET_MATERIALS> {}

    public static class ItemLocal_ET_MATERIALS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MATERIAL")
        public String material;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("NAME")
        public String name;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MATYPE")
        public String matype;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("BUOM")
        public String buom;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MATR_TYPE")
        public String matrType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("ABTNR")
        public String abtnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("IS_RETURN")
        public String isReturn;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("IS_EXC")
        public String isExc;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("IS_ALCO")
        public String isAlco;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MATKL")
        public String matkl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("EKGRP")
        public String ekgrp;


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

