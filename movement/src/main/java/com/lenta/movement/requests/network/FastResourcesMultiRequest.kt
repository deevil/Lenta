package com.lenta.movement.requests.network

import com.lenta.movement.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz38V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz22V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.requests.network.CoreResourcesMultiRequest
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import javax.inject.Inject

class FastResourcesMultiRequest @Inject constructor(
        private val hyperHive: HyperHive
) : CoreResourcesMultiRequest() {

    override val isDeltaRequest = true

    override fun getMapOfRequests(): Map<String, RequestBuilder<out CustomParameter, out ScalarParameter<Any>>> {
        return mapOf(
                ZmpUtz07V001.NAME_RESOURCE to ZmpUtz07V001(hyperHive).newRequest(),
                ZmpUtz14V001.NAME_RESOURCE to ZmpUtz14V001(hyperHive).newRequest(),
                ZmpUtz26V001.NAME_RESOURCE to ZmpUtz26V001(hyperHive).newRequest(),
                ZmpUtz38V001.NAME_RESOURCE to ZmpUtz38V001(hyperHive).newRequest(),
                ZmpUtz47V001.NAME_RESOURCE to ZmpUtz47V001(hyperHive).newRequest(),
                ZmpUtz48V001.NAME_RESOURCE to ZmpUtz48V001(hyperHive).newRequest(),
                ZmpUtz49V001.NAME_RESOURCE to ZmpUtz49V001(hyperHive).newRequest(),
                ZmpUtz50V001.NAME_RESOURCE to ZmpUtz50V001(hyperHive).newRequest(),
                ZmpUtz79V001.NAME_RESOURCE to ZmpUtz79V001(hyperHive).newRequest(),
                ZmpUtz25V001.NAME_RESOURCE to ZmpUtz25V001(hyperHive).newRequest(),
                ZmpUtz30V001.NAME_RESOURCE to ZmpUtz30V001(hyperHive).newRequest(),
                ZmpUtz22V001.NAME_RESOURCE to ZmpUtz22V001(hyperHive).newRequest()
        )
    }


}


