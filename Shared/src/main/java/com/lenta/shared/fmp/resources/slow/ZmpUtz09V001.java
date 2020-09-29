package com.lenta.shared.fmp.resources.slow;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz09V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_09_V001";
    public static final String NAME_OUT_PARAM_ET_VENDORS = "ET_VENDORS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_VENDORS, Status_ET_VENDORS> localHelper_ET_VENDORS;


    public ZmpUtz09V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_VENDORS = 
                 new LocalTableResourceHelper<ItemLocal_ET_VENDORS, Status_ET_VENDORS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_VENDORS, 
                         hyperHive,
                         Status_ET_VENDORS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_VENDORS extends StatusSelectTable<ItemLocal_ET_VENDORS> {}

    public static class ItemLocal_ET_VENDORS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("VENDOR")
        public String vendor;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("VENDORNAME")
        public String vendorname;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("SPERR")
        public String sperr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("SPERM")
        public String sperm;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("LOEVM")
        public String loevm;


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

