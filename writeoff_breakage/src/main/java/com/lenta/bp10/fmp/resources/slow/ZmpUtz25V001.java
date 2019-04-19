package com.lenta.bp10.fmp.resources.slow;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.models.StatusSelectTable;

@SuppressWarnings("ALL")
public class ZmpUtz25V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_25_V001";
    public static final String NAME_OUT_PARAM_ET_EANS = "ET_EANS";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_EANS, Status_ET_EANS> localHelper_ET_EANS;


    public ZmpUtz25V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_EANS = 
                 new LocalTableResourceHelper<ItemLocal_ET_EANS, Status_ET_EANS>(NAME_RESOURCE, 
                         NAME_OUT_PARAM_ET_EANS, 
                         hyperHive,
                         Status_ET_EANS.class);

    }

    public RequestBuilder<Params, LimitedScalarParameter> newRequest() { return new RequestBuilder<Params, LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);}

    static final class Status_ET_EANS extends StatusSelectTable<ItemLocal_ET_EANS> {}

    public static class ItemLocal_ET_EANS {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("EAN")
        public String ean;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("MATERIAL")
        public String material;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("UOM")
        public String uom;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("UMREZ")
        public Double umrez;

        //  type: DOUBLE, source: {'name': 'SAP', 'type': 'P'}
        @SerializedName("UMREN")
        public Double umren;


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

