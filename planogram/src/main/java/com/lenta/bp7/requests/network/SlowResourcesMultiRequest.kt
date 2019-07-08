package com.lenta.bp7.requests.network


import com.lenta.shared.fmp.resources.slow.ZmpUtz24V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
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
                ZmpUtz24V001(hyperHive).newRequest(),
                ZmpUtz25V001(hyperHive).newRequest()
        )
    }
}
