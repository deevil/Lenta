package com.lenta.shared.fmp.resources.fast;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

public class ZmpUtz38V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_38_V001";
    public static final String NAME_OUT_PARAM_ET_ICONS = "ET_ICONS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_ICONS, Status_ET_ICONS> localHelper_ET_ICONS;


    public ZmpUtz38V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_ICONS = 
                 new LocalTableResourceHelper<ItemLocal_ET_ICONS, Status_ET_ICONS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_ICONS, 
                         hyperHive,
                         Status_ET_ICONS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_ICONS extends StatusSelectTable<ItemLocal_ET_ICONS> {}

    public static class ItemLocal_ET_ICONS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("CODE")
        public String code;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("TEXT")
        public String text;


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

