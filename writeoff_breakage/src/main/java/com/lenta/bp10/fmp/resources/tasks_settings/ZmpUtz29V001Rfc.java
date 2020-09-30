package com.lenta.bp10.fmp.resources.tasks_settings;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

@Deprecated
public class ZmpUtz29V001Rfc {

    public static final String NAME_RESOURCE = "ZFMP_UTZ_54_V001";
    public static final String NAME_OUT_PARAM_ET_TASK_TPS = "ET_TASK_TPS";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_TASK_TPS, Status_ET_TASK_TPS> localHelper_ET_TASK_TPS;


    public ZmpUtz29V001Rfc(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_TASK_TPS =
                new LocalTableResourceHelper<ItemLocal_ET_TASK_TPS, Status_ET_TASK_TPS>(NAME_RESOURCE,
                        NAME_OUT_PARAM_ET_TASK_TPS,
                        hyperHive,
                        Status_ET_TASK_TPS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_TASK_TPS extends StatusSelectTable<ItemLocal_ET_TASK_TPS> {}

    public static class ItemLocal_ET_TASK_TPS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TASK_TYPE")
        public String taskType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("BWART")
        public String bwart;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("KOSTL")
        public String kostl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("LGORTTO")
        public String lgortto;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("SEND_GIS")
        public String sendGis;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("NO_GRUND")
        public String noGrund;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("LONG_NAME")
        public String longName;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @Nullable
        @SerializedName("LIMIT")
        public Double limit;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("CHK_OWNPR")
        public String chkOwnpr;


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

        public static LimitedScalarParameter IV_USER(String value) {
            return new LimitedScalarParameter("IV_USER", value);
        }

    }
}

