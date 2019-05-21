package com.lenta.bp10.requests.network

import com.lenta.bp10.fmp.resources.fast.*
import com.lenta.bp10.fmp.resources.slow.ZmpUtz25V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz30V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.requests.network.CoreResourcesMultiRequest
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import javax.inject.Inject

class FastResourcesMultiRequest @Inject constructor(val hyperHive: HyperHive) : CoreResourcesMultiRequest() {

    override val isDeltaRequest = true

    override fun getListOfRequests(): List<RequestBuilder<out CustomParameter, out ScalarParameter<Any>>> {
        return arrayListOf(
                ZmpUtz07V001(hyperHive).newRequest(),
                ZmpUtz14V001(hyperHive).newRequest(),
                ZmpUtz26V001(hyperHive).newRequest(),
                ZmpUtz26V001(hyperHive).newRequest(),
                ZmpUtz31V001(hyperHive).newRequest(),
                ZmpUtz32V001(hyperHive).newRequest(),
                ZmpUtz33V001(hyperHive).newRequest(),
                ZmpUtz34V001(hyperHive).newRequest(),
                ZmpUtz36V001(hyperHive).newRequest(),
                ZmpUtz38V001(hyperHive).newRequest(),

                //TODO удалить загрузку SlowData после добавления в FMP sdk возможности обращения к базе во время синхронизации

                ZmpUtz46V001(hyperHive).newRequest(),
                ZmpUtz25V001(hyperHive).newRequest(),
                ZmpUtz30V001(hyperHive).newRequest()
        )
    }


}


