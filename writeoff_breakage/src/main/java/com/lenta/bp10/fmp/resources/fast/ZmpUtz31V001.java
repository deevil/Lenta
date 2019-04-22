package com.lenta.bp10.fmp.resources.fast;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.models.StatusSelectTable;

@SuppressWarnings("ALL")
public class ZmpUtz31V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_31_V001";
    public static final String NAME_OUT_PARAM_ET_WOBSECREASONS = "ET_WOBSECREASONS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_WOBSECREASONS, Status_ET_WOBSECREASONS> localHelper_ET_WOBSECREASONS;


    public ZmpUtz31V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_WOBSECREASONS = 
                 new LocalTableResourceHelper<ItemLocal_ET_WOBSECREASONS, Status_ET_WOBSECREASONS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_WOBSECREASONS, 
                         hyperHive,
                         Status_ET_WOBSECREASONS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_WOBSECREASONS extends StatusSelectTable<ItemLocal_ET_WOBSECREASONS> {}

    public static class ItemLocal_ET_WOBSECREASONS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TASK_TYPE")
        public String taskType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("SECTION_ID")
        public String sectionId;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'N'}
        @SerializedName("REASON")
        public String reason;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("GRTXT")
        public String grtxt;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATKL")
        public String matkl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("EKGRP")
        public String ekgrp;


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
