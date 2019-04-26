package com.lenta.bp10.fmp.resources.storloc;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz02V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_02_V001";
    public static final String NAME_OUT_PARAM_ET_STORLOCS = "ET_STORLOCS";
    public static final String LIFE_TIME = null;

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_STORLOCS, Status_ET_STORLOCS> localHelper_ET_STORLOCS;


    public ZmpUtz02V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_STORLOCS =
                new LocalTableResourceHelper<ItemLocal_ET_STORLOCS, Status_ET_STORLOCS>(NAME_RESOURCE,
                        NAME_OUT_PARAM_ET_STORLOCS,
                        hyperHive,
                        Status_ET_STORLOCS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() {
        return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, false);
    }

    static final class Status_ET_STORLOCS extends StatusSelectTable<ItemLocal_ET_STORLOCS> {
    }

    public static class ItemLocal_ET_STORLOCS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("STORLOC")
        public String storloc;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("LOCKED")
        public String locked;


    }


    public interface Params extends CustomParameter {
    }


    public static class LimitedScalarParameter extends ScalarParameter {
        @SuppressWarnings("unchecked")
        public LimitedScalarParameter(String name, Object value) {
            super(name, value);
        }

        public static LimitedScalarParameter IV_PLANT(String value) {
            return new LimitedScalarParameter("IV_PLANT", value);
        }

    }
}

