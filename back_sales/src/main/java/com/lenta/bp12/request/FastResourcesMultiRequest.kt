package com.lenta.bp12.request

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
                ZmpUtz07V001.NAME_RESOURCE to ZmpUtz07V001(hyperHive).newRequest(), // ZMP_UTZ_07_V001 Единицы измерения
                ZmpUtz14V001.NAME_RESOURCE to ZmpUtz14V001(hyperHive).newRequest(), // ZMP_UTZ_14_V001 Настройки
                ZmpUtz17V001.NAME_RESOURCE to ZmpUtz17V001(hyperHive).newRequest(), // ZMP_UTZ_17_V001 Список значений словаря данных УТЗ ТСД
                ZmpUtz26V001.NAME_RESOURCE to ZmpUtz26V001(hyperHive).newRequest(), // ZMP_UTZ_26_V001 Справочник принтеров
                ZmpUtz38V001.NAME_RESOURCE to ZmpUtz38V001(hyperHive).newRequest(), // ZMP_UTZ_38_V001 Справочник пиктограмм
                ZmpUtz39V001.NAME_RESOURCE to ZmpUtz39V001(hyperHive).newRequest(), // ZMP_UTZ_39_V001 Справочник BKS – настройки типов заданий
                ZmpUtz40V001.NAME_RESOURCE to ZmpUtz40V001(hyperHive).newRequest(), // ZMP_UTZ_40_V001 Справочник BKS - склад отправитель для типов заданий
                ZmpUtz41V001.NAME_RESOURCE to ZmpUtz41V001(hyperHive).newRequest(), // ZMP_UTZ_41_V001 Справочник BKS – разрешенные товары для типов заданий
                ZmpUtz42V001.NAME_RESOURCE to ZmpUtz42V001(hyperHive).newRequest(), // ZMP_UTZ_42_V001 Справочник BKS – запрещенные товары для типов заданий
                ZmpUtz44V001.NAME_RESOURCE to ZmpUtz44V001(hyperHive).newRequest(), // ZMP_UTZ_44_V001 Справочник причин возврата по типам заданий

                ZmpUtz109V001.NAME_RESOURCE to ZmpUtz109V001(hyperHive).newRequest(), // группы маркировки

                ZmpUtz09V001.NAME_RESOURCE to ZmpUtz09V001(hyperHive).newRequest(), // ZMP_UTZ_09_V001 Справочник наименования поставщиков
                ZmpUtz22V001.NAME_RESOURCE to ZmpUtz22V001(hyperHive).newRequest(), // ZMP_UTZ_22_V001 Справочник алкогольных товаров
                ZmpUtz25V001.NAME_RESOURCE to ZmpUtz25V001(hyperHive).newRequest(), // ZMP_UTZ_25_V001 Справочник штрих-кодов
                ZmpUtz43V001.NAME_RESOURCE to ZmpUtz43V001(hyperHive).newRequest(), // ZMP_UTZ_43_V001 Справочник наименования производителей
                ZfmpUtz48V001.NAME_RESOURCE to ZfmpUtz48V001(hyperHive).newRequest() // ZFMP_UTZ_48_V002 Справочник товаров

        )
    }

}