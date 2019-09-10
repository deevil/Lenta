package com.lenta.bp14.fmp.resources;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtzWkl03V001Rfc {

    public static final String NAME_RESOURCE = "ZMP_UTZ_WKL_03_V001_RFC";
    public static final String NAME_OUT_PARAM_ET_ADDINFO = "ET_ADDINFO";
    public static final String NAME_OUT_PARAM_ET_CHECK_RESULT = "ET_CHECK_RESULT";
    public static final String NAME_OUT_PARAM_ET_LIFNR = "ET_LIFNR";
    public static final String NAME_OUT_PARAM_ET_MATERIALS = "ET_MATERIALS";
    public static final String NAME_OUT_PARAM_ET_PLACES = "ET_PLACES";
    public static final String NAME_OUT_PARAM_ET_RETCODE = "ET_RETCODE";
    public static final String NAME_OUT_PARAM_ET_STOCKS = "ET_STOCKS";
    public static final String NAME_OUT_PARAM_ET_TASK_MARK = "ET_TASK_MARK";
    public static final String NAME_OUT_PARAM_ET_TASK_POS = "ET_TASK_POS";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_ADDINFO, Status_ET_ADDINFO> localHelper_ET_ADDINFO;
    public final LocalTableResourceHelper<ItemLocal_ET_CHECK_RESULT, Status_ET_CHECK_RESULT> localHelper_ET_CHECK_RESULT;
    public final LocalTableResourceHelper<ItemLocal_ET_LIFNR, Status_ET_LIFNR> localHelper_ET_LIFNR;
    public final LocalTableResourceHelper<ItemLocal_ET_MATERIALS, Status_ET_MATERIALS> localHelper_ET_MATERIALS;
    public final LocalTableResourceHelper<ItemLocal_ET_PLACES, Status_ET_PLACES> localHelper_ET_PLACES;
    public final LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE> localHelper_ET_RETCODE;
    public final LocalTableResourceHelper<ItemLocal_ET_STOCKS, Status_ET_STOCKS> localHelper_ET_STOCKS;
    public final LocalTableResourceHelper<ItemLocal_ET_TASK_MARK, Status_ET_TASK_MARK> localHelper_ET_TASK_MARK;
    public final LocalTableResourceHelper<ItemLocal_ET_TASK_POS, Status_ET_TASK_POS> localHelper_ET_TASK_POS;



    //Получение состава задания - РБС
    public ZmpUtzWkl03V001Rfc(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_ADDINFO = 
                 new LocalTableResourceHelper<ItemLocal_ET_ADDINFO, Status_ET_ADDINFO>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_ADDINFO, 
                         hyperHive,
                         Status_ET_ADDINFO.class);

        localHelper_ET_CHECK_RESULT = 
                 new LocalTableResourceHelper<ItemLocal_ET_CHECK_RESULT, Status_ET_CHECK_RESULT>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_CHECK_RESULT, 
                         hyperHive,
                         Status_ET_CHECK_RESULT.class);

        localHelper_ET_LIFNR = 
                 new LocalTableResourceHelper<ItemLocal_ET_LIFNR, Status_ET_LIFNR>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_LIFNR, 
                         hyperHive,
                         Status_ET_LIFNR.class);

        localHelper_ET_MATERIALS = 
                 new LocalTableResourceHelper<ItemLocal_ET_MATERIALS, Status_ET_MATERIALS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_MATERIALS, 
                         hyperHive,
                         Status_ET_MATERIALS.class);

        localHelper_ET_PLACES = 
                 new LocalTableResourceHelper<ItemLocal_ET_PLACES, Status_ET_PLACES>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_PLACES, 
                         hyperHive,
                         Status_ET_PLACES.class);

        localHelper_ET_RETCODE = 
                 new LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_RETCODE, 
                         hyperHive,
                         Status_ET_RETCODE.class);

        localHelper_ET_STOCKS = 
                 new LocalTableResourceHelper<ItemLocal_ET_STOCKS, Status_ET_STOCKS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_STOCKS, 
                         hyperHive,
                         Status_ET_STOCKS.class);

        localHelper_ET_TASK_MARK = 
                 new LocalTableResourceHelper<ItemLocal_ET_TASK_MARK, Status_ET_TASK_MARK>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_TASK_MARK, 
                         hyperHive,
                         Status_ET_TASK_MARK.class);

        localHelper_ET_TASK_POS = 
                 new LocalTableResourceHelper<ItemLocal_ET_TASK_POS, Status_ET_TASK_POS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_TASK_POS, 
                         hyperHive,
                         Status_ET_TASK_POS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_ADDINFO extends StatusSelectTable<ItemLocal_ET_ADDINFO> {}
    static final class Status_ET_CHECK_RESULT extends StatusSelectTable<ItemLocal_ET_CHECK_RESULT> {}
    static final class Status_ET_LIFNR extends StatusSelectTable<ItemLocal_ET_LIFNR> {}
    static final class Status_ET_MATERIALS extends StatusSelectTable<ItemLocal_ET_MATERIALS> {}
    static final class Status_ET_PLACES extends StatusSelectTable<ItemLocal_ET_PLACES> {}
    static final class Status_ET_RETCODE extends StatusSelectTable<ItemLocal_ET_RETCODE> {}
    static final class Status_ET_STOCKS extends StatusSelectTable<ItemLocal_ET_STOCKS> {}
    static final class Status_ET_TASK_MARK extends StatusSelectTable<ItemLocal_ET_TASK_MARK> {}
    static final class Status_ET_TASK_POS extends StatusSelectTable<ItemLocal_ET_TASK_POS> {}

    public static class ItemLocal_ET_ADDINFO {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MIN_STOCK")
        public String minStock;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("LAST_INV")
        public String lastInv;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PLAN_DELIVERY")
        public String planDelivery;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PRICE1")
        public String price1;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PRICE2")
        public String price2;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PROMO_TEXT1")
        public String promoText1;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PROMO_TEXT2")
        public String promoText2;


    }

    public static class ItemLocal_ET_CHECK_RESULT {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("FACT_QNT")
        public Double factQnt;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("COMMENT")
        public String comment;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'D', 'format': '2019-09-10'}
        @SerializedName("DATA_PROD")
        public String dataProd;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'D', 'format': '2019-09-10'}
        @SerializedName("SHELF_LIFE")
        public String shelfLife;


    }

    public static class ItemLocal_ET_LIFNR {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("LIFNR")
        public String lifnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("LIFNR_NAME")
        public String lifnrName;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PERIOD_ACT")
        public String periodAct;


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

    public static class ItemLocal_ET_PLACES {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("PLACE_CODE")
        public String placeCode;


    }

    public static class ItemLocal_ET_RETCODE {
        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("RETCODE")
        public Integer retcode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ERROR_TEXT")
        public String errorText;


    }

    public static class ItemLocal_ET_STOCKS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("LGORT")
        public String lgort;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("STOCK")
        public Double stock;


    }

    public static class ItemLocal_ET_TASK_MARK {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MARK_NUM")
        public String markNum;


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

