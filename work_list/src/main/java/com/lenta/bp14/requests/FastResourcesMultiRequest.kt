package com.lenta.bp14.requests

import com.lenta.bp14.fmp.resources.ZfmpUtz49V001
import com.lenta.bp14.fmp.resources.ZfmpUtz50V001
import com.lenta.bp14.fmp.resources.ZfmpUtz51V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz38V001
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
                ZmpUtz23V001.NAME_RESOURCE to ZmpUtz23V001(hyperHive).newRequest(),
                ZmpUtz38V001.NAME_RESOURCE to ZmpUtz38V001(hyperHive).newRequest(),
                ZfmpUtz49V001.NAME_RESOURCE to ZfmpUtz49V001(hyperHive).newRequest(),
                ZfmpUtz50V001.NAME_RESOURCE to ZfmpUtz50V001(hyperHive).newRequest(),
                ZfmpUtz51V001.NAME_RESOURCE to ZfmpUtz51V001(hyperHive).newRequest(),

                //TODO удалить загрузку SlowData после добавления в FMP sdk возможности обращения к базе во время синхронизации
                ZfmpUtz48V001.NAME_RESOURCE to ZfmpUtz48V001(hyperHive).newRequest(),
                ZmpUtz25V001.NAME_RESOURCE to ZmpUtz25V001(hyperHive).newRequest()
        )
    }
}