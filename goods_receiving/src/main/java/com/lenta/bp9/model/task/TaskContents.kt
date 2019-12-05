package com.lenta.bp9.model.task

import com.lenta.bp9.requests.network.DirectSupplierStartRecountRestInfo
import com.lenta.bp9.requests.network.TaskComposition
import com.lenta.bp9.requests.network.TaskContentsRequestResult
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

    fun getTaskContentsInfo(startRecountRestInfo: DirectSupplierStartRecountRestInfo) : TaskContentsInfo {
        return TaskContentsInfo(
                conversionToProductInfo(startRecountRestInfo.taskComposition),
                conversionToProductDiscrepancies(startRecountRestInfo.taskProductDiscrepancies),
                startRecountRestInfo.taskBatches.map {
                    TaskBatchInfo.from(it)
                },
                conversionToBatchesDiscrepancies(startRecountRestInfo.taskBatchesDiscrepancies),
                conversionToMercuryInfo(startRecountRestInfo.taskMercuryInfoRestData),
                conversionToMercuryNotActual(startRecountRestInfo.taskMercuryNotActualRestData)
        )
    }

    fun getTaskContentsInfo(startRecountRestInfo: TaskContentsRequestResult) : TaskContentsInfo {
        return TaskContentsInfo(
                conversionToProductInfo(startRecountRestInfo.taskComposition),
                conversionToProductDiscrepancies(startRecountRestInfo.taskProductDiscrepancies),
                startRecountRestInfo.taskBatches.map {
                    TaskBatchInfo.from(it)
                },
                conversionToBatchesDiscrepancies(startRecountRestInfo.taskBatchesDiscrepancies),
                conversionToMercuryInfo(startRecountRestInfo.taskMercuryInfoRestData),
                conversionToMercuryNotActual(startRecountRestInfo.taskMercuryNotActualRestData)
        )
    }

    private fun conversionToProductInfo(taskComposition: List<TaskComposition>) : List<TaskProductInfo> {
        return taskComposition.map {
            val materialInfo = zfmpUtz48V001.getProductInfoByMaterial(it.materialNumber)
            val uomInfo = zmpUtz07V001.getUomInfo(it.uom)
            TaskProductInfo(
                    materialNumber = materialInfo?.material ?: "",
                    description = materialInfo?.name ?: "",
                    uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                    type = getProductType(isAlco = it.isAlco == "X", isExcise = it.isExc == "X"),
                    isSet = it.isSet == "X",
                    sectionId = materialInfo?.abtnr ?: "",
                    matrixType = getMatrixType(materialInfo?.matrType ?: ""),
                    materialType = materialInfo?.matype ?: "",
                    origQuantity = it.origDeliveryQuantity,
                    orderQuantity = it.menge,
                    quantityCapitalized = it.volumeGoodsReceived,
                    overdToleranceLimit = it.overDeliveryToleranceLimit,
                    underdToleranceLimit = it.shortDeliveryToleranceLimit,
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
                    numberStampsControl = it.quantityStampsControl
            )
        }
    }

    private fun conversionToProductDiscrepancies(taskProductDiscrepanciesRestData: List<TaskProductDiscrepanciesRestData>) : List<TaskProductDiscrepancies> {
        return taskProductDiscrepanciesRestData.map {
            val uomInfo = zmpUtz07V001.getUomInfo(it.unit)
            TaskProductDiscrepancies(
                    materialNumber = it.materialNumber,
                    exidv = it.exidv,
                    numberDiscrepancies = it.numberDiscrepancies,
                    uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                    typeDiscrepancies = it.typeDiscrepancies,
                    isNotEdit = it.isNotEdit.isNotEmpty(),
                    isNew = it.isNew.isNotEmpty()
            )
        }
    }

    private fun conversionToBatchesDiscrepancies(taskBatchesDiscrepanciesRestData: List<TaskBatchesDiscrepanciesRestData>) : List<TaskBatchesDiscrepancies> {
        return taskBatchesDiscrepanciesRestData.map {
            val uomInfo = zmpUtz07V001.getUomInfo(it.unit)
            TaskBatchesDiscrepancies(
                    materialNumber = it.materialNumber,
                    exidv = it.exidv,
                    numberDiscrepancies = it.numberDiscrepancies,
                    uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                    typeDiscrepancies = it.typeDiscrepancies,
                    isNotEdit = it.isNotEdit.isNotEmpty(),
                    isNew = it.isNew.isNotEmpty()
            )
        }
    }

    private fun conversionToMercuryInfo(taskMercuryInfoRestData: List<TaskMercuryInfoRestData>) : List<TaskMercuryInfo> {
        return taskMercuryInfoRestData.map {
            val uomInfo = zmpUtz07V001.getUomInfo(it.unit)
            TaskMercuryInfo(
                    materialNumber= it.materialNumber,
                    vetDocumentID = it.vetDocumentID,
                    volume = it.volume.toDouble(),
                    uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                    typeDiscrepancies = it.typeDiscrepancies,
                    numberDiscrepancies = it.numberDiscrepancies.toDouble(),
                    productionDate = it.productionDates,
                    manufacturer = it.manufacturers,
                    productionDateTo = it.productionDateTo
            )
        }
    }

    private fun conversionToMercuryNotActual(taskMercuryNotActualRestData: List<TaskMercuryNotActualRestData>) : List<TaskMercuryNotActual> {
        return taskMercuryNotActualRestData.map {
            val uomInfo = zmpUtz07V001.getUomInfo(it.unit)
            TaskMercuryNotActual(
                    materialNumber= it.materialNumber,
                    vetDocumentID = it.vetDocumentID,
                    volume = it.volume.toDouble(),
                    productName = it.productName,
                    uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                    productionDate = it.productionDate,
                    manufacturer = it.manufacturer,
                    productionDateTo = it.productionDateTo
            )
        }
    }
}

data class TaskContentsInfo(
        val products: List<TaskProductInfo>,
        val productsDiscrepancies: List<TaskProductDiscrepancies>,
        val taskBatches: List<TaskBatchInfo>,
        val taskBatchesDiscrepancies: List<TaskBatchesDiscrepancies>,
        val taskMercuryInfo: List<TaskMercuryInfo>,
        val taskMercuryNotActual: List<TaskMercuryNotActual>
)