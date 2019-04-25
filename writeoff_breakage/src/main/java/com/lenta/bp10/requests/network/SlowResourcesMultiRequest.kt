package com.lenta.bp10.requests.network

import com.lenta.bp10.fmp.resources.slow.ZmpUtz22V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz25V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.requests.network.CoreResourcesMultiRequest
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import javax.inject.Inject

class SlowResourcesMultiRequest @Inject constructor(private val hyperHive: HyperHive) : CoreResourcesMultiRequest() {
    override val isDeltaRequest = true

    override fun getListOfRequests(): List<RequestBuilder<out CustomParameter, out ScalarParameter<Any>>> {
        return listOf(
                ZmpUtz22V001(hyperHive).newRequest(),
                ZmpUtz25V001(hyperHive).newRequest(),
                ZmpUtz30V001(hyperHive).newRequest()
        )

    }


}

