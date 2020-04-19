package com.lenta.movement.requests.network

import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz38V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001
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
            ZmpUtz14V001.NAME_RESOURCE to ZmpUtz14V001(hyperHive).newRequest(),
            ZmpUtz26V001.NAME_RESOURCE to ZmpUtz26V001(hyperHive).newRequest(),
            ZmpUtz38V001.NAME_RESOURCE to ZmpUtz38V001(hyperHive).newRequest(),
            // TODO MB_S_45
            // TODO MB_S_47
            // TODO MB_S_48
            // TODO MB_S_49
            // TODO MB_S_50
            // TODO MB_S_51

            //TODO удалить загрузку SlowData после добавления в FMP sdk возможности обращения к базе во время синхронизации

            ZmpUtz25V001.NAME_RESOURCE to ZmpUtz25V001(hyperHive).newRequest()
            // TODO MB_S_30
            // TODO MB_S_22
        )
    }


}


