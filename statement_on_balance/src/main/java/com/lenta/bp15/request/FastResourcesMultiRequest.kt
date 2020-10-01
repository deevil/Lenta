package com.lenta.bp15.request

import com.lenta.shared.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.slow.*
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
                ZmpUtz14V001.NAME_RESOURCE to ZmpUtz14V001(hyperHive).newRequest(), // ZMP_UTZ_14_V001 Настройки
                ZmpUtz38V001.NAME_RESOURCE to ZmpUtz38V001(hyperHive).newRequest(), // ZMP_UTZ_38_V001 Справочник пиктограмм
                ZfmpUtz52V001.NAME_RESOURCE to ZfmpUtz52V001(hyperHive).newRequest(), // ZFMP_UTZ_52_V001 Справочник типов заданий
                ZfmpUtz53V001.NAME_RESOURCE to ZfmpUtz53V001(hyperHive).newRequest(), // ZFMP_UTZ_53_V001 Справочник типов марок

                //ZmpUtz25V001.NAME_RESOURCE to ZmpUtz25V001(hyperHive).newRequest(), // ZMP_UTZ_25_V001 Справочник штрих-кодов
                ZfmpUtz48V001.NAME_RESOURCE to ZfmpUtz48V001(hyperHive).newRequest() // ZFMP_UTZ_48_V002 Справочник товаров
        )
    }

}