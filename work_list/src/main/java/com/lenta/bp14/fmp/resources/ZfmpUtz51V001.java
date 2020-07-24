package com.lenta.bp14.fmp.resources;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.ForbiddenScalarParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.models.StatusSelectTable;

//Справочник WKL – типы ценников

public class ZfmpUtz51V001 {

    public static final String NAME_RESOURCE = "ZFMP_UTZ_51_V001";
    public static final String NAME_OUT_PARAM_ET_PRICE_TYPE = "ET_PRICE_TYPE";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ItemLocal_ET_PRICE_TYPE, Status_ET_PRICE_TYPE> localHelper_ET_PRICE_TYPE;


    public ZfmpUtz51V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_PRICE_TYPE =
                new LocalTableResourceHelper<ItemLocal_ET_PRICE_TYPE, Status_ET_PRICE_TYPE>(NAME_RESOURCE,
                        NAME_OUT_PARAM_ET_PRICE_TYPE,
                        hyperHive,
                        Status_ET_PRICE_TYPE.class);

    }

    public RequestBuilder<Params, ForbiddenScalarParameter> newRequest() {
        return new RequestBuilder<Params, ForbiddenScalarParameter>(hyperHive, NAME_RESOURCE, true);
    }

    static final class Status_ET_PRICE_TYPE extends StatusSelectTable<ItemLocal_ET_PRICE_TYPE> {
    }

    public static class ItemLocal_ET_PRICE_TYPE {
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TEMPLATE_CODE")
        public String templateCode;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("TEMPLATE_NAME")
        public String templateName;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        @SerializedName("IS_REGULAR")
        public String isRegular;


    }


    public interface Params extends CustomParameter {
    }


}

