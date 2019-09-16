package com.lenta.bp14.fmp.resources;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ForbiddenScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.models.StatusSelectTable;

//Справочник WKL – типы заданий

public class ZfmpUtz49V001 {

    public static final String NAME_RESOURCE = "ZFMP_UTZ_49_V001";
    public static final String NAME_OUT_PARAM_ET_TASK_TPS = "ET_TASK_TPS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_TASK_TPS, Status_ET_TASK_TPS> localHelper_ET_TASK_TPS;


    public ZfmpUtz49V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_TASK_TPS = 
                 new LocalTableResourceHelper<ItemLocal_ET_TASK_TPS, Status_ET_TASK_TPS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_TASK_TPS, 
                         hyperHive,
                         Status_ET_TASK_TPS.class);

    }

    public RequestBuilder<Params, ForbiddenScalarParameter> newRequest() { return new RequestBuilder<Params, ForbiddenScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_TASK_TPS extends StatusSelectTable<ItemLocal_ET_TASK_TPS> {}

    public static class ItemLocal_ET_TASK_TPS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TASK_TYPE")
        public String taskType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TASK_NAME")
        public String taskName;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("ANNOTATION")
        public String annotation;


    }


    public interface Params extends CustomParameter {}


}

