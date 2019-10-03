package com.lenta.bp14.models.check_list.repo

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.check_list.Good
import com.lenta.bp14.platform.extentions.CheckListGoodInfo
import com.lenta.bp14.platform.extentions.toCheckListGoodInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUnitName
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AppScope
class CheckListRepo(
        hyperHive: HyperHive,
        val units: ZmpUtz07V001 = ZmpUtz07V001(hyperHive), // Единицы измерения
        //val settings: ZmpUtz14V001 = ZmpUtz14V001(hyperHive), // Настройки
        //val stores: ZmpUtz23V001 = ZmpUtz23V001(hyperHive), // Список магазинов
        val productInfo: ZfmpUtz48V001 = ZfmpUtz48V001(hyperHive), // Информация о товаре
        val barCodeInfo: ZmpUtz25V001 = ZmpUtz25V001(hyperHive) // Информация о штрих-коде
) : ICheckListRepo {

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

    override fun getGoodByEan(ean: String): Good? {
        return null
    }

}

interface ICheckListRepo {
    suspend fun getCheckListGoodInfoByMaterial(material: String?): CheckListGoodInfo?




    suspend fun getGoodByMaterial(material: String): Good?

    fun getGoodByEan(ean: String): Good?

    suspend fun getUnitsName(code: String?): String?
}