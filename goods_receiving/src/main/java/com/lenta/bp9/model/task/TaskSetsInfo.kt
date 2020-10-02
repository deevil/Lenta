package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_TASK_SETS Список наборов
data class TaskSetsInfo (
    val setNumber: String,
    val componentNumber: String,
    val quantity: Double,
    val uom: Uom
) {
    companion object {
        suspend fun from(hyperHive: HyperHive, restData: TaskSetsRestData): TaskSetsInfo {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val uomInfo = zmpUtz07V001.getUomInfo(restData.uomCode)
                return@withContext TaskSetsInfo(
                        setNumber = restData.setNumber.orEmpty(),
                        componentNumber = restData.componentNumber.orEmpty(),
                        quantity = restData.quantity?.toDouble() ?: 0.0,
                        uom = Uom(code = uomInfo?.uom.orEmpty(), name = uomInfo?.name.orEmpty())
                )
            }
        }
    }
}


data class TaskSetsRestData(
        @SerializedName("MATNR_OSN")
        val setNumber: String?, //Номер набора
        @SerializedName("MATNR")
        val componentNumber: String?, //Номер компонента
        @SerializedName("MENGE")
        val quantity: String?, //Количество вложенного
        @SerializedName("MEINS")
        val uomCode: String? //Базисная единица измерения
)