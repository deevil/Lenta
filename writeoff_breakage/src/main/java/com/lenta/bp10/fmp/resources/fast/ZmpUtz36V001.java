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
public class ZmpUtz36V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_36_V001";
    public static final String NAME_OUT_PARAM_ET_CNTRL_TXT = "ET_CNTRL_TXT";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_CNTRL_TXT, Status_ET_CNTRL_TXT> localHelper_ET_CNTRL_TXT;


    public ZmpUtz36V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_CNTRL_TXT = 
                 new LocalTableResourceHelper<ItemLocal_ET_CNTRL_TXT, Status_ET_CNTRL_TXT>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_CNTRL_TXT, 
                         hyperHive,
                         Status_ET_CNTRL_TXT.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_CNTRL_TXT extends StatusSelectTable<ItemLocal_ET_CNTRL_TXT> {}

    public static class ItemLocal_ET_CNTRL_TXT {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TASK_CNTRL")
        public String taskCntrl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("CNTRL_TXT")
        public String cntrlTxt;


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

