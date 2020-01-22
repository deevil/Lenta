package com.lenta.shared.fmp.resources.slow;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ForbiddenScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZfmpUtz48V001 {

    public static final String NAME_RESOURCE = "ZFMP_UTZ_48_V002";
    public static final String NAME_OUT_PARAM_ET_MATNR_LIST = "ET_MATNR_LIST";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_MATNR_LIST, Status_ET_MATNR_LIST> localHelper_ET_MATNR_LIST;


    public ZfmpUtz48V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_MATNR_LIST = 
                 new LocalTableResourceHelper<ItemLocal_ET_MATNR_LIST, Status_ET_MATNR_LIST>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_MATNR_LIST, 
                         hyperHive,
                         Status_ET_MATNR_LIST.class);

    }

    public RequestBuilder<Params, ForbiddenScalarParameter> newRequest() { return new RequestBuilder<Params, ForbiddenScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_MATNR_LIST extends StatusSelectTable<ItemLocal_ET_MATNR_LIST> {}

    public static class ItemLocal_ET_MATNR_LIST {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATERIAL")
        public String material;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("NAME")
        public String name;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATYPE")
        public String matype;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATCODE")
        public String matcode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("BUOM")
        public String buom;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("BSTME")
        public String bstme;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATR_TYPE")
        public String matrType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ABTNR")
        public String abtnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_RETURN")
        public String isReturn;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_EXC")
        public String isExc;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_ALCO")
        public String isAlco;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_SET")
        public String isSet;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATKL")
        public String matkl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("EKGRP")
        public String ekgrp;

        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("MHDHB_DAYS")
        public Integer mhdhbDays;

        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("MHDRZ_DAYS")
        public Integer mhdrzDays;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_MARK")
        public String isMark;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_VET")
        public String isVet;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_HF")
        public String isHf;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_NEW")
        public String isNew;


    }


    public interface Params extends CustomParameter {}


}

