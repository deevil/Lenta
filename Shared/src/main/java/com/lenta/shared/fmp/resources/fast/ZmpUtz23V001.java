package com.lenta.shared.fmp.resources.fast;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz23V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_23_V001";
    public static final String NAME_OUT_PARAM_ET_WERKS_ADR = "ET_WERKS_ADR";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_WERKS_ADR, Status_ET_WERKS_ADR> localHelper_ET_WERKS_ADR;


    public ZmpUtz23V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_WERKS_ADR =
                 new LocalTableResourceHelper<ItemLocal_ET_WERKS_ADR, Status_ET_WERKS_ADR>(NAME_RESOURCE,
                         NAME_OUT_PARAM_ET_WERKS_ADR,
                         hyperHive,
                         Status_ET_WERKS_ADR.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() {
        return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);
    }

    static final class Status_ET_WERKS_ADR extends StatusSelectTable<ItemLocal_ET_WERKS_ADR> {}

    public static class ItemLocal_ET_WERKS_ADR {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("WERKS")
        public String werks;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("ADDRES")
        public String addres;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("RETAIL_TYPE")
        public String retailType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("VERSION")
        public String version;
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

