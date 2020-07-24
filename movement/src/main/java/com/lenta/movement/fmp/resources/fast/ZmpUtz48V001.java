package com.lenta.movement.fmp.resources.fast;

import com.google.gson.annotations.SerializedName;
import com.lenta.movement.models.MovementType;
import com.lenta.movement.models.TaskType;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

/**
 * Склад комплектации для типов заданий
 */
public class ZmpUtz48V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_48_V001";
    public static final String NAME_OUT_PARAM_ET_PLANTS = "ET_LGORT_SRC";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<Item_Local_ET_LGORT_SRC, Status_ET_LGORT_SRC> localHelper_ET_LGORT_SRC;

    public ZmpUtz48V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_LGORT_SRC = new LocalTableResourceHelper<>(
                NAME_RESOURCE,
                NAME_OUT_PARAM_ET_PLANTS,
                hyperHive,
                Status_ET_LGORT_SRC.class
        );

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() {
        return new RequestBuilder<>(hyperHive, NAME_RESOURCE, true);
    }

    static final class Status_ET_LGORT_SRC extends StatusSelectTable<Item_Local_ET_LGORT_SRC> {
    }

    public static class Item_Local_ET_LGORT_SRC {
        @SerializedName("TASK_TYPE")
        public TaskType taskType;

        @SerializedName("TYPE_MVM")
        public MovementType mvmType;

        @SerializedName("LGORT_SRC")
        public String lgortSource;
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
