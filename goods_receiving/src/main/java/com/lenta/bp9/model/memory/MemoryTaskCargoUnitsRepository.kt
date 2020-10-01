package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskCargoUnitsRepository
import com.lenta.bp9.model.task.TaskCargoUnitInfo

class MemoryTaskCargoUnitsRepository : ITaskCargoUnitsRepository {

    private val cargoUnitsInfo: ArrayList<TaskCargoUnitInfo> = ArrayList()

    override fun getCargoUnits(): List<TaskCargoUnitInfo> {
        return cargoUnitsInfo
    }

    override fun findCargoUnits(cargoUnit: TaskCargoUnitInfo): TaskCargoUnitInfo? {
        return findCargoUnits(cargoUnit.cargoUnitNumber)
    }

    override fun findCargoUnits(cargoUnitNumber: String): TaskCargoUnitInfo? {
        return cargoUnitsInfo.firstOrNull { it.cargoUnitNumber == cargoUnitNumber}
    }

    override fun addCargoUnit(cargoUnit: TaskCargoUnitInfo): Boolean {
        var index = -1
        for (i in cargoUnitsInfo.indices) {
            if (cargoUnit.cargoUnitNumber == cargoUnitsInfo[i].cargoUnitNumber) {
                index = i
            }
        }

        if (index == -1) {
            cargoUnitsInfo.add(cargoUnit)
            return true
        } else if (index !=  cargoUnitsInfo.size - 1) {
            cargoUnitsInfo.getOrNull(index)?.let {
                cargoUnitsInfo.removeAt(index)
                cargoUnitsInfo.add(it)
            }
        }

        return false
    }

    override fun updateCargoUnits(newCargoUnits: List<TaskCargoUnitInfo>) {
        clear()
        newCargoUnits.map {
            addCargoUnit(it)
        }
    }

    override fun changeCargoUnit(cargoUnit: TaskCargoUnitInfo): Boolean {
        deleteCargoUnit(cargoUnit)
        return addCargoUnit(cargoUnit)
    }

    override fun deleteCargoUnit(cargoUnit: TaskCargoUnitInfo): Boolean {
        cargoUnitsInfo.map { it }.filter {unitInfo ->
            if (cargoUnit.cargoUnitNumber == unitInfo.cargoUnitNumber) {
                cargoUnitsInfo.remove(unitInfo)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun clear() {
        cargoUnitsInfo.clear()
    }
}