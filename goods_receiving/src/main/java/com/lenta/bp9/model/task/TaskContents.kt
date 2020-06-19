package com.lenta.bp9.model.task

import com.lenta.bp9.requests.network.*
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.*
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class TaskContents
@Inject constructor() {

    @Inject
    lateinit var hyperHive: HyperHive

    private val zmpUtz07V001: ZmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
    }

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    suspend fun getTaskContentsInfo(startRecountRestInfo: DirectSupplierStartRecountRestInfo) : TaskContentsInfo {
        return TaskContentsInfo(
                conversionToProductInfo(startRecountRestInfo.taskComposition),
                startRecountRestInfo.taskProductDiscrepancies.map {
                    TaskProductDiscrepancies.from(hyperHive, it)
                },
                startRecountRestInfo.taskBatches.map {
                    TaskBatchInfo.from(it)
                },
                startRecountRestInfo.taskBatchesDiscrepancies.map {
                    TaskBatchesDiscrepancies.from(hyperHive, it)
                },
                startRecountRestInfo.taskMercuryInfoRestData.map {
                    TaskMercuryInfo.from(hyperHive, it)
                },
                startRecountRestInfo.taskExciseStamps.map {
                    TaskExciseStampInfo.from(it)
                },
                startRecountRestInfo.taskExciseStampsDiscrepancies.map {
                    TaskExciseStampDiscrepancies.from(it)
                },
                startRecountRestInfo.taskExciseStampBad.map {
                    TaskExciseStampBad.from(it)
                },
                startRecountRestInfo.taskBoxes.map {
                    TaskBoxInfo.from(it)
                },
                startRecountRestInfo.taskBoxesDiscrepancies.map {
                    TaskBoxDiscrepancies.from(it)
                }
        )
    }

    suspend fun getTaskContentsInfo(startRecountRestInfo: TaskContentsRequestResult) : TaskContentsInfo {
        return TaskContentsInfo(
                conversionToProductInfo(startRecountRestInfo.taskComposition),
                startRecountRestInfo.taskProductDiscrepancies.map {
                    TaskProductDiscrepancies.from(hyperHive, it)
                },
                startRecountRestInfo.taskBatches.map {
                    TaskBatchInfo.from(it)
                },
                startRecountRestInfo.taskBatchesDiscrepancies.map {
                    TaskBatchesDiscrepancies.from(hyperHive, it)
                },
                startRecountRestInfo.taskMercuryInfoRestData.map {
                    TaskMercuryInfo.from(hyperHive, it)
                },
                startRecountRestInfo.taskExciseStamps.map {
                    TaskExciseStampInfo.from(it)
                },
                startRecountRestInfo.taskExciseStampsDiscrepancies.map {
                    TaskExciseStampDiscrepancies.from(it)
                },
                startRecountRestInfo.taskExciseStampBad.map {
                    TaskExciseStampBad.from(it)
                },
                startRecountRestInfo.taskBoxes.map {
                    TaskBoxInfo.from(it)
                },
                startRecountRestInfo.taskBoxesDiscrepancies.map {
                    TaskBoxDiscrepancies.from(it)
                }
        )
    }

    suspend fun getTaskContentsRDSInfo(startRecountRestInfo: TaskContentsReceptionDistrCenterResult) : TaskContentsInfo {
        return TaskContentsInfo(
                conversionToProductInfo(startRecountRestInfo.taskComposition),
                startRecountRestInfo.taskProductDiscrepancies.map {
                    TaskProductDiscrepancies.from(hyperHive, it)
                },
                startRecountRestInfo.taskBatches.map {
                    TaskBatchInfo.from(it)
                },
                startRecountRestInfo.taskBatchesDiscrepancies.map {
                    TaskBatchesDiscrepancies.from(hyperHive, it)
                },

                //todo это условие прописано временно, т.к. на продакшене для ПГЕ и ПРЦ не реализована таблица ET_VET_DIFF, она приходит пустой в 28 и 30 рестах, поэтому обрабатываем данные товары не как вет, а как обычные
                if (startRecountRestInfo.taskMercuryInfoRestData != null) {
                    startRecountRestInfo.taskMercuryInfoRestData.map {
                        TaskMercuryInfo.from(hyperHive, it)
                    }
                } else null,

                startRecountRestInfo.taskExciseStamps.map {
                    TaskExciseStampInfo.from(it)
                },
                startRecountRestInfo.taskExciseStampsDiscrepancies.map {
                    TaskExciseStampDiscrepancies.from(it)
                },
                startRecountRestInfo.taskExciseStampBad.map {
                    TaskExciseStampBad.from(it)
                },
                startRecountRestInfo.taskBoxes.map {
                    TaskBoxInfo.from(it)
                },
                startRecountRestInfo.taskBoxesDiscrepancies.map {
                    TaskBoxDiscrepancies.from(it)
                }
        )
    }

    suspend fun getTaskContentsPGEInfo(startRecountRestInfo: StartRecountPGERestInfo) : TaskContentsInfo {
        return TaskContentsInfo(
                conversionToProductInfo(startRecountRestInfo.taskComposition),
                startRecountRestInfo.taskProductDiscrepancies.map {
                    TaskProductDiscrepancies.from(hyperHive, it)
                },
                startRecountRestInfo.taskBatches.map {
                    TaskBatchInfo.from(it)
                },
                startRecountRestInfo.taskBatchesDiscrepancies.map {
                    TaskBatchesDiscrepancies.from(hyperHive, it)
                },

                //todo это условие прописано временно, т.к. на продакшене для ПГЕ и ПРЦ не реализована таблица ET_VET_DIFF, она приходит пустой в 28 и 30 рестах, поэтому обрабатываем данные товары не как вет, а как обычные
                if (startRecountRestInfo.taskMercuryInfoRestData != null) {
                    startRecountRestInfo.taskMercuryInfoRestData.map {
                        TaskMercuryInfo.from(hyperHive, it)
                    }
                } else null,

                startRecountRestInfo.taskExciseStamps.map {
                    TaskExciseStampInfo.from(it)
                },
                startRecountRestInfo.taskExciseStampsDiscrepancies.map {
                    TaskExciseStampDiscrepancies.from(it)
                },
                startRecountRestInfo.taskExciseStampBad.map {
                    TaskExciseStampBad.from(it)
                },
                startRecountRestInfo.taskBoxes.map {
                    TaskBoxInfo.from(it)
                },
                startRecountRestInfo.taskBoxesDiscrepancies.map {
                    TaskBoxDiscrepancies.from(it)
                }
        )
    }

    private fun conversionToProductInfo(taskComposition: List<TaskComposition>) : List<TaskProductInfo> {
        return taskComposition.map {
            val materialInfo = zfmpUtz48V001.getProductInfoByMaterial(it.materialNumber)
            val uomInfo = zmpUtz07V001.getUomInfo(it.uom)
            val purchaseOrderUnitUomInfo = zmpUtz07V001.getUomInfo(it.purchaseOrderUnits)
            TaskProductInfo(
                    materialNumber = materialInfo?.material ?: "",
                    description = materialInfo?.name ?: "",
                    uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                    type = getProductType(isAlco = it.isAlco == "X", isExcise = it.isExc == "X"),
                    isSet = it.isSet == "X",
                    sectionId = materialInfo?.abtnr ?: "",
                    matrixType = getMatrixType(materialInfo?.matrType ?: ""),
                    materialType = materialInfo?.matype ?: "",
                    origQuantity = it.origDeliveryQuantity ?: "0.0",
                    orderQuantity = it.menge,
                    quantityCapitalized = it.volumeGoodsReceived ?: "0.0",
                    purchaseOrderUnits = Uom(code = purchaseOrderUnitUomInfo?.uom ?: "", name = purchaseOrderUnitUomInfo?.name ?: ""),
                    overdToleranceLimit = it.overDeliveryToleranceLimit ?: "0.0",
                    underdToleranceLimit = it.shortDeliveryToleranceLimit ?: "0.0",
                    upLimitCondAmount = it.upperLimitConditionAmount,
                    quantityInvest = it.quantityInvestments,
                    roundingSurplus = it.roundingSurplus,
                    roundingShortages = it.roundingShortages,
                    isNoEAN = it.noEAN == "X",
                    isWithoutRecount = it.noRecount == "X",
                    isUFF = it.isUFF == "X",
                    isNotEdit = it.notEdit == "X",
                    generalShelfLife = it.generalShelfLife,
                    remainingShelfLife = it.remainingShelfLife,
                    isRus = it.isRus == "X",
                    isBoxFl = it.BoxFl == "X",
                    isMarkFl = it.isStampFl == "X",
                    isVet = it.isVet == "X",
                    numberBoxesControl = it.quantityBoxesControl,
                    numberStampsControl = it.quantityStampsControl,
                    processingUnit = it.processingUnit ?: "",
                    isGoodsAddedAsSurplus = false,
                    mhdhbDays = materialInfo?.mhdhbDays ?: 0,
                    mhdrzDays = materialInfo?.mhdrzDays ?: 0
            )
        }
    }
}

data class TaskContentsInfo(
        val products: List<TaskProductInfo>,
        val productsDiscrepancies: List<TaskProductDiscrepancies>,
        val taskBatches: List<TaskBatchInfo>,
        val taskBatchesDiscrepancies: List<TaskBatchesDiscrepancies>,
        val taskMercuryInfo: List<TaskMercuryInfo>?,
        val taskExciseStampInfo: List<TaskExciseStampInfo>,
        val taskExciseStampDiscrepancies: List<TaskExciseStampDiscrepancies>,
        val taskExciseStampBad: List<TaskExciseStampBad>,
        val taskBoxes: List<TaskBoxInfo>,
        val taskBoxesDiscrepancies: List<TaskBoxDiscrepancies>
)