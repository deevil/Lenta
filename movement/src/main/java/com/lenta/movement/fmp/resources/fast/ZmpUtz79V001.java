package com.lenta.movement.fmp.resources.fast;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz79V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_79_V001";
    public static final String NAME_OUT_PARAM_ET_PLANTS = "ET_PLANTS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<Item_Local_ET_PLANTS, Status_ET_PLANTS> localHelper_ET_PLANTS;

    public ZmpUtz79V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_PLANTS = new LocalTableResourceHelper<>(
                NAME_RESOURCE,
                NAME_OUT_PARAM_ET_PLANTS,
                hyperHive,
                Status_ET_PLANTS.class
        );

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() {
        return new RequestBuilder<>(hyperHive, NAME_RESOURCE, true);
    }

    static final class Status_ET_PLANTS extends StatusSelectTable<Item_Local_ET_PLANTS> {
    }

    public static class Item_Local_ET_PLANTS {
        @SerializedName("PLANT")
        public String plant;
    }

    public interface Params extends CustomParameter {
    }

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
