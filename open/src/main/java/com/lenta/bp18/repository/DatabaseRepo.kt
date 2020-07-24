package com.lenta.bp18.repository

import com.lenta.bp18.model.pojo.Good
import com.lenta.bp18.platform.Constants
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.pojo.*
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AppScope
class DatabaseRepo(
        hyperHive: HyperHive,
        val units: ZmpUtz07V001 = ZmpUtz07V001(hyperHive), // Единицы измерения
        val settings: ZmpUtz14V001 = ZmpUtz14V001(hyperHive), // Настройки
        val stores: ZmpUtz23V001 = ZmpUtz23V001(hyperHive), // Список магазинов
        val productInfo: ZfmpUtz48V001 = ZfmpUtz48V001(hyperHive), // Информация о товаре
        val barCodeInfo: ZmpUtz25V001 = ZmpUtz25V001(hyperHive), // Информация о штрих-коде
        val groupInfo: ZmpUtz110V001 = ZmpUtz110V001(hyperHive), //Информация о группе весового оборудования
        val conditionInfo: ZmpUtz111V001 = ZmpUtz111V001(hyperHive) //Список условий хранения
) : IDatabaseRepo {

    override suspend fun getGoodByEan(ean: String): Good? {
        return withContext(Dispatchers.IO) {
            getEanInfoByEan(ean)?.run {
                val productInfo = getProductInfoByMaterial(this.materialNumber)
                val unitName = getGoodUnitName(productInfo?.buom)
                Good(
                        ean = ean,
                        material = this.materialNumber,
                        matcode = productInfo?.matcode.orEmpty(),
                        name = productInfo?.name.orEmpty(),
                        uom = Uom(
                                code = productInfo?.buom.orEmpty(),
                                name = unitName.orEmpty()))
            }
        }
    }

    override suspend fun getEanInfoByEan(ean: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            //val allData = barCodeInfo.localHelper_ET_EANS.all.takeLast(100)
            barCodeInfo.getEanInfo(ean)?.toEanInfo()
        }
    }

    override suspend fun getProductInfoByMaterial(material: String?): ProductInfo? {
        return withContext(Dispatchers.IO) {
            productInfo.getProductInfoByMaterial(material)?.toMaterialInfo()
        }
    }

    override suspend fun getGoodUnitName(unitCode: String?): String? {
        return withContext(Dispatchers.IO) {
            units.getUnitName(unitCode)
        }
    }

    override suspend fun getAllMarkets(): List<MarketInfo> {
        return withContext(Dispatchers.IO) {
            stores.getAllMarkets().toMarketInfoList()
        }
    }

    override suspend fun getAllGoodGroup(): List<GroupInfo> {
        return withContext(Dispatchers.IO) {
            groupInfo.getAllGroups().toGroupInfoList()
        }
    }

    override suspend fun getAllGoodCondition(): List<ConditionInfo> {
        return withContext(Dispatchers.IO) {
            conditionInfo.getAllConditions().toConditionInfoList()
        }
    }

}


interface IDatabaseRepo {
    suspend fun getEanInfoByEan(ean: String?): EanInfo?
    suspend fun getProductInfoByMaterial(material: String?): ProductInfo?
    suspend fun getGoodUnitName(unitCode: String?): String?
    suspend fun getGoodByEan(ean: String): Good?
    suspend fun getAllMarkets(): List<MarketInfo>
    suspend fun getAllGoodCondition(): List<ConditionInfo>
    suspend fun getAllGoodGroup(): List<GroupInfo>
}