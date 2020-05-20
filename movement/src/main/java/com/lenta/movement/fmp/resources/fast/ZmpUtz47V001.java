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
 * Настройки типов заданий
 */
public class ZmpUtz47V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_47_V001";
    public static final String NAME_OUT_PARAM_ET_TASK_TSP = "ET_TASK_TPS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<Item_Local_ET_TASK_TPS, Status_ET_TASK_TSP> localHelper_ET_TASK_TPS;

    public ZmpUtz47V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_TASK_TPS = new LocalTableResourceHelper<>(
                NAME_RESOURCE,
                NAME_OUT_PARAM_ET_TASK_TSP,
                hyperHive,
                Status_ET_TASK_TSP.class
        );

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() {
        return new RequestBuilder<>(hyperHive, NAME_RESOURCE, true);
    }

    static final class Status_ET_TASK_TSP extends StatusSelectTable<Item_Local_ET_TASK_TPS> {
    }

    public static class Item_Local_ET_TASK_TPS {
        @SerializedName("TASK_TYPE")
        public TaskType taskType;

        @SerializedName("TYPE_MVM")
        public MovementType mvmType;

        @SerializedName("ANNOTATION")
        public String annotation;

        @SerializedName("LGORT_TGT")
        public String lgortTarget;

        @SerializedName("DIV_MARK_PARTS")
        public String divMarkParts;

        @SerializedName("DIV_ALCO")
        public String divAlco;

        @SerializedName("DIV_USUAL")
        public String divUsual;

        @SerializedName("DIV_VET")
        public String divVet;

        @SerializedName("DIV_PARTS")
        public String divParts;

        @SerializedName("DIV_MTART")
        public String divMtart;

        @SerializedName("DIV_FOOD")
        public String divFood;

        @SerializedName("DIV_LIFNR")
        public String divLifnr;

        @SerializedName("DIV_MATNR")
        public String divMatnr;

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
