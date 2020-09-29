package com.lenta.bp10.fmp.resources.gis_control;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

@Deprecated
public class ZmpUtz35V001Rfc {

    public static final String NAME_RESOURCE = "ZFMP_UTZ_54_V001";
    public static final String NAME_OUT_PARAM_ET_CNTRL = "ET_CNTRL";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_CNTRL, Status_ET_CNTRL> localHelper_ET_CNTRL;


    public ZmpUtz35V001Rfc(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_CNTRL =
                new LocalTableResourceHelper<ItemLocal_ET_CNTRL, Status_ET_CNTRL>(NAME_RESOURCE,
                        NAME_OUT_PARAM_ET_CNTRL,
                        hyperHive,
                        Status_ET_CNTRL.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);}

    static final class Status_ET_CNTRL extends StatusSelectTable<ItemLocal_ET_CNTRL> {}

    public static class ItemLocal_ET_CNTRL {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TASK_TYPE")
        public String taskType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TASK_CNTRL")
        public String taskCntrl;
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

