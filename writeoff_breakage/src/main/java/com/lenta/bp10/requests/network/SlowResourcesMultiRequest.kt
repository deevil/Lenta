package com.lenta.bp10.requests.network


import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.requests.network.CoreResourcesMultiRequest
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import javax.inject.Inject

class SlowResourcesMultiRequest @Inject constructor(private val hyperHive: HyperHive) : CoreResourcesMultiRequest() {
    override val isDeltaRequest = true

    override fun getMapOfRequests(): Map<String, RequestBuilder<out CustomParameter, out ScalarParameter<Any>>> {
        return mapOf(
                ZmpUtz25V001.NAME_RESOURCE to ZmpUtz25V001(hyperHive).newRequest(),
                ZfmpUtz48V001.NAME_RESOURCE to ZfmpUtz48V001(hyperHive).newRequest(),
                ZmpUtz46V001.NAME_RESOURCE to ZmpUtz46V001(hyperHive).newRequest()
        )
    }


}

