package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_PROPERTIES таблица свойств для маркированного товара
data class TaskMarkingGoodsProperties (
    val ean: String,
    val properties: String,
    val value: String
) {
    companion object {
        fun from(restData: TaskMarkingGoodsPropertiesRestData): TaskMarkingGoodsProperties {
            return TaskMarkingGoodsProperties(
                    ean = restData.ean.orEmpty(),
                    properties = restData.properties.orEmpty(),
                    value = restData.value.orEmpty()
            )
        }
    }
}


data class TaskMarkingGoodsPropertiesRestData(
        @SerializedName("EAN")
        val ean: String?, //Номер набора
        @SerializedName("PROPERTIES", alternate = ["NAME"])
        val properties: String?, // название свойства
        @SerializedName("VALUE")
        val value: String? //значение для свойства
)