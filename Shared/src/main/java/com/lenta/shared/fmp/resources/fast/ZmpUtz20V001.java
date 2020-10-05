package com.lenta.shared.fmp.resources.fast;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz20V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_20_V001";
    public static final String NAME_OUT_PARAM_ET_GRUNDS = "ET_GRUNDS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_GRUNDS, Status_ET_GRUNDS> localHelper_ET_GRUNDS;


    public ZmpUtz20V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_GRUNDS =
                new LocalTableResourceHelper<ItemLocal_ET_GRUNDS, Status_ET_GRUNDS>(NAME_RESOURCE,
                        NAME_OUT_PARAM_ET_GRUNDS,
                        hyperHive,
                        Status_ET_GRUNDS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_GRUNDS extends StatusSelectTable<ItemLocal_ET_GRUNDS> {}

    public static class ItemLocal_ET_GRUNDS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TID")
        public String tid;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("GRUNDCAT")
        public String grundcat;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("GRUND")
        public String grund;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("IS_ACCBL")
        public String isAccbl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("GTEXT")
        public String gtext;


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

