package com.lenta.shared.fmp.resources.slow;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ForbiddenScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz46V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_46_V001";
    public static final String NAME_OUT_PARAM_ET_SET_LIST = "ET_SET_LIST";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_SET_LIST, Status_ET_SET_LIST> localHelper_ET_SET_LIST;


    public ZmpUtz46V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_SET_LIST = 
                 new LocalTableResourceHelper<ItemLocal_ET_SET_LIST, Status_ET_SET_LIST>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_SET_LIST, 
                         hyperHive,
                         Status_ET_SET_LIST.class);

    }

    public RequestBuilder<Params, ForbiddenScalarParameter> newRequest() { return new RequestBuilder<Params, ForbiddenScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_SET_LIST extends StatusSelectTable<ItemLocal_ET_SET_LIST> {}

    public static class ItemLocal_ET_SET_LIST {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR_OSN")
        public String matnrOsn;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATNR")
        public String matnr;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("MENGE")
        public Double menge;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MEINS")
        public String meins;


    }


    public interface Params extends CustomParameter {}


}

