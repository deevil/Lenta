package com.lenta.bp14.fmp.resources;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;


//Получение состава задания - СЦН
public class ZmpUtzWkl04V001Rfc {

    public static final String NAME_RESOURCE = "ZMP_UTZ_WKL_04_V001_RFC";
    public static final String NAME_OUT_PARAM_ET_CHECK_PRICE = "ET_CHECK_PRICE";
    public static final String NAME_OUT_PARAM_ET_MATERIALS = "ET_MATERIALS";
    public static final String NAME_OUT_PARAM_ET_PRICE = "ET_PRICE";
    public static final String NAME_OUT_PARAM_ET_RETCODE = "ET_RETCODE";
    public static final String NAME_OUT_PARAM_ET_TASK_POS = "ET_TASK_POS";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_CHECK_PRICE, Status_ET_CHECK_PRICE> localHelper_ET_CHECK_PRICE;
    public final LocalTableResourceHelper<ItemLocal_ET_MATERIALS, Status_ET_MATERIALS> localHelper_ET_MATERIALS;
    public final LocalTableResourceHelper<ItemLocal_ET_PRICE, Status_ET_PRICE> localHelper_ET_PRICE;
    public final LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE> localHelper_ET_RETCODE;
    public final LocalTableResourceHelper<ItemLocal_ET_TASK_POS, Status_ET_TASK_POS> localHelper_ET_TASK_POS;


    public ZmpUtzWkl04V001Rfc(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_CHECK_PRICE = 
                 new LocalTableResourceHelper<ItemLocal_ET_CHECK_PRICE, Status_ET_CHECK_PRICE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_CHECK_PRICE, 
                         hyperHive,
                         Status_ET_CHECK_PRICE.class);

        localHelper_ET_MATERIALS = 
                 new LocalTableResourceHelper<ItemLocal_ET_MATERIALS, Status_ET_MATERIALS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_MATERIALS, 
                         hyperHive,
                         Status_ET_MATERIALS.class);

        localHelper_ET_PRICE = 
                 new LocalTableResourceHelper<ItemLocal_ET_PRICE, Status_ET_PRICE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_PRICE, 
                         hyperHive,
                         Status_ET_PRICE.class);

        localHelper_ET_RETCODE = 
                 new LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_RETCODE, 
                         hyperHive,
                         Status_ET_RETCODE.class);

        localHelper_ET_TASK_POS = 
                 new LocalTableResourceHelper<ItemLocal_ET_TASK_POS, Status_ET_TASK_POS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_TASK_POS, 
                         hyperHive,
                         Status_ET_TASK_POS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_CHECK_PRICE extends StatusSelectTable<ItemLocal_ET_CHECK_PRICE> {}
    static final class Status_ET_MATERIALS extends StatusSelectTable<ItemLocal_ET_MATERIALS> {}
    static final class Status_ET_PRICE extends StatusSelectTable<ItemLocal_ET_PRICE> {}
    static final class Status_ET_RETCODE extends StatusSelectTable<ItemLocal_ET_RETCODE> {}
    static final class Status_ET_TASK_POS extends StatusSelectTable<ItemLocal_ET_TASK_POS> {}

    public static class ItemLocal_ET_CHECK_PRICE {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("STAT_CHECK")
        public String statCheck;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_PRINT")
        public String isPrint;


    }

    public static class ItemLocal_ET_MATERIALS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATERIAL")
        public String material;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("NAME")
        public String name;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("BSTME")
        public String bstme;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("BUOM")
        public String buom;

        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("MHDHB_DAYS")
        public Integer mhdhbDays;

        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("MHDRZ_DAYS")
        public Integer mhdrzDays;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATKL")
        public String matkl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("EKGRP")
        public String ekgrp;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATR_TYPE")
        public String matrType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ABTNR")
        public String abtnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_EXC")
        public String isExc;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_ALCO")
        public String isAlco;

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

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("EAN")
        public String ean;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("EAN_UOM")
        public String eanUom;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("EAN_UMREZ")
        public Double eanUmrez;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("EAN_UMREN")
        public Double eanUmren;


    }

    public static class ItemLocal_ET_PRICE {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PRICE1")
        public String price1;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PRICE2")
        public String price2;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PRICE3")
        public String price3;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PRICE4")
        public String price4;


    }

    public static class ItemLocal_ET_RETCODE {
        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("RETCODE")
        public Integer retcode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ERROR_TEXT")
        public String errorText;


    }

    public static class ItemLocal_ET_TASK_POS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("XZAEL")
        public String xzael;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("FACT_QNT")
        public Double factQnt;


    }


    public interface Params extends CustomParameter {}


    public static class LimitedScalarParameter extends ScalarParameter {
        @SuppressWarnings("unchecked")
        public LimitedScalarParameter(String name, Object value) {
            super(name, value);
        }

        public static LimitedScalarParameter IV_IP(String value) {
            return new LimitedScalarParameter("IV_IP", value);
        }

        public static LimitedScalarParameter IV_MATNR_DATA_FLAG(String value) {
            return new LimitedScalarParameter("IV_MATNR_DATA_FLAG", value);
        }

        public static LimitedScalarParameter IV_MODE(String value) {
            return new LimitedScalarParameter("IV_MODE", value);
        }

        public static LimitedScalarParameter IV_TASK_NUM(String value) {
            return new LimitedScalarParameter("IV_TASK_NUM", value);
        }

    }
}

