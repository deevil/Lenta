package com.lenta.shared.fmp.resources.fast;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz40V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_40_V001";
    public static final String NAME_OUT_PARAM_ET_LGORT_SRC = "ET_LGORT_SRC";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_LGORT_SRC, Status_ET_LGORT_SRC> localHelper_ET_LGORT_SRC;


    public ZmpUtz40V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_LGORT_SRC = 
                 new LocalTableResourceHelper<ItemLocal_ET_LGORT_SRC, Status_ET_LGORT_SRC>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_LGORT_SRC, 
                         hyperHive,
                         Status_ET_LGORT_SRC.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_LGORT_SRC extends StatusSelectTable<ItemLocal_ET_LGORT_SRC> {}

    public static class ItemLocal_ET_LGORT_SRC {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TASK_TYPE")
        public String taskType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("LGORT_SRC")
        public String lgortSrc;
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

