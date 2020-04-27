package com.lenta.bp9.repos

import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz17V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz20V001
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataBaseRepo(
        hyperHive: HyperHive,
        private val zmpUtz14V001: ZmpUtz14V001 = ZmpUtz14V001(hyperHive), //параметры
        private val zmpUtz17V001: ZmpUtz17V001 = ZmpUtz17V001(hyperHive), //Качество
        private val zmpUtz20V001: ZmpUtz20V001 = ZmpUtz20V001(hyperHive) //Причины отказа

) : IDataBaseRepo {

    override suspend fun getQualityInfo(): List<QualityInfo>? = withContext(Dispatchers.IO) {
            zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
                it.id == "005"
            }
    }

    override suspend fun getQualityMercuryInfo(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "005" && it.code != "4"
        }
    }

    override suspend fun getQualityInfoForDiscrepancy(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "005" && it.code != "1"
        }
    }

    override suspend fun getQualityMercuryInfoForDiscrepancy(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "005" && !(it.code == "1" || it.code == "4")
        }
    }

    override suspend fun getReasonRejectionInfoOfQuality(quality: String): List<ReasonRejectionInfo>? = withContext(Dispatchers.IO) {
            zmpUtz20V001.getAllReasonRejection()?.toReasonRejectionInfoList()?.filter {
                it.id == "005" && it.qualityCode == quality
            }
    }

    override suspend fun getAllReasonRejectionInfo(): List<ReasonRejectionInfo>? = withContext(Dispatchers.IO) {
        zmpUtz20V001.getAllReasonRejection()?.toReasonRejectionInfoList()?.filter {
            it.id == "005"
        }
    }

    override suspend fun getTermControlInfo(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "007"
        }
    }

    override suspend fun getParamGrsGrundPos(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getGrsGrundPos()
    }

    override suspend fun getParamGrsGrundNeg(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getGrsGrundNeg()
    }

    override suspend fun getParamPermittedNumberDays(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getGrzArriveBackDD()
    }

    override suspend fun getParamGrzUffMhdhb(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getGrzUffMhdhb()
    }

    override suspend fun getParamGrwOlGrundcat(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getGrwOlGrundcat()
    }

    override suspend fun getParamGrwUlGrundcat(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getGrwUlGrundcat()
    }

    override suspend fun getParamGrzWerksOwnpr(): List<String>? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getGrzWerksOwnpr()
    }

    override suspend fun getParamGrzRoundLackRatio(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getParamGrzRoundLackRatio()
    }

    override suspend fun getParamGrzRoundLackUnit(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getParamGrzRoundLackUnit()
    }

    override suspend fun getParamGrzRoundHeapRatio(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getParamGrzRoundHeapRatio()
    }

    override suspend fun getParamGrzCrGrundcat(): String? = withContext(Dispatchers.IO) {
        zmpUtz14V001.getGrzCrGrundcat()
    }

    override suspend fun getExclusionFromIntegration(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "016"
        }
    }

    override suspend fun getAllStatusInfoForPRC(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "010"
        }
    }

    override suspend fun getStatusInfoForPRC(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "010" && it.code != "3"
        }
    }

    override suspend fun getSurplusInfoForPRC(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "010" && it.code == "3"
        }
    }

    override suspend fun getTypePalletInfo(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "014"
        }
    }

    override suspend fun getQualityInfoPGE(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "013"
        }
    }

    override suspend fun getQualityInfoPGENotRecountBreaking(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "013" && (it.code == "4" || it.code == "5")
        }
    }

    override suspend fun getSurplusInfoForPGE(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "013" && it.code == "2"
        }
    }

    override suspend fun getQualityInfoPGEForDiscrepancy(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "013" && (it.code == "3" || it.code == "4" || it.code == "5")
        }
    }

    override suspend fun getFailureReasons(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "011"
        }
    }

    override suspend fun getStatusInfoShipmentRC(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "018"
        }
    }

    override suspend fun getQualityInfoTransportMarriage(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "006" && it.code == "4"
        }
    }

    override suspend fun getQualityBoxesDefectInfo(): List<QualityInfo>? = withContext(Dispatchers.IO) {
        zmpUtz17V001.getAllQuality()?.toQualityInfoList()?.filter {
            it.id == "005" && it.code != "4"
        }
    }

    override suspend fun getGrzCrGrundcatName(code: String): String? = withContext(Dispatchers.IO) {
        zmpUtz20V001.getAllReasonRejection()?.toReasonRejectionInfoList()?.findLast {
            it.code == code
        }?.name
    }
}

interface IDataBaseRepo {
    suspend fun getQualityInfo(): List<QualityInfo>?
    suspend fun getQualityMercuryInfo(): List<QualityInfo>?
    suspend fun getQualityInfoForDiscrepancy(): List<QualityInfo>?
    suspend fun getQualityMercuryInfoForDiscrepancy(): List<QualityInfo>?
    suspend fun getReasonRejectionInfoOfQuality(quality: String): List<ReasonRejectionInfo>?
    suspend fun getAllReasonRejectionInfo(): List<ReasonRejectionInfo>?
    suspend fun getTermControlInfo(): List<QualityInfo>?
    suspend fun getParamGrsGrundPos(): String?
    suspend fun getParamGrsGrundNeg(): String?
    suspend fun getParamPermittedNumberDays(): String?
    suspend fun getParamGrzUffMhdhb(): String?
    suspend fun getParamGrwOlGrundcat(): String?
    suspend fun getParamGrwUlGrundcat(): String?
    suspend fun getParamGrzWerksOwnpr(): List<String>?
    suspend fun getParamGrzRoundLackRatio(): String?
    suspend fun getParamGrzRoundLackUnit(): String?
    suspend fun getParamGrzRoundHeapRatio(): String?
    suspend fun getParamGrzCrGrundcat(): String?
    suspend fun getExclusionFromIntegration(): List<QualityInfo>?
    suspend fun getAllStatusInfoForPRC(): List<QualityInfo>?
    suspend fun getStatusInfoForPRC(): List<QualityInfo>?
    suspend fun getSurplusInfoForPRC(): List<QualityInfo>?
    suspend fun getTypePalletInfo(): List<QualityInfo>?
    suspend fun getQualityInfoPGE(): List<QualityInfo>?
    suspend fun getQualityInfoPGENotRecountBreaking(): List<QualityInfo>?
    suspend fun getSurplusInfoForPGE(): List<QualityInfo>?
    suspend fun getQualityInfoPGEForDiscrepancy(): List<QualityInfo>?
    suspend fun getFailureReasons(): List<QualityInfo>?
    suspend fun getStatusInfoShipmentRC(): List<QualityInfo>?
    suspend fun getQualityInfoTransportMarriage(): List<QualityInfo>?
    suspend fun getQualityBoxesDefectInfo(): List<QualityInfo>?
    suspend fun getGrzCrGrundcatName(code: String): String?
}
