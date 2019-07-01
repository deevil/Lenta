package com.lenta.bp10.features.good_information

import com.lenta.bp10.fmp.resources.dao_ext.getDefaultReason
import com.lenta.bp10.fmp.resources.dao_ext.getLimit
import com.lenta.bp10.fmp.resources.fast.ZmpUtz31V001
import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.getMaterial
import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.models.core.ProductType
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AppScope
class GoodInformationRepo(hyperHive: HyperHive) : IGoodInformationRepo {

    val zmpUt29V001 by lazy { ZmpUtz29V001Rfc(hyperHive) }
    val zmpUtz30V001 by lazy { ZmpUtz30V001(hyperHive) }
    val zmpUtz31V001 by lazy { ZmpUtz31V001(hyperHive) }

    override suspend fun getDefaultReason(taskType: String, sectionId: String, materialNumber: String): String {
        return withContext(Dispatchers.IO) {
            return@withContext zmpUtz31V001.getDefaultReason(
                    taskType = taskType,
                    sectionId = sectionId,
                    material = zmpUtz30V001.getMaterial(materialNumber))
        }
    }

    override suspend fun getLimit(taskTypeCode: String, productType: ProductType): Double {
        if (productType == ProductType.General) {
            return 0.0
        }
        return withContext(Dispatchers.IO) {
            return@withContext zmpUt29V001.getLimit(
                    taskTypeCode = taskTypeCode)
        }
    }
}

interface IGoodInformationRepo {
    suspend fun getDefaultReason(taskType: String, sectionId: String, materialNumber: String): String
    suspend fun getLimit(taskTypeCode: String, productType: ProductType): Double
}