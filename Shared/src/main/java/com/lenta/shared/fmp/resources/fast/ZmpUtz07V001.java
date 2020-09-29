package com.lenta.shared.fmp.resources.fast;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

@SuppressWarnings("ALL")
public class ZmpUtz07V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_07_V001";
    public static final String NAME_OUT_PARAM_ET_UOMS = "ET_UOMS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_UOMS, Status_ET_UOMS> localHelper_ET_UOMS;


    public ZmpUtz07V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_UOMS = 
                 new LocalTableResourceHelper<ItemLocal_ET_UOMS, Status_ET_UOMS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_UOMS, 
                         hyperHive,
                         Status_ET_UOMS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_UOMS extends StatusSelectTable<ItemLocal_ET_UOMS> {}

    public static class ItemLocal_ET_UOMS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("UOM")
        public String uom;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @Nullable
        @SerializedName("NAME")
        public String name;

        //  type: BIGINT, source: {'name': 'SAP', 'type': 's'}
        @Nullable
        @SerializedName("DECAN")
        public Integer decan;


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

