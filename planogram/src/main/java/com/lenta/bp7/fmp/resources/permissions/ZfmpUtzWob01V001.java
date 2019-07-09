package com.lenta.bp7.fmp.resources.permissions;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZfmpUtzWob01V001 {

    public static final String NAME_RESOURCE = "ZFMP_UTZ_WOB_01_V001";
    public static final String NAME_OUT_PARAM_ET_RETCODE = "ET_RETCODE";
    public static final String NAME_OUT_PARAM_ET_WERKS = "ET_WERKS";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE> localHelper_ET_RETCODE;
    public final LocalTableResourceHelper<ItemLocal_ET_WERKS, Status_ET_WERKS> localHelper_ET_WERKS;


    public ZfmpUtzWob01V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_RETCODE = 
                 new LocalTableResourceHelper<ItemLocal_ET_RETCODE, Status_ET_RETCODE>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_RETCODE, 
                         hyperHive,
                         Status_ET_RETCODE.class);

        localHelper_ET_WERKS = 
                 new LocalTableResourceHelper<ItemLocal_ET_WERKS, Status_ET_WERKS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_WERKS, 
                         hyperHive,
                         Status_ET_WERKS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_RETCODE extends StatusSelectTable<ItemLocal_ET_RETCODE> {}
    static final class Status_ET_WERKS extends StatusSelectTable<ItemLocal_ET_WERKS> {}

    public static class ItemLocal_ET_RETCODE {
        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("RETCODE")
        public Integer retcode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ERROR_TEXT")
        public String errorText;


    }

    public static class ItemLocal_ET_WERKS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("WERKS")
        public String werks;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ADDRES")
        public String addres;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("RETAIL_TYPE")
        public String retailType;


    }


    public interface Params extends CustomParameter {}


    public static class LimitedScalarParameter extends ScalarParameter {
        @SuppressWarnings("unchecked")
        public LimitedScalarParameter(String name, Object value) {
            super(name, value);
        }

        public static LimitedScalarParameter IV_USER(String value) {
            return new LimitedScalarParameter("IV_USER", value);
        }

    }
}

