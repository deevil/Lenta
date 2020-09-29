package com.lenta.shared.fmp.resources.slow;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz43V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_43_V001";
    public static final String NAME_OUT_PARAM_ET_ZPROD = "ET_ZPROD";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_ZPROD, Status_ET_ZPROD> localHelper_ET_ZPROD;


    public ZmpUtz43V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_ZPROD = 
                 new LocalTableResourceHelper<ItemLocal_ET_ZPROD, Status_ET_ZPROD>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_ZPROD, 
                         hyperHive,
                         Status_ET_ZPROD.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_ZPROD extends StatusSelectTable<ItemLocal_ET_ZPROD> {}

    public static class ItemLocal_ET_ZPROD {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Expose
        @Nullable
        @SerializedName("ZPROD")
        public String zprod;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Expose
        @Nullable
        @SerializedName("PROD_NAME")
        public String prodName;


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

