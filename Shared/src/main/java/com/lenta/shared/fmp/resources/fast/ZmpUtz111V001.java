package com.lenta.shared.fmp.resources.fast;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

@SuppressWarnings("ALL")
public class ZmpUtz111V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_111_V001";
    public static final String NAME_OUT_PARAM_ET_PARAMS = "ET_ST_COND";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ZmpUtz111V001.ItemLocal_ET_ST_COND, ZmpUtz111V001.Status_ET_ST_COND> localHelper_ET_ST_COND;


    public ZmpUtz111V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_ST_COND =
                new LocalTableResourceHelper<ZmpUtz111V001.ItemLocal_ET_ST_COND, ZmpUtz111V001.Status_ET_ST_COND>(NAME_RESOURCE,
                        NAME_OUT_PARAM_ET_PARAMS,
                        hyperHive,
                        ZmpUtz111V001.Status_ET_ST_COND.class);

    }

    public RequestBuilder<ZmpUtz111V001.Params, ZmpUtz111V001.LimitedScalarParameter> newRequest() {
        return new RequestBuilder<ZmpUtz111V001.Params, ZmpUtz111V001.LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);
    }

    static final class Status_ET_ST_COND extends StatusSelectTable<ZmpUtz111V001.ItemLocal_ET_ST_COND> {
    }

    public static class ItemLocal_ET_ST_COND {

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        /*Код предприятия*/
        @SerializedName("WERKS_D")
        public String werks;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        /*Товар*/
        @SerializedName("MATNR")
        public String matnr;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        /*Код условия хранения*/
        @SerializedName("ZST_COND")
        public String stcond;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        /*Наименование условия хранения*/
        @SerializedName("ZST_COND_NAM")
        public String stcondnam;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        /*Условия хранения по умолчанию*/
        @SerializedName("ZSTDEF_COND")
        public String defcond;

    }

    public interface Params extends CustomParameter {
    }

    public static class LimitedScalarParameter extends ScalarParameter {
        @SuppressWarnings("unchecked")
        public LimitedScalarParameter(String name, Object value) {
            super(name, value);
        }

        public static ZmpUtz111V001.LimitedScalarParameter IV_NODEPLOY(String value) {
            return new ZmpUtz111V001.LimitedScalarParameter("IV_NODEPLOY", value);
        }
    }
}
