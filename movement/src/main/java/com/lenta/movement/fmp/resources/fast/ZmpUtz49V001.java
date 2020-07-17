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
 * Таблица настройки разрешенных товаров
 */
public class ZmpUtz49V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_49_V001";
    public static final String NAME_OUT_PARAM_ET_PLANTS = "ET_ALLOW_MATNR";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<Item_Local_ET_ALLOW_MATNR, Status_ET_ALLOW_MATNR> localHelper_ET_ALLOW_MATNR;

    public ZmpUtz49V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_ALLOW_MATNR = new LocalTableResourceHelper<>(
                NAME_RESOURCE,
                NAME_OUT_PARAM_ET_PLANTS,
                hyperHive,
                Status_ET_ALLOW_MATNR.class
        );

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() {
        return new RequestBuilder<>(hyperHive, NAME_RESOURCE, true);
    }

    static final class Status_ET_ALLOW_MATNR extends StatusSelectTable<Item_Local_ET_ALLOW_MATNR> {
    }

    public static class Item_Local_ET_ALLOW_MATNR {
        @SerializedName("TASK_TYPE")
        public TaskType taskType;

        @SerializedName("TASK_CNTRL")
        public String taskCntrl;

        @SerializedName("MTART")
        public String mtart;

        @SerializedName("EKGRP")
        public String ekgrp;

        @SerializedName("MATKL")
        public String matkl;
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
