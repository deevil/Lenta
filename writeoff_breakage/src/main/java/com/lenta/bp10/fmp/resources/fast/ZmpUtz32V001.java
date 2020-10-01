package com.lenta.bp10.fmp.resources.fast;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

@SuppressWarnings("ALL")
public class ZmpUtz32V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_32_V001";
    public static final String NAME_OUT_PARAM_ET_MOVREASONS = "ET_MOVREASONS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_MOVREASONS, Status_ET_MOVREASONS> localHelper_ET_MOVREASONS;


    public ZmpUtz32V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_MOVREASONS = 
                 new LocalTableResourceHelper<ItemLocal_ET_MOVREASONS, Status_ET_MOVREASONS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_MOVREASONS, 
                         hyperHive,
                         Status_ET_MOVREASONS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_MOVREASONS extends StatusSelectTable<ItemLocal_ET_MOVREASONS> {}

    public static class ItemLocal_ET_MOVREASONS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TASK_TYPE")
        public String taskType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TASK_CNTRL")
        public String taskCntrl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'N'}
        @Nullable
        @SerializedName("REASON")
        public String reason;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("GRTXT")
        public String grtxt;


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

