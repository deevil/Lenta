package com.lenta.bp7.requests.network

import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz24V001
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
                ZmpUtz23V001.NAME_RESOURCE to ZmpUtz23V001(hyperHive).newRequest(),

                //TODO удалить загрузку SlowData после добавления в FMP sdk возможности обращения к базе во время синхронизации
                ZmpUtz24V001.NAME_RESOURCE to ZmpUtz24V001(hyperHive).newRequest(),
                ZmpUtz25V001.NAME_RESOURCE to ZmpUtz25V001(hyperHive).newRequest()
                )
    }
}


