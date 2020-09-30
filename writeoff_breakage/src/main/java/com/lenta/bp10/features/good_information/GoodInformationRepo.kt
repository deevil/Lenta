package com.lenta.bp10.features.good_information

import com.lenta.bp10.fmp.resources.dao_ext.getDefaultReason
import com.lenta.bp10.fmp.resources.dao_ext.getReasonPosition
import com.lenta.bp10.fmp.resources.fast.ZmpUtz31V001
import com.lenta.bp10.repos.IRepoInMemoryHolder
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.ProductType
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AppScope
class GoodInformationRepo(
        hyperHive: HyperHive,
        private val repoInMemoryHolder: IRepoInMemoryHolder
) : IGoodInformationRepo {

    val zfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) }
    val zmpUtz31V001 by lazy { ZmpUtz31V001(hyperHive) }

    override suspend fun getDefaultReason(taskType: String, sectionId: String, materialNumber: String): String {
        return withContext(Dispatchers.IO) {
            return@withContext zmpUtz31V001.getDefaultReason(
                    taskType = taskType,
                    sectionId = sectionId,
                    material = zfmpUtz48V001.getProductInfoByMaterial(materialNumber))
        }
    }

    override suspend fun getLimit(taskTypeCode: String, productType: ProductType): Double {
        if (productType == ProductType.General) {
            return 0.0
        }
        return withContext(Dispatchers.IO) {
            val allSettings = repoInMemoryHolder.userResourceResult?.taskSettings.orEmpty()
            allSettings.firstOrNull { it.taskType == taskTypeCode }?.limit ?: 0.0
        }
    }

    override suspend fun getStartReasonPosition(taskType: String, sectionId: String): String {
        return withContext(Dispatchers.IO) {
            zmpUtz31V001.getReasonPosition(taskType, sectionId)
        }
    }
}

interface IGoodInformationRepo {
    suspend fun getDefaultReason(taskType: String, sectionId: String, materialNumber: String): String
    suspend fun getLimit(taskTypeCode: String, productType: ProductType): Double
    suspend fun getStartReasonPosition(taskType: String, sectionId: String): String
}