package com.lenta.bp18.repository

import com.lenta.bp18.model.pojo.Good
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz110V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz111V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
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
        private val hyperHive: HyperHive
) : IDatabaseRepo {

    private val stores: ZmpUtz23V001 by lazy { ZmpUtz23V001(hyperHive) } // Список магазинов
    private val productInfo: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) } // Информация о товаре
    private val barCodeInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Информация о штрих-коде
    private val groupInfo: ZmpUtz110V001 by lazy { ZmpUtz110V001(hyperHive) } //Информация о группе весового оборудования
    private val conditionInfo: ZmpUtz111V001 by lazy { ZmpUtz111V001(hyperHive) } //Список условий хранения
    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения

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
                                name = unitName.orEmpty())
                )
            }
        }
    }

    override suspend fun getEanInfoByEan(ean: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            barCodeInfo.getEanInfo(ean)?.toEanInfo()
        }
    }

    override suspend fun getEanInfoByMaterial(material: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            barCodeInfo.getEanInfoFromMaterial(material)?.toEanInfo()
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

    override suspend fun getMarketByNumber(tkNumber: String): MarketInfo? {
        return withContext(Dispatchers.IO) {
            stores.getMarketByNumber(tkNumber)?.toMarketInfo()
        }
    }

    override suspend fun getAllGoodGroup(): List<GroupInfo> {
        return withContext(Dispatchers.IO) {
            groupInfo.getAllGroups().toGroupInfoList()
        }
    }

    override suspend fun getGroupToSelectedMarket(marketNumber: String): List<GroupInfo> {
        return withContext(Dispatchers.IO){
            groupInfo.getGroupsToSelectedMarket(marketNumber).toGroupInfoList()
        }
    }

    override suspend fun getConditionByName(good: String?): List<ConditionInfo> {
        return withContext(Dispatchers.IO) {
            conditionInfo.getConditionByName(good).toConditionInfoList()
        }
    }

    override suspend fun getAllCondition(): List<ConditionInfo>{
        return withContext(Dispatchers.IO){
            conditionInfo.getAllCondition().toConditionInfoList()
        }
    }
}


interface IDatabaseRepo {
    suspend fun getEanInfoByEan(ean: String?): EanInfo?
    suspend fun getEanInfoByMaterial(material: String?): EanInfo?
    suspend fun getProductInfoByMaterial(material: String?): ProductInfo?
    suspend fun getGoodUnitName(unitCode: String?): String?
    suspend fun getGoodByEan(ean: String): Good?
    suspend fun getAllMarkets(): List<MarketInfo>
    suspend fun getMarketByNumber(tkNumber: String): MarketInfo?
    suspend fun getConditionByName(good: String?): List<ConditionInfo>
    suspend fun getAllGoodGroup(): List<GroupInfo>
    suspend fun getGroupToSelectedMarket(marketNumber: String): List<GroupInfo>
    suspend fun getAllCondition(): List<ConditionInfo>
}