package com.lenta.bp10.requests.network

import com.lenta.bp10.fmp.resources.fast.*
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

class FastResourcesMultiRequest @Inject constructor(val hyperHive: HyperHive) : CoreResourcesMultiRequest() {

    override val isDeltaRequest = true

    override fun getMapOfRequests(): Map<String, RequestBuilder<out CustomParameter, out ScalarParameter<Any>>> {
        return mapOf(
                ZmpUtz07V001.NAME_RESOURCE to ZmpUtz07V001(hyperHive).newRequest(),
                ZmpUtz14V001.NAME_RESOURCE to ZmpUtz14V001(hyperHive).newRequest(),
                ZmpUtz26V001.NAME_RESOURCE to ZmpUtz26V001(hyperHive).newRequest(),
                ZmpUtz31V001.NAME_RESOURCE to ZmpUtz31V001(hyperHive).newRequest(),
                ZmpUtz32V001.NAME_RESOURCE to ZmpUtz32V001(hyperHive).newRequest(),
                ZmpUtz33V001.NAME_RESOURCE to ZmpUtz33V001(hyperHive).newRequest(),
                ZmpUtz34V001.NAME_RESOURCE to ZmpUtz34V001(hyperHive).newRequest(),
                ZmpUtz36V001.NAME_RESOURCE to ZmpUtz36V001(hyperHive).newRequest(),
                ZmpUtz38V001.NAME_RESOURCE to ZmpUtz38V001(hyperHive).newRequest(),


                //TODO удалить загрузку SlowData после добавления в FMP sdk возможности обращения к базе во время синхронизации

                ZmpUtz46V001.NAME_RESOURCE to ZmpUtz46V001(hyperHive).newRequest(),
                ZmpUtz25V001.NAME_RESOURCE to ZmpUtz25V001(hyperHive).newRequest(),
                ZfmpUtz48V001.NAME_RESOURCE to ZfmpUtz48V001(hyperHive).newRequest()
        )
    }


}


