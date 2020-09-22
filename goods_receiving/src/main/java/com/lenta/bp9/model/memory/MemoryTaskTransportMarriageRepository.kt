package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskTransportMarriageRepository
import com.lenta.bp9.model.task.TaskTransportMarriageInfo

class MemoryTaskTransportMarriageRepository : ITaskTransportMarriageRepository {

    private val transportMarriageInfo: ArrayList<TaskTransportMarriageInfo> = ArrayList()

    override fun getTransportMarriage(): List<TaskTransportMarriageInfo> {
        return transportMarriageInfo
    }

    override fun findTransportMarriage(transportMarriage: TaskTransportMarriageInfo): List<TaskTransportMarriageInfo>? {
        return findTransportMarriage(transportMarriage.cargoUnitNumber, transportMarriage.materialNumber)
    }

    override fun findTransportMarriage(cargoUnitNumber: String, materialNumber: String): List<TaskTransportMarriageInfo>? {
        return transportMarriageInfo.filter { it.cargoUnitNumber == cargoUnitNumber && it.materialNumber == materialNumber}
    }

    override fun addTransportMarriage(transportMarriage: TaskTransportMarriageInfo): Boolean {
        var index = -1
        for (i in transportMarriageInfo.indices) {
            if (transportMarriage.cargoUnitNumber == transportMarriageInfo[i].cargoUnitNumber &&
                    transportMarriage.processingUnitNumber == transportMarriageInfo[i].processingUnitNumber &&
                    transportMarriage.materialNumber == transportMarriageInfo[i].materialNumber) {
                index = i
            }
        }

        if (index == -1) {
            transportMarriageInfo.add(transportMarriage)
            return true
        } else if (index !=  transportMarriageInfo.size - 1) {
            transportMarriageInfo.getOrNull(index)?.let {
                transportMarriageInfo.removeAt(index)
                transportMarriageInfo.add(it)
            }
        }

        return false
    }

    override fun updateTransportMarriage(newTransportMarriage: List<TaskTransportMarriageInfo>) {
        clear()
        newTransportMarriage.map {
            addTransportMarriage(it)
        }
    }

    override fun changeTransportMarriage(transportMarriage: TaskTransportMarriageInfo): Boolean {
        deleteTransportMarriage(transportMarriage)
        return addTransportMarriage(transportMarriage)
    }

    override fun deleteTransportMarriage(transportMarriage: TaskTransportMarriageInfo): Boolean {
        var index = -1
        for (i in transportMarriageInfo.indices) {
            if (transportMarriage.cargoUnitNumber == transportMarriageInfo[i].cargoUnitNumber &&
                    transportMarriage.processingUnitNumber == transportMarriageInfo[i].processingUnitNumber &&
                    transportMarriage.materialNumber == transportMarriageInfo[i].materialNumber) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }

        transportMarriageInfo.removeAt(index)
        return true
    }

    override fun clear() {
        transportMarriageInfo.clear()
    }
}