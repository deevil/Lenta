package com.lenta.bp14.models.check_list.repo

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.check_list.Good
import com.lenta.bp14.platform.extentions.CheckListGoodInfo
import com.lenta.bp14.platform.extentions.getQuantity
import com.lenta.bp14.platform.extentions.toCheckListGoodInfo
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import com.lenta.shared.utilities.extentions.dropZeros
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckListRepo @Inject constructor(
        hyperHive: HyperHive
) : ICheckListRepo {

    val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения
    val productInfo: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) } // Информация о товаре
    val eanInfo: ZmpUtz25V001 = ZmpUtz25V001(hyperHive) // Информация о штрих-коде

    override suspend fun getGoodByMaterial(material: String): Good? {
        return withContext(Dispatchers.IO) {
            getGoodInfoByMaterial(material)?.let { goodInfo ->
                val defaultUnits = Uom(code = goodInfo.unitsCode, name = getUnitsName(goodInfo.unitsCode))
                val units = if (defaultUnits == Uom.G) Uom.KG else defaultUnits
                val ean = getEanByMaterialUnits(material, defaultUnits.code)

                return@withContext Good(
                        ean = ean,
                        material = goodInfo.material,
                        name = goodInfo.name,
                        defaultUnits = defaultUnits,
                        units = units,
                        quantity = MutableLiveData("0")
                )
            }

            return@withContext null
        }
    }

    override suspend fun getGoodByEan(ean: String): Good? {
        val scanCodeInfo = ScanCodeInfo(ean)

        return withContext(Dispatchers.IO) {
            getMaterialByEan(scanCodeInfo.eanWithoutWeight)?.let { eanInfo ->
                getGoodInfoByMaterial(eanInfo.materialNumber)?.let { goodInfo ->
                    val defaultUnits = Uom(code = goodInfo.unitsCode, name = getUnitsName(goodInfo.unitsCode))
                    val units = if (defaultUnits == Uom.G) Uom.KG else defaultUnits
                    val quantity = scanCodeInfo.getQuantity(defaultUnits)

                    return@withContext Good(
                            ean = eanInfo.ean,
                            material = goodInfo.material,
                            name = goodInfo.name,
                            defaultUnits = defaultUnits,
                            units = units,
                            quantity = MutableLiveData(quantity.dropZeros())
                    )
                }
            }

            return@withContext null
        }
    }

    private suspend fun getMaterialByEan(ean: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext eanInfo.getEanInfo(ean)?.toEanInfo()
        }
    }


    private suspend fun getGoodInfoByMaterial(material: String?): CheckListGoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext productInfo.getProductInfoByMaterial(material)?.toCheckListGoodInfo()
        }
    }

    private suspend fun getUnitsName(code: String?): String {
        return withContext(Dispatchers.IO) {
            return@withContext units.getUnitName(code) ?: ""
        }
    }

    private suspend fun getEanByMaterialUnits(material: String, unitsCode: String): String? {
        return withContext(Dispatchers.IO) {
            return@withContext eanInfo.getEanInfoByMaterialUnits(material, unitsCode)?.toEanInfo()?.ean
        }
    }

}

interface ICheckListRepo {
    suspend fun getGoodByEan(ean: String): Good?
    suspend fun getGoodByMaterial(material: String): Good?
}