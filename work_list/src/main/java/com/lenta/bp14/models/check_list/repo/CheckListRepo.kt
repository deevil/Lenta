package com.lenta.bp14.models.check_list.repo

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.di.CheckListScope
import com.lenta.bp14.models.check_list.Good
import com.lenta.bp14.platform.extentions.CheckListGoodInfo
import com.lenta.bp14.platform.extentions.toCheckListGoodInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.getEanInfo
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUnitName
import com.lenta.shared.fmp.resources.dao_ext.toEanInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
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
            getCheckListGoodInfoByMaterial(material)?.let { checkListGoodInfo ->
                val unitsName = getUnitsName(checkListGoodInfo.buom)
                return@withContext Good(
                        material = checkListGoodInfo.material,
                        name = checkListGoodInfo.name,
                        quantity = MutableLiveData("1"),
                        units = Uom(
                                code = checkListGoodInfo.buom,
                                name = unitsName ?: ""))

            }
            return@withContext null
        }
    }

    override suspend fun getGoodByEan(ean: String): Good? {
        return withContext(Dispatchers.IO) {
            getEanInfoByEan(ean)?.let { eanInfo ->
                getCheckListGoodInfoByMaterial(eanInfo.materialNumber)?.let { checkListGoodInfo ->
                    val unitsName = getUnitsName(checkListGoodInfo.buom)
                    return@withContext Good(
                            ean = ean,
                            material = checkListGoodInfo.material,
                            name = checkListGoodInfo.name,
                            quantity = MutableLiveData("1"),
                            units = Uom(
                                    code = checkListGoodInfo.buom,
                                    name = unitsName ?: ""))

                }
                return@withContext null
            }
            return@withContext null
        }
    }

    override suspend fun getEanInfoByEan(ean: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext eanInfo.getEanInfo(ean)?.toEanInfo()
        }
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
    suspend fun getGoodByMaterial(material: String): Good?

    suspend fun getEanInfoByEan(ean: String?): EanInfo?
    suspend fun getCheckListGoodInfoByMaterial(material: String?): CheckListGoodInfo?
    suspend fun getUnitsName(code: String?): String?
}