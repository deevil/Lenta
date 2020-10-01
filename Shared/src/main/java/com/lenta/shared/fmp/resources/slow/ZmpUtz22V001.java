package com.lenta.shared.fmp.resources.slow;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ForbiddenScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz22V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_22_V001";
    public static final String NAME_OUT_PARAM_ET_ALCOD_LIST = "ET_ALCOD_LIST";
    public static final String NAME_OUT_PARAM_ET_MATNRLIST = "ET_MATNRLIST";
    public static final String NAME_OUT_PARAM_ET_SET_LIST = "ET_SET_LIST";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_ALCOD_LIST, Status_ET_ALCOD_LIST> localHelper_ET_ALCOD_LIST;
    public final LocalTableResourceHelper<ItemLocal_ET_MATNRLIST, Status_ET_MATNRLIST> localHelper_ET_MATNRLIST;
    public final LocalTableResourceHelper<ItemLocal_ET_SET_LIST, Status_ET_SET_LIST> localHelper_ET_SET_LIST;


    public ZmpUtz22V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_ALCOD_LIST = 
                 new LocalTableResourceHelper<ItemLocal_ET_ALCOD_LIST, Status_ET_ALCOD_LIST>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_ALCOD_LIST, 
                         hyperHive,
                         Status_ET_ALCOD_LIST.class);

        localHelper_ET_MATNRLIST = 
                 new LocalTableResourceHelper<ItemLocal_ET_MATNRLIST, Status_ET_MATNRLIST>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_MATNRLIST, 
                         hyperHive,
                         Status_ET_MATNRLIST.class);

        localHelper_ET_SET_LIST = 
                 new LocalTableResourceHelper<ItemLocal_ET_SET_LIST, Status_ET_SET_LIST>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_SET_LIST, 
                         hyperHive,
                         Status_ET_SET_LIST.class);

    }

    public RequestBuilder<Params, ForbiddenScalarParameter> newRequest() { return new RequestBuilder<Params, ForbiddenScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_ALCOD_LIST extends StatusSelectTable<ItemLocal_ET_ALCOD_LIST> {}
    static final class Status_ET_MATNRLIST extends StatusSelectTable<ItemLocal_ET_MATNRLIST> {}
    static final class Status_ET_SET_LIST extends StatusSelectTable<ItemLocal_ET_SET_LIST> {}

    public static class ItemLocal_ET_ALCOD_LIST {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("ZALCCOD")
        public String zalccod;


    }

    public static class ItemLocal_ET_MATNRLIST {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MATCODE")
        public String matcode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MATKL")
        public String matkl;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("ZEXLTEXT")
        public String zexltext;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("IS_SET")
        public String isSet;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("MEINS")
        public String meins;


    }

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

