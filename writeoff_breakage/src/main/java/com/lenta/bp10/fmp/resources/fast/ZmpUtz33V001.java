package com.lenta.bp10.fmp.resources.fast;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.models.StatusSelectTable;

@SuppressWarnings("ALL")
public class ZmpUtz33V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_33_V001";
    public static final String NAME_OUT_PARAM_ET_LGORT = "ET_LGORT";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_LGORT, Status_ET_LGORT> localHelper_ET_LGORT;


    public ZmpUtz33V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_LGORT = 
                 new LocalTableResourceHelper<ItemLocal_ET_LGORT, Status_ET_LGORT>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_LGORT, 
                         hyperHive,
                         Status_ET_LGORT.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_LGORT extends StatusSelectTable<ItemLocal_ET_LGORT> {}

    public static class ItemLocal_ET_LGORT {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TASK_TYPE")
        public String taskType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("WERKS")
        public String werks;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("LGORT")
        public String lgort;


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
