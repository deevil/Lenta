package com.lenta.bp14.fmp.resources;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.request_assistant.ConvertableToArray;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.api.request_assistant.ParameterField;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.models.StatusSelectTable;

//Получение данных по товару(ам) в фоне
public class ZmpUtzWkl11V001Rfc {

    public static final String NAME_RESOURCE = "ZMP_UTZ_WKL_11_V001_RFC";
    public static final String NAME_OUT_PARAM_ET_ADDINFO = "ET_ADDINFO";
    public static final String NAME_OUT_PARAM_ET_LIFNR = "ET_LIFNR";
    public static final String NAME_OUT_PARAM_ET_MATERIALS = "ET_MATERIALS";
    public static final String NAME_OUT_PARAM_ET_PLACES = "ET_PLACES";
    public static final String NAME_OUT_PARAM_ET_PRICE = "ET_PRICE";
    public static final String NAME_OUT_PARAM_ET_RETCODE = "ET_RETCODE";
    public static final String NAME_OUT_PARAM_ET_STOCKS = "ET_STOCKS";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_ADDINFO, Status_ET_ADDINFO> localHelper_ET_ADDINFO;
    public final LocalTableResourceHelper<ItemLocal_ET_LIFNR, Status_ET_LIFNR> localHelper_ET_LIFNR;
    public final LocalTableResourceHelper<ItemLocal_ET_MATERIALS, Status_ET_MATERIALS> localHelper_ET_MATERIALS;
    public final LocalTableResourceHelper<ItemLocal_ET_PLACES, Status_ET_PLACES> localHelper_ET_PLACES;
    public final LocalTableResourceHelper<ItemLocal_ET_PRICE, Status_ET_PRICE> localHelper_ET_PRICE;
    public final LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE> localHelper_ET_RETCODE;
    public final LocalTableResourceHelper<ItemLocal_ET_STOCKS, Status_ET_STOCKS> localHelper_ET_STOCKS;


    public ZmpUtzWkl11V001Rfc(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_ADDINFO = 
                 new LocalTableResourceHelper<ItemLocal_ET_ADDINFO, Status_ET_ADDINFO>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_ADDINFO, 
                         hyperHive,
                         Status_ET_ADDINFO.class);

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

        localHelper_ET_STOCKS = 
                 new LocalTableResourceHelper<ItemLocal_ET_STOCKS, Status_ET_STOCKS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_STOCKS, 
                         hyperHive,
                         Status_ET_STOCKS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_ADDINFO extends StatusSelectTable<ItemLocal_ET_ADDINFO> {}
    static final class Status_ET_LIFNR extends StatusSelectTable<ItemLocal_ET_LIFNR> {}
    static final class Status_ET_MATERIALS extends StatusSelectTable<ItemLocal_ET_MATERIALS> {}
    static final class Status_ET_PLACES extends StatusSelectTable<ItemLocal_ET_PLACES> {}
    static final class Status_ET_PRICE extends StatusSelectTable<ItemLocal_ET_PRICE> {}
    static final class Status_ET_RETCODE extends StatusSelectTable<ItemLocal_ET_RETCODE> {}
    static final class Status_ET_STOCKS extends StatusSelectTable<ItemLocal_ET_STOCKS> {}

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


    public interface Params extends CustomParameter {}

    public static class Param_IT_EAN_LIST implements Params, ConvertableToArray {
        // type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @ParameterField(position = 0)
        @SerializedName("EAN")
        public String ean;


        public Param_IT_EAN_LIST(String ean) {
            this.ean = ean;

        }

        public Param_IT_EAN_LIST() {
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[]{ean};
        }

        @NonNull
        @Override
        public String getParameterName() {
            return "IT_EAN_LIST";
        }
    }

    public static class Param_IT_MATNR_LIST implements Params, ConvertableToArray {
        // type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @ParameterField(position = 0)
        @SerializedName("MATNR")
        public String matnr;


        public Param_IT_MATNR_LIST(String matnr) {
            this.matnr = matnr;

        }

        public Param_IT_MATNR_LIST() {
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[]{matnr};
        }

        @NonNull
        @Override
        public String getParameterName() {
            return "IT_MATNR_LIST";
        }
    }


    public static class LimitedScalarParameter extends ScalarParameter {
        @SuppressWarnings("unchecked")
        public LimitedScalarParameter(String name, Object value) {
            super(name, value);
        }

        public static LimitedScalarParameter IV_MATNR_DATA_FLG(String value) {
            return new LimitedScalarParameter("IV_MATNR_DATA_FLG", value);
        }

        public static LimitedScalarParameter IV_RBSINFO_FLG(String value) {
            return new LimitedScalarParameter("IV_RBSINFO_FLG", value);
        }

        public static LimitedScalarParameter IV_TASK_TYPE(String value) {
            return new LimitedScalarParameter("IV_TASK_TYPE", value);
        }

        public static LimitedScalarParameter IV_WERKS(String value) {
            return new LimitedScalarParameter("IV_WERKS", value);
        }

    }
}

