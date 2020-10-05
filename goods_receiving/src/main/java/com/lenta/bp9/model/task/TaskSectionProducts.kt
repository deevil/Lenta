package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_MATNR_ABTNR Таблица списка товаров по секциям
data class TaskSectionProducts(
        val sectionNumber: String, //Номер секции
        val materialNumber: String, //Номер товара
        val materialName: String, //наименование товара
        val quantity: Double, //Количество
        val uom: Uom //базисная единица измерения
        ) {
        companion object {
                suspend fun from(hyperHive: HyperHive, restData: TaskSectionProductsRestData): TaskSectionProducts {
                        return withContext(Dispatchers.IO) {
                                val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
                                        ZfmpUtz48V001(hyperHive)
                                }
                                val materialInfo = zfmpUtz48V001.getProductInfoByMaterial(restData.materialNumber)

                                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                                        ZmpUtz07V001(hyperHive)
                                }
                                val uomInfo = zmpUtz07V001.getUomInfo(restData.uom)
                                return@withContext TaskSectionProducts(
                                        sectionNumber = restData.sectionNumber.orEmpty(),
                                        materialNumber = restData.materialNumber.orEmpty(),
                                        materialName = materialInfo?.name.orEmpty(),
                                        quantity = restData.quantity?.toDouble() ?: 0.0,
                                        uom = Uom(code = uomInfo?.uom.orEmpty(), name = uomInfo?.name.orEmpty())
                                )
                        }
                }
        }
}

data class TaskSectionProductsRestData(
        @SerializedName("ABTNR")
        val sectionNumber: String?, //Номер секции
        @SerializedName("MATNR")
        val materialNumber: String?, //Номер товара
        @SerializedName("MENGE")
        val quantity: String?, //Количество
        @SerializedName("MEINS")
        val uom: String? //базисная единица измерения
)