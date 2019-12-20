package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskCargoUnitInfo

interface ITaskCargoUnitsRepository {
    fun getCargoUnits(): List<TaskCargoUnitInfo>
    fun findCargoUnits(cargoUnit: TaskCargoUnitInfo): TaskCargoUnitInfo?
    fun findCargoUnits(cargoUnitNumber: String): TaskCargoUnitInfo?
    fun addCargoUnit(cargoUnit: TaskCargoUnitInfo): Boolean
    fun updateCargoUnits(newCargoUnits: List<TaskCargoUnitInfo>)
    fun changeCargoUnit(cargoUnit: TaskCargoUnitInfo): Boolean
    fun deleteCargoUnit(cargoUnit: TaskCargoUnitInfo): Boolean
    fun clear()
}