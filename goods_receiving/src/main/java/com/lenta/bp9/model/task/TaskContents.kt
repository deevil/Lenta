package com.lenta.bp9.model.task

import android.annotation.SuppressLint
import com.lenta.bp9.requests.network.*
import com.lenta.shared.fmp.resources.dao_ext.getEanInfoFromMaterial
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.platform.constants.Constants.DATE_FORMAT_yyyy_mm_dd
import com.mobrun.plugin.api.HyperHive
import java.text.SimpleDateFormat
import javax.inject.Inject

class TaskContents
@Inject constructor() {

    @Inject
    lateinit var hyperHive: HyperHive

    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat(DATE_FORMAT_yyyy_mm_dd)

    @SuppressLint("SimpleDateFormat")
    private val formatterERP = SimpleDateFormat(DATE_FORMAT_yyyy_mm_dd)

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
                startRecountRestInfo
                        .taskMercuryDiscrepancies
                        ?.map { TaskMercuryDiscrepancies.from(hyperHive, it) }
                        .orEmpty(),
                startRecountRestInfo.taskExciseStamps.map {
                    val batch = getBatchInfo(it.batchNumber, startRecountRestInfo.taskBatches)
                    TaskExciseStampInfo.from(
                            it.copy(
                                    organizationCodeEGAIS = batch?.egais.orEmpty(),
                                    bottlingDate = getBottlingDate(batch)
                            )
                    )
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
                },
                startRecountRestInfo.taskBlocks.map {
                    TaskBlockInfo.from(it)
                },
                startRecountRestInfo.taskBlocksDiscrepancies.map {
                    TaskBlockDiscrepancies.from(it)
                },
                startRecountRestInfo.taskZBatchesDiscrepancies
                        ?.map { TaskZBatchesDiscrepancies.from(hyperHive, it) }
                        .orEmpty()
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
                startRecountRestInfo
                        .taskMercuryDiscrepancies
                        ?.map { TaskMercuryDiscrepancies.from(hyperHive, it) }
                        .orEmpty(),
                startRecountRestInfo.taskExciseStamps.map {
                    val batch = getBatchInfo(it.batchNumber, startRecountRestInfo.taskBatches)
                    TaskExciseStampInfo.from(
                            it.copy(
                                    organizationCodeEGAIS = batch?.egais.orEmpty(),
                                    bottlingDate = getBottlingDate(batch)
                            )
                    )
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
                },
                startRecountRestInfo.taskBlocks.map {
                    TaskBlockInfo.from(it)
                },
                startRecountRestInfo.taskBlocksDiscrepancies.map {
                    TaskBlockDiscrepancies.from(it)
                },
                startRecountRestInfo.taskZBatchesDiscrepancies
                        ?.map { TaskZBatchesDiscrepancies.from(hyperHive, it) }
                        .orEmpty()
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
                startRecountRestInfo
                        .taskMercuryDiscrepancies
                        ?.map { TaskMercuryDiscrepancies.from(hyperHive, it) }
                        .orEmpty(),
                startRecountRestInfo.taskExciseStamps.map {
                    val batch = getBatchInfo(it.batchNumber, startRecountRestInfo.taskBatches)
                    TaskExciseStampInfo.from(
                            it.copy(
                                    organizationCodeEGAIS = batch?.egais.orEmpty(),
                                    bottlingDate = getBottlingDate(batch)
                            )
                    )
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
                },
                startRecountRestInfo.taskBlocks.map {
                    TaskBlockInfo.from(it)
                },
                startRecountRestInfo.taskBlocksDiscrepancies.map {
                    TaskBlockDiscrepancies.from(it)
                },
                startRecountRestInfo.taskZBatchesDiscrepancies
                        ?.map { TaskZBatchesDiscrepancies.from(hyperHive, it) }
                        .orEmpty()
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
                startRecountRestInfo
                        .taskMercuryDiscrepancies
                        ?.map { TaskMercuryDiscrepancies.from(hyperHive, it) }
                        .orEmpty(),
                startRecountRestInfo.taskExciseStamps.map {
                    val batch = getBatchInfo(it.batchNumber, startRecountRestInfo.taskBatches)
                    TaskExciseStampInfo.from(
                            it.copy(
                                    organizationCodeEGAIS = batch?.egais.orEmpty(),
                                    bottlingDate = getBottlingDate(batch)
                            )
                    )
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
                },
                startRecountRestInfo.taskBlocks.map {
                    TaskBlockInfo.from(it)
                },
                startRecountRestInfo.taskBlocksDiscrepancies.map {
                    TaskBlockDiscrepancies.from(it)
                },
                startRecountRestInfo.taskZBatchesDiscrepancies
                        ?.map { TaskZBatchesDiscrepancies.from(hyperHive, it) }
                        .orEmpty()
        )
    }

    private fun conversionToProductInfo(taskComposition: List<TaskComposition>) : List<TaskProductInfo> {
        return taskComposition.map {
            val materialInfo = ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(it.materialNumber)
            val uomInfo = ZmpUtz07V001(hyperHive).getUomInfo(it.uom)
            val purchaseOrderUnitUomInfo = ZmpUtz07V001(hyperHive).getUomInfo(it.purchaseOrderUnits)
            val eanInfo = ZmpUtz25V001(hyperHive).getEanInfoFromMaterial(it.materialNumber)
            TaskProductInfo(
                    materialNumber = materialInfo?.material.orEmpty(),
                    description = materialInfo?.name.orEmpty(),
                    uom = Uom(code = uomInfo?.uom.orEmpty(), name = uomInfo?.name.orEmpty()),
                    type = getProductType(isAlco = it.isAlco == "X", isExcise = it.isExc == "X", isZBatch = (it.isZBatches == "X" && it.isVet == "")),
                    isSet = it.isSet == "X",
                    sectionId = materialInfo?.abtnr.orEmpty(),
                    matrixType = getMatrixType(materialInfo?.matrType.orEmpty()),
                    materialType = materialInfo?.matype.orEmpty(),
                    origQuantity = it.origDeliveryQuantity ?: "0.0",
                    orderQuantity = it.menge,
                    quantityCapitalized = it.volumeGoodsReceived ?: "0.0",
                    purchaseOrderUnits = Uom(code = purchaseOrderUnitUomInfo?.uom.orEmpty(), name = purchaseOrderUnitUomInfo?.name.orEmpty()),
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
                    processingUnit = it.processingUnit.orEmpty(),
                    isGoodsAddedAsSurplus = false,
                    mhdhbDays = materialInfo?.mhdhbDays ?: 0,
                    mhdrzDays = materialInfo?.mhdrzDays ?: 0,
                    markType = getMarkType(it.markType.orEmpty()),
                    isCountingBoxes = it.isCountingBoxes == "X",
                    nestingInOneBlock = it.nestingInOneBlock ?: "0.0",
                    isControlGTIN = it.isControlGTIN == "X",
                    isGrayZone = it.isGrayZone == "X",
                    countPiecesBox = it.countPiecesBox,
                    numeratorConvertBaseUnitMeasure = eanInfo?.umrez?.toDouble() ?: 0.0,
                    denominatorConvertBaseUnitMeasure = eanInfo?.umren?.toDouble() ?: 0.0,
                    isZBatches = it.isZBatches == "X",
                    isNeedPrint = it.isNeedPrint == "X",
                    alternativeUnitMeasure = it.alternativeUnitMeasure.orEmpty(),
                    quantityAlternativeUnitMeasure = it.quantityAlternativeUnitMeasure?.toDoubleOrNull() ?: 0.0
            )
        }
    }

    private fun getBatchInfo(batchNumber: String, batches: List<TaskBatchInfoRestData>) : TaskBatchInfoRestData? {
        return batches
                .findLast { batchInfo ->
                    batchInfo.batchNumber == batchNumber
                }
    }

    private fun getBottlingDate(batch: TaskBatchInfoRestData?) : String {
        return batch
                ?.bottlingDate
                ?.takeIf { it.isNotEmpty() }
                ?.let { formatterERP.format(formatterEN.parse(it)) }
                .orEmpty()
    }
}

data class TaskContentsInfo(
        val products: List<TaskProductInfo>,
        val productsDiscrepancies: List<TaskProductDiscrepancies>,
        val taskBatches: List<TaskBatchInfo>,
        val taskBatchesDiscrepancies: List<TaskBatchesDiscrepancies>,
        val taskMercuryDiscrepancies: List<TaskMercuryDiscrepancies>,
        val taskExciseStampInfo: List<TaskExciseStampInfo>,
        val taskExciseStampDiscrepancies: List<TaskExciseStampDiscrepancies>,
        val taskExciseStampBad: List<TaskExciseStampBad>,
        val taskBoxes: List<TaskBoxInfo>,
        val taskBoxesDiscrepancies: List<TaskBoxDiscrepancies>,
        val taskBlock: List<TaskBlockInfo>,
        val taskBlockDiscrepancies: List<TaskBlockDiscrepancies>,
        val taskZBatchesDiscrepancies: List<TaskZBatchesDiscrepancies>
)