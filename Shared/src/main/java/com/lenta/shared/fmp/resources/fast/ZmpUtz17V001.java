package com.lenta.shared.fmp.resources.fast;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz17V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_17_V001";
    public static final String NAME_OUT_PARAM_ET_DICT = "ET_DICT";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_DICT, Status_ET_DICT> localHelper_ET_DICT;


    public ZmpUtz17V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_DICT =
                new LocalTableResourceHelper<ItemLocal_ET_DICT, Status_ET_DICT>(NAME_RESOURCE,
                        NAME_OUT_PARAM_ET_DICT,
                        hyperHive,
                        Status_ET_DICT.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_DICT extends StatusSelectTable<ItemLocal_ET_DICT> {}

    public static class ItemLocal_ET_DICT {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TID")
        public String tid;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("CODE")
        public String code;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("SORDER")
        public String sorder;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("ABBR")
        public String abbr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("SHTXT")
        public String shtxt;
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

