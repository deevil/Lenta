package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskZBatchesDiscrepanciesRepository
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
import com.lenta.shared.utilities.extentions.addItemToListWithPredicate
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate

class MemoryTaskZBatchesDiscrepanciesRepository : ITaskZBatchesDiscrepanciesRepository {

    private val zBatchesDiscrepancies: ArrayList<TaskZBatchesDiscrepancies> = ArrayList()
    private val partySignsOfZBatches: ArrayList<PartySignsOfZBatches> = ArrayList()


    override fun getZBatchesDiscrepancies(): List<TaskZBatchesDiscrepancies> {
        return zBatchesDiscrepancies
    }

    override fun findZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): List<TaskZBatchesDiscrepancies> {
        return zBatchesDiscrepancies.filter {
            discrepancies.materialNumber == it.materialNumber
                    && discrepancies.batchNumber == it.batchNumber
                    && discrepancies.processingUnit == it.processingUnit
                    && it.typeDiscrepancies == discrepancies.typeDiscrepancies
                    && discrepancies.manufactureCode == it.manufactureCode
                    && discrepancies.shelfLifeDate == it.shelfLifeDate
                    && discrepancies.shelfLifeTime == it.shelfLifeTime
        }
    }

    override fun findZBatchDiscrepanciesOfProduct(materialNumber: String): List<TaskZBatchesDiscrepancies> {
        return zBatchesDiscrepancies.filter { materialNumber == it.materialNumber }
    }

    override fun addZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): Boolean {
        return zBatchesDiscrepancies.addItemToListWithPredicate(discrepancies) {
            it.materialNumber == discrepancies.materialNumber
                    && it.batchNumber == discrepancies.batchNumber
                    && it.processingUnit == discrepancies.processingUnit
                    && it.typeDiscrepancies == discrepancies.typeDiscrepancies
                    && it.manufactureCode == discrepancies.manufactureCode
                    && it.shelfLifeDate == discrepancies.shelfLifeDate
                    && it.shelfLifeTime == discrepancies.shelfLifeTime
        }
    }

    override fun updateZBatchesDiscrepancy(newZBatchesDiscrepancies: List<TaskZBatchesDiscrepancies>) {
        clear()
        newZBatchesDiscrepancies.map {
            addZBatchDiscrepancies(it)
        }
    }

    override fun changeZBatchDiscrepancy(discrepancy: TaskZBatchesDiscrepancies): Boolean {
        deleteZBatchDiscrepancies(discrepancy)
        return addZBatchDiscrepancies(discrepancy)
    }

    override fun deleteZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): Boolean {
        return zBatchesDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber == discrepancies.materialNumber
                    && it.batchNumber == discrepancies.batchNumber
                    && it.processingUnit == discrepancies.processingUnit
                    && it.typeDiscrepancies == discrepancies.typeDiscrepancies
                    && it.manufactureCode == discrepancies.manufactureCode
                    && it.shelfLifeDate == discrepancies.shelfLifeDate
                    && it.shelfLifeTime == discrepancies.shelfLifeTime
        }
    }

    override fun deleteZBatchesDiscrepanciesForProduct(materialNumber: String): Boolean {
        return zBatchesDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber == materialNumber
        }
    }

    override fun deleteZBatchesDiscrepanciesNotNormForProduct(materialNumber: String): Boolean {
        return zBatchesDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber == materialNumber
                    && it.typeDiscrepancies != TYPE_DISCREPANCIES_QUALITY_NORM
        }
    }

    override fun deleteZBatchesDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean {
        return zBatchesDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber == materialNumber
                    && it.typeDiscrepancies == typeDiscrepancies
        }
    }

    override fun getCountAcceptOfZBatch(discrepancies: TaskZBatchesDiscrepancies): Double {
        var countAccept = 0.0
        findZBatchDiscrepancies(discrepancies)
                .filter {
                    it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                }.map {disc ->
                    countAccept += disc.numberDiscrepancies.toDouble()
                }
        return countAccept
    }

    override fun getCountAcceptOfZBatchPGE(discrepancies: TaskZBatchesDiscrepancies): Double {
        var countAccept = 0.0
        findZBatchDiscrepancies(discrepancies)
                .filter {
                    it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                            || it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
                }.map {disc ->
                    countAccept += disc.numberDiscrepancies.toDouble()
                }
        return countAccept
    }

    override fun getCountRefusalOfZBatchPGE(discrepancies: TaskZBatchesDiscrepancies): Double {
        var countRefusal = 0.0
        findZBatchDiscrepancies(discrepancies)
                .filter {
                    !(it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                            || it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS)
                }.map {disc ->
                    countRefusal += disc.numberDiscrepancies.toDouble()
                }
        return countRefusal
    }

    override fun findPartySignOfZBatch(zBatchesDiscrepancies: TaskZBatchesDiscrepancies): PartySignsOfZBatches? {
        return partySignsOfZBatches.findLast {
            it.materialNumber == zBatchesDiscrepancies.materialNumber
                    && it.batchNumber == zBatchesDiscrepancies.batchNumber
                    && it.processingUnit == zBatchesDiscrepancies.processingUnit
                    && it.typeDiscrepancies == zBatchesDiscrepancies.typeDiscrepancies
                    && it.manufactureCode == zBatchesDiscrepancies.manufactureCode
                    && it.shelfLifeDate == zBatchesDiscrepancies.shelfLifeDate
                    && it.shelfLifeTime == zBatchesDiscrepancies.shelfLifeTime
        }
    }

    override fun findPartySignsOfProduct(materialNumber: String, processingUnit: String): List<PartySignsOfZBatches> {
        return partySignsOfZBatches
                .filter { it.materialNumber == materialNumber
                        && it.processingUnit == processingUnit
                }
    }

    override fun updatePartySignFromZBatch(zBatchesDiscrepancies: TaskZBatchesDiscrepancies, productionDate: String) {
        addPartySignOfZBatches(
                PartySignsOfZBatches(
                        processingUnit = zBatchesDiscrepancies.processingUnit,
                        materialNumber = zBatchesDiscrepancies.materialNumber,
                        batchNumber = zBatchesDiscrepancies.batchNumber,
                        typeDiscrepancies = zBatchesDiscrepancies.typeDiscrepancies,
                        manufactureCode = zBatchesDiscrepancies.manufactureCode,
                        shelfLifeDate = zBatchesDiscrepancies.shelfLifeDate,
                        shelfLifeTime = zBatchesDiscrepancies.shelfLifeTime,
                        productionDate = productionDate,
                        partySign = PartySignsTypeOfZBatches.ProductionDate
                )
        )
    }

    override fun addPartySignOfZBatches(partySign: PartySignsOfZBatches): Boolean {
        return partySignsOfZBatches.addItemToListWithPredicate(partySign) {
            it.materialNumber == partySign.materialNumber
                    && it.batchNumber == partySign.batchNumber
                    && it.processingUnit == partySign.processingUnit
                    && it.typeDiscrepancies == partySign.typeDiscrepancies
                    && it.manufactureCode == partySign.manufactureCode
                    && it.shelfLifeDate == partySign.shelfLifeDate
                    && it.shelfLifeTime == partySign.shelfLifeTime
                    && it.productionDate == partySign.productionDate
        }
    }

    override fun changePartySign(partySign: PartySignsOfZBatches): Boolean {
        deletePartySignOfZBatches(partySign)
        return addPartySignOfZBatches(partySign)
    }

    override fun deletePartySignOfZBatches(partySign: PartySignsOfZBatches): Boolean {
        return partySignsOfZBatches.removeItemFromListWithPredicate {
            it.materialNumber == partySign.materialNumber
                    && it.batchNumber == partySign.batchNumber
                    && it.processingUnit == partySign.processingUnit
                    && it.typeDiscrepancies == partySign.typeDiscrepancies
                    && it.manufactureCode == partySign.manufactureCode
                    && it.shelfLifeDate == partySign.shelfLifeDate
                    && it.shelfLifeTime == partySign.shelfLifeTime
                    && it.productionDate == partySign.productionDate
        }
    }

    override fun clear() {
        zBatchesDiscrepancies.clear()
        partySignsOfZBatches.clear()
    }
}