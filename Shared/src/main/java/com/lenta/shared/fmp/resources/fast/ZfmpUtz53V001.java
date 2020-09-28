package com.lenta.shared.fmp.resources.fast;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ForbiddenScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZfmpUtz53V001 {

    public static final String NAME_RESOURCE = "ZFMP_UTZ_53_V001";
    public static final String NAME_OUT_PARAM_ET_MARK_TPS = "ET_MARK_TPS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_MARK_TPS, Status_ET_MARK_TPS> localHelper_ET_MARK_TPS;


    public ZfmpUtz53V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_MARK_TPS = 
                 new LocalTableResourceHelper<ItemLocal_ET_MARK_TPS, Status_ET_MARK_TPS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_MARK_TPS, 
                         hyperHive,
                         Status_ET_MARK_TPS.class);

    }

    public RequestBuilder<Params, ForbiddenScalarParameter> newRequest() { return new RequestBuilder<Params, ForbiddenScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_MARK_TPS extends StatusSelectTable<ItemLocal_ET_MARK_TPS> {}

    public static class ItemLocal_ET_MARK_TPS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MARK_TYPE")
        public String markType;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("NAME")
        public String name;


    }


    public interface Params extends CustomParameter {}


}

