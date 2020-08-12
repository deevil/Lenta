package com.lenta.bp18.request

import com.lenta.shared.fmp.resources.fast.*
import com.lenta.shared.requests.network.CoreResourcesMultiRequest
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import javax.inject.Inject

class FastResourcesMultiRequest @Inject constructor(val hyperHive: HyperHive) : CoreResourcesMultiRequest() {

    override val isDeltaRequest = true

    override fun getMapOfRequests(): Map<String, RequestBuilder<out CustomParameter, out ScalarParameter<Any>>> {
        return mapOf(
                /*Справочник групп весового оборудования*/
                ZmpUtz110V001.NAME_RESOURCE to ZmpUtz110V001(hyperHive).newRequest(),
                /*Справочник условий хранения*/
                ZmpUtz111V001.NAME_RESOURCE to ZmpUtz111V001(hyperHive).newRequest(),
                /*Справочник ТК*/
                ZmpUtz23V001.NAME_RESOURCE to ZmpUtz23V001(hyperHive).newRequest()
        )
    }
}