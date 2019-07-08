package com.lenta.shared.fmp.resources.slow;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz24V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_24_V001";
    public static final String NAME_OUT_PARAM_ET_MATERIALS = "ET_MATERIALS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_MATERIALS, Status_ET_MATERIALS> localHelper_ET_MATERIALS;


    public ZmpUtz24V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_MATERIALS = 
                 new LocalTableResourceHelper<ItemLocal_ET_MATERIALS, Status_ET_MATERIALS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_MATERIALS, 
                         hyperHive,
                         Status_ET_MATERIALS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() {
        return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);
    }

    static final class Status_ET_MATERIALS extends StatusSelectTable<ItemLocal_ET_MATERIALS> {}

    public static class ItemLocal_ET_MATERIALS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATERIAL")
        public String material;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("NAME")
        public String name;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATYPE")
        public String matype;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("BUOM")
        public String buom;

        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("MHDHB_DAYS")
        public Integer mhdhbDays;

        //  type: BIGINT, source: {'name': 'SAP', 'type': 'I'}
        @SerializedName("MHDRZ_DAYS")
        public Integer mhdrzDays;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("BSTME")
        public String bstme;


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

