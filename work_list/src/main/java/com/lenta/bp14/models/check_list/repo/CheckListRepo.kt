package com.lenta.bp14.models.check_list.repo

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.check_list.Good
import com.lenta.bp14.platform.extentions.CheckListGoodInfo
import com.lenta.bp14.platform.extentions.toCheckListGoodInfo
import com.lenta.shared.fmp.resources.dao_ext.getEanInfo
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUnitName
import com.lenta.shared.fmp.resources.dao_ext.toEanInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import com.lenta.shared.utilities.extentions.toStringFormatted
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

    override suspend fun getGoodByMaterial(material: String, scanCodeInfo: ScanCodeInfo?, eanInfo: EanInfo?): Good? {
        return withContext(Dispatchers.IO) {
            getCheckListGoodInfoByMaterial(material)?.let { goodInfo ->
                //TODO реализовать конвертацию грамм в килограммы. Учесть отправку отчетов
                //val isGrammUom = goodInfo.buom == Uom.G.code
                val isGrammUom = false
                val unitsCode = if (isGrammUom) Uom.KG.code else goodInfo.buom
                val unitsName = getUnitsName(unitsCode)

                var quantity = 0.0

                if (scanCodeInfo != null && eanInfo != null) {
                    quantity = scanCodeInfo.extractQuantityFromEan(
                            eanInfo
                    )
                }

                return@withContext Good(
                        material = goodInfo.material,
                        name = goodInfo.name,
                        quantity = MutableLiveData((if (isGrammUom) quantity / 1000 else quantity).toStringFormatted()),
                        units = Uom(
                                code = unitsCode,
                                name = unitsName ?: "")
                )

            }

            return@withContext null
        }
    }

    override suspend fun getGoodByEan(ean: String): Good? {
        val scanCodeInfo = ScanCodeInfo(ean, null)
        eanInfo.getEanInfo(scanCodeInfo.eanNumberForSearch ?: ean)?.toEanInfo()?.let {
            return withContext(Dispatchers.IO) {
                return@withContext getGoodByMaterial(it.materialNumber, scanCodeInfo, eanInfo = it)
            }
        }
        return null
    }


    override suspend fun getCheckListGoodInfoByMaterial(material: String?): CheckListGoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext productInfo.getProductInfoByMaterial(material)?.toCheckListGoodInfo()
        }
    }

    override suspend fun getUnitsName(code: String?): String? {
        return withContext(Dispatchers.IO) {
            return@withContext units.getUnitName(code)
        }
    }

}

interface ICheckListRepo {
    suspend fun getGoodByEan(ean: String): Good?
    suspend fun getGoodByMaterial(material: String, scanCodeInfo: ScanCodeInfo? = null, eanInfo: EanInfo? = null): Good?

    suspend fun getCheckListGoodInfoByMaterial(material: String?): CheckListGoodInfo?
    suspend fun getUnitsName(code: String?): String?
}