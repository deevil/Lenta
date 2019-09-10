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


//Сохранение данных задания - СЦН
public class ZmpUtzWkl07V001Rfc {

    public static final String NAME_RESOURCE = "ZMP_UTZ_WKL_07_V001_RFC";
    public static final String NAME_OUT_PARAM_ET_RETCODE = "ET_RETCODE";
    public static final String NAME_OUT_PARAM_ET_TASK_LIST = "ET_TASK_LIST";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE> localHelper_ET_RETCODE;
    public final LocalTableResourceHelper<ItemLocal_ET_TASK_LIST, Status_ET_TASK_LIST> localHelper_ET_TASK_LIST;


    public ZmpUtzWkl07V001Rfc(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_RETCODE = 
                 new LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_RETCODE, 
                         hyperHive,
                         Status_ET_RETCODE.class);

        localHelper_ET_TASK_LIST = 
                 new LocalTableResourceHelper<ItemLocal_ET_TASK_LIST, Status_ET_TASK_LIST>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_TASK_LIST, 
                         hyperHive,
                         Status_ET_TASK_LIST.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_RETCODE extends StatusSelectTable<ItemLocal_ET_RETCODE> {}
    static final class Status_ET_TASK_LIST extends StatusSelectTable<ItemLocal_ET_TASK_LIST> {}

    public static class ItemLocal_ET_RETCODE {
        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("RETCODE")
        public Integer retcode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ERROR_TEXT")
        public String errorText;


    }

    public static class ItemLocal_ET_TASK_LIST {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TASK_NUM")
        public String taskNum;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TEXT1")
        public String text1;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TEXT2")
        public String text2;


    }


    public interface Params extends CustomParameter {}

    public static class Param_IT_CHECK_PRICE implements Params, ConvertableToArray {
        // type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @ParameterField(position = 0)
        @SerializedName("MATNR")
        public String matnr;

        // type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @ParameterField(position = 1)
        @SerializedName("STAT_CHECK")
        public String statCheck;

        // type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @ParameterField(position = 2)
        @SerializedName("IS_PRINT")
        public String isPrint;


        public Param_IT_CHECK_PRICE(String matnr, String statCheck, String isPrint) {
            this.matnr = matnr;
            this.statCheck = statCheck;
            this.isPrint = isPrint;

        }

        public Param_IT_CHECK_PRICE() {
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[]{matnr, statCheck, isPrint};
        }

        @NonNull
        @Override
        public String getParameterName() {
            return "IT_CHECK_PRICE";
        }
    }

    public static class Param_IT_TASK_POS implements Params, ConvertableToArray {
        // type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @ParameterField(position = 0)
        @SerializedName("MATNR")
        public String matnr;

        // type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @ParameterField(position = 1)
        @SerializedName("XZAEL")
        public String xzael;

        // type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @ParameterField(position = 2)
        @SerializedName("FACT_QNT")
        public Double factQnt;


        public Param_IT_TASK_POS(String matnr, String xzael, Double factQnt) {
            this.matnr = matnr;
            this.xzael = xzael;
            this.factQnt = factQnt;

        }

        public Param_IT_TASK_POS() {
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[]{matnr, xzael, factQnt};
        }

        @NonNull
        @Override
        public String getParameterName() {
            return "IT_TASK_POS";
        }
    }


    public static class LimitedScalarParameter extends ScalarParameter {
        @SuppressWarnings("unchecked")
        public LimitedScalarParameter(String name, Object value) {
            super(name, value);
        }

        public static LimitedScalarParameter IV_DESCR(String value) {
            return new LimitedScalarParameter("IV_DESCR", value);
        }

        public static LimitedScalarParameter IV_IP(String value) {
            return new LimitedScalarParameter("IV_IP", value);
        }

        public static LimitedScalarParameter IV_NOT_FINISH(String value) {
            return new LimitedScalarParameter("IV_NOT_FINISH", value);
        }

        public static LimitedScalarParameter IV_TASK_NUM(String value) {
            return new LimitedScalarParameter("IV_TASK_NUM", value);
        }

        public static LimitedScalarParameter IV_WERKS(String value) {
            return new LimitedScalarParameter("IV_WERKS", value);
        }

    }
}

