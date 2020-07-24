package com.lenta.bp14.fmp.resources;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;


// Проверка марки
public class ZmpUtzWkl12V001Rfc {

    public static final String NAME_RESOURCE = "ZMP_UTZ_WKL_12_V001_RFC";
    public static final String NAME_OUT_PARAM_ET_RESULT = "ET_RESULT";
    public static final String NAME_OUT_PARAM_ET_RETCODE = "ET_RETCODE";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_RESULT, Status_ET_RESULT> localHelper_ET_RESULT;
    public final LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE> localHelper_ET_RETCODE;


    public ZmpUtzWkl12V001Rfc(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_RESULT = 
                 new LocalTableResourceHelper<ItemLocal_ET_RESULT, Status_ET_RESULT>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_RESULT, 
                         hyperHive,
                         Status_ET_RESULT.class);

        localHelper_ET_RETCODE = 
                 new LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_RETCODE, 
                         hyperHive,
                         Status_ET_RETCODE.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_RESULT extends StatusSelectTable<ItemLocal_ET_RESULT> {}
    static final class Status_ET_RETCODE extends StatusSelectTable<ItemLocal_ET_RETCODE> {}

    public static class ItemLocal_ET_RESULT {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("STAT")
        public String stat;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("STAT_TEXT")
        public String statText;


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

        public static LimitedScalarParameter IV_MARK_NUM(String value) {
            return new LimitedScalarParameter("IV_MARK_NUM", value);
        }

        public static LimitedScalarParameter IV_MATNR(String value) {
            return new LimitedScalarParameter("IV_MATNR", value);
        }

        public static LimitedScalarParameter IV_MODE(String value) {
            return new LimitedScalarParameter("IV_MODE", value);
        }

        public static LimitedScalarParameter IV_WERKS(String value) {
            return new LimitedScalarParameter("IV_WERKS", value);
        }

    }
}

