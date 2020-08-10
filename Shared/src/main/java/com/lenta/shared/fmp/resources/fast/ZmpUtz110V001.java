package com.lenta.shared.fmp.resources.fast;

import com.google.gson.annotations.SerializedName;
import com.mobrun.plugin.api.HyperHive;
import com.mobrun.plugin.api.helper.LocalTableResourceHelper;
import com.mobrun.plugin.api.request_assistant.CustomParameter;
import com.mobrun.plugin.api.request_assistant.RequestBuilder;
import com.mobrun.plugin.api.request_assistant.ScalarParameter;
import com.mobrun.plugin.models.StatusSelectTable;

@SuppressWarnings("ALL")
public class ZmpUtz110V001 {

    public static final String NAME_RESOURCE = "ZMP_UTZ_110_V001";
    public static final String NAME_OUT_PARAM_ET_PARAMS = "ET_GR_WEIGHT";
    public static final String LIFE_TIME = "1 day, 0:00:00";

    private final HyperHive hyperHive;

    public final LocalTableResourceHelper<ZmpUtz110V001.ItemLocal_ET_GR_WEIGHT, ZmpUtz110V001.Status_ET_GR_WEIGHT> localHelper_ET_GR_WEIGHT;


    public ZmpUtz110V001(HyperHive hyperHive) {

        this.hyperHive = hyperHive;

        localHelper_ET_GR_WEIGHT =
                new LocalTableResourceHelper<ZmpUtz110V001.ItemLocal_ET_GR_WEIGHT, ZmpUtz110V001.Status_ET_GR_WEIGHT>(NAME_RESOURCE,
                        NAME_OUT_PARAM_ET_PARAMS,
                        hyperHive,
                        ZmpUtz110V001.Status_ET_GR_WEIGHT.class);

    }

    public RequestBuilder<ZmpUtz110V001.Params, ZmpUtz110V001.LimitedScalarParameter> newRequest() {
        return new RequestBuilder<ZmpUtz110V001.Params, ZmpUtz110V001.LimitedScalarParameter>(hyperHive, NAME_RESOURCE, true);
    }

    static final class Status_ET_GR_WEIGHT extends StatusSelectTable<ZmpUtz110V001.ItemLocal_ET_GR_WEIGHT> {
    }

    public static class ItemLocal_ET_GR_WEIGHT {

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        /*Код предприятия*/
        @SerializedName("WERKS_D")
        public String werks;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        /*Код группы весового оборудования*/
        @SerializedName("GRNUM")
        public String grnum;

        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        /*Наименование группы весового оборудования*/
        @SerializedName("GRNAME")
        public String grname;

    }

    public interface Params extends CustomParameter {
    }

    public static class LimitedScalarParameter extends ScalarParameter {
        @SuppressWarnings("unchecked")
        public LimitedScalarParameter(String name, Object value) {
            super(name, value);
        }

        public static ZmpUtz110V001.LimitedScalarParameter IV_NODEPLOY(String value) {
            return new ZmpUtz110V001.LimitedScalarParameter("IV_NODEPLOY", value);
        }
    }
}
