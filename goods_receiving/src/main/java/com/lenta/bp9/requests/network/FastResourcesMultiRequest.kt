package com.lenta.bp9.requests.network

import com.lenta.shared.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
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
                ZmpUtz07V001.NAME_RESOURCE to ZmpUtz07V001(hyperHive).newRequest(),
                ZmpUtz14V001.NAME_RESOURCE to ZmpUtz14V001(hyperHive).newRequest(),
                ZmpUtz17V001.NAME_RESOURCE to ZmpUtz17V001(hyperHive).newRequest(),
                ZmpUtz20V001.NAME_RESOURCE to ZmpUtz20V001(hyperHive).newRequest(),
                ZmpUtz25V001.NAME_RESOURCE to ZmpUtz25V001(hyperHive).newRequest(),
                ZmpUtz26V001.NAME_RESOURCE to ZmpUtz26V001(hyperHive).newRequest(),
                ZfmpUtz48V001.NAME_RESOURCE to ZfmpUtz48V001(hyperHive).newRequest()
        )
    }
}







