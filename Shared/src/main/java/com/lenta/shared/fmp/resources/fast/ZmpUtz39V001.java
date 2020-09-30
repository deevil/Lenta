package com.lenta.shared.fmp.resources.fast;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz39V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_39_V002";
    public static final String NAME_OUT_PARAM_ET_TASK_TPS = "ET_TASK_TPS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_TASK_TPS, Status_ET_TASK_TPS> localHelper_ET_TASK_TPS;


    public ZmpUtz39V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_TASK_TPS = 
                 new LocalTableResourceHelper<ItemLocal_ET_TASK_TPS, Status_ET_TASK_TPS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_TASK_TPS, 
                         hyperHive,
                         Status_ET_TASK_TPS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_TASK_TPS extends StatusSelectTable<ItemLocal_ET_TASK_TPS> {}

    public static class ItemLocal_ET_TASK_TPS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        /** Тип задания */
        @Nullable
        @SerializedName("TASK_TYPE")
        public String taskType;

        /** Аннотация к типу задания*/
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("ANNOTATION")
        public String annotation;

        /** Флаг – делить по ГИС-контролю */
        @Nullable
        @SerializedName("DIV_CNTRL")
        public String isDivByGis;

        /** Флаг – Делить по поставщику */
        @Nullable
        @SerializedName("DIV_LIFNR")
        public String isDivByProvider;

        /** Флаг – Делить по виду товара */
        @Nullable
        @SerializedName("DIV_MTART")
        public String isDivByMaterialType;

        /** Флаг - Делить по секции */
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("DIV_ABTNR")
        public String divAbtnr;

        /** Флаг - Делить по группе закупок товаров */
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("DIV_EKGRP")
        public String divEkgrp;

        /** Флаг - Делить группе маркировки */
        @Nullable
        @SerializedName("DIV_MARKTYPE")
        public String isDivByMarkType;

        /** Флаг – Делить по МРЦ */
        @Nullable
        @SerializedName("DIV_MPR")
        public String isDivByMinimalPrice;
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

